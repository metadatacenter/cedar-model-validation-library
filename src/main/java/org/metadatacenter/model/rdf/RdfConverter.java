package org.metadatacenter.model.rdf;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdErrorCode;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.rdf.nquads.NQuadsWriter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import jakarta.json.Json;
import org.metadatacenter.model.validation.IriReference;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

/**
 * CEDAR instance JSON to RDF. No network access, input mutation, or IRI normalization.
 * Empty fields and attribute-name lists carry no RDF statement. Populated information that
 * cannot be represented is rejected. An optional JSON template identifies mapped attribute groups.
 */
public final class RdfConverter {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private RdfConverter() {}

  public static String toNQuads(JsonNode instance) { return toNQuads(instance, null); }
  public static String toNQuads(JsonNode instance, JsonNode template) { return render(instance, template, false); }
  public static String toTurtle(JsonNode instance) { return toTurtle(instance, null); }
  /** Default-graph N-Triples is valid Turtle; named graphs require N-Quads. */
  public static String toTurtle(JsonNode instance, JsonNode template) { return render(instance, template, true); }

  public static JsonNode prepare(JsonNode instance, JsonNode template) {
    if (instance == null || !instance.isObject()) throw invalid("An instance must be a JSON object");
    Set<String> names = new HashSet<>();
    collectNames(instance, names);
    return prepare(instance, template, new Scope(new HashSet<>(), new HashMap<>(), new HashMap<>(), names, new int[]{0}));
  }

  private record Scope(Set<String> prefixes, Map<String, String> aliases, Map<String, JsonNode> definitions,
                       Set<String> names, int[] next) {}

  private static void collectNames(JsonNode node, Set<String> names) {
    if (node.isObject()) node.fields().forEachRemaining(e -> { names.add(e.getKey()); collectNames(e.getValue(), names); });
    else if (node.isArray()) node.forEach(n -> collectNames(n, names));
  }

  private static boolean empty(JsonNode node) {
    if (!node.isObject()) return false;
    if (node.isEmpty()) return true;
    if (!node.has("@value") || !node.get("@value").isNull()) return false;
    var keys = node.fieldNames();
    while (keys.hasNext()) if (!Set.of("@value", "@type", "@language").contains(keys.next())) return false;
    return true;
  }

  private static JsonNode childSchema(JsonNode schema, String name) {
    if (schema == null) return null;
    JsonNode child = schema.path("properties").path(name);
    return child.path("type").asText().equals("array") ? child.path("items") : child;
  }

  private static boolean nameList(String name, JsonNode value, JsonNode node, Scope scope, JsonNode schema) {
    if (name.startsWith("@") || !value.isArray() || value.isEmpty()) return false;
    boolean declared = schema != null && schema.path("_ui").path("inputType").asText().equals("attribute-value");
    if (!declared && (scope.definitions.containsKey(name) || scope.definitions.containsKey("@vocab"))) return false;
    for (JsonNode item : value) if (!item.isTextual() || item.asText().equals(name) || !node.has(item.asText())) return false;
    return true;
  }

  private static boolean needsAlias(String term, Set<String> prefixes) {
    if (term.startsWith("@")) return false;
    if (Set.of("__proto__", "constructor", "prototype").contains(term)) return true;
    int colon = term.indexOf(':');
    if (colon > 0 && prefixes.contains(term.substring(0, colon)) && !term.substring(colon + 1).startsWith("//")) return false;
    return term.contains("/") || colon >= 0;
  }

  private record Context(JsonNode node, Scope scope) {}
  private static Context context(JsonNode node, Scope outer) {
    if (node.isNull()) return new Context(node, new Scope(new HashSet<>(), new HashMap<>(), new HashMap<>(), outer.names, outer.next));
    if (node.isArray()) {
      ArrayNode entries = MAPPER.createArrayNode();
      Scope scope = outer;
      for (JsonNode entry : node) { Context entered = context(entry, scope); entries.add(entered.node); scope = entered.scope; }
      return new Context(entries, scope);
    }
    if (!node.isObject()) return new Context(node.deepCopy(), outer);
    Scope scope = new Scope(new HashSet<>(outer.prefixes), new HashMap<>(outer.aliases),
        new HashMap<>(outer.definitions), outer.names, outer.next);
    node.fields().forEachRemaining(e -> {
      String value = e.getValue().asText();
      if (e.getValue().isTextual() && (value.endsWith("/") || value.endsWith("#")) && !e.getKey().contains(":"))
        scope.prefixes.add(e.getKey());
      if (e.getValue().path("@prefix").asBoolean(false) && e.getValue().path("@id").isTextual()
          && !e.getKey().contains(":")) scope.prefixes.add(e.getKey());
    });
    ObjectNode result = MAPPER.createObjectNode();
    node.fields().forEachRemaining(e -> {
      String term = e.getKey();
      scope.definitions.put(term, e.getValue());
      if (needsAlias(term, scope.prefixes)) {
        String alias;
        do { alias = "cee-term-" + scope.next[0]++; } while (scope.names.contains(alias));
        scope.aliases.put(term, alias);
        result.set(alias, e.getValue().deepCopy());
      } else {
        scope.aliases.remove(term);
        result.set(term, e.getValue().deepCopy());
      }
    });
    return new Context(result, scope);
  }

  private static JsonNode prepare(JsonNode node, JsonNode schema, Scope outer) {
    if (node.isArray()) {
      ArrayNode result = MAPPER.createArrayNode();
      node.forEach(n -> { JsonNode value = prepare(n, schema, outer); if (!empty(value)) result.add(value); });
      return result;
    }
    if (!node.isObject()) return node.deepCopy();
    if (node.has("@value")) {
      if (node.has("@id")) throw invalid("An RDF field cannot contain both @id and @value");
      if (node.path("@type").isArray() && node.get("@type").size() > 1) throw invalid("An RDF literal has at most one datatype");
      if (node.get("@value").isNull() && !empty(node)) throw invalid("A null literal carries metadata RDF would discard");
    }
    ObjectNode result = MAPPER.createObjectNode();
    Context entered = node.has("@context") ? context(node.get("@context"), outer) : new Context(null, outer);
    if (entered.node != null) result.set("@context", entered.node);
    node.fields().forEachRemaining(e -> {
      String key = e.getKey();
      JsonNode value = e.getValue();
      JsonNode child = childSchema(schema, key);
      if (key.equals("@context") || key.equals("@id") && value.isNull() || nameList(key, value, node, entered.scope, child)) return;
      if (key.equals("@type") && node.has("@value") && value.isArray()) {
        if (value.size() == 1) result.set(key, value.get(0).deepCopy());
        return;
      }
      JsonNode converted = prepare(value, child, entered.scope);
      if (empty(converted) || converted.isArray() && converted.isEmpty()) return;
      result.set(entered.scope.aliases.getOrDefault(key, key), converted);
    });
    return result;
  }

  private static void iri(String value) {
    if (value.startsWith("_:")) {
      if (!value.matches("_:[A-Za-z0-9_][A-Za-z0-9._-]*") || value.endsWith(".")) throw invalid("Invalid blank-node identifier");
      return;
    }
    try {
      if (!IriReference.toUri(value).isAbsolute()) throw invalid("RDF requires an absolute IRI: " + value);
    } catch (java.net.URISyntaxException e) { throw invalid("Invalid RDF IRI: " + value); }
  }

  /** Protect validated RDF terms from URI-only processor checks; restore their exact spelling. */
  private static final class Terms {
    final Map<String, String> forward = new HashMap<>(), reverse = new HashMap<>();
    final String prefix;
    Terms(JsonNode input) {
      String candidate = "urn:cedar:rdf:";
      while (input.toString().contains(candidate)) candidate += "x:";
      prefix = candidate;
    }
    JsonNode protect(JsonNode node) {
      if (!node.isTextual()) throw invalid("RDF identifiers and datatypes must be strings");
      String original = node.asText();
      iri(original);
      if (original.startsWith("_:")) return node;
      return TextNode.valueOf(forward.computeIfAbsent(original, key -> {
        String token = prefix + forward.size(); reverse.put(token, key); return token;
      }));
    }
    String restore(String value) { return reverse.getOrDefault(value, value); }
    JsonNode walk(JsonNode node) {
      if (node.isArray()) { ArrayNode a = MAPPER.createArrayNode(); node.forEach(n -> a.add(walk(n))); return a; }
      if (!node.isObject()) return node.deepCopy();
      ObjectNode result = MAPPER.createObjectNode();
      node.fields().forEachRemaining(e -> {
        String key = e.getKey(); JsonNode value = e.getValue();
        if (key.equals("@language") && (!value.isTextual() || !value.asText().matches("[a-zA-Z]+(-[a-zA-Z0-9]+)*")))
          throw invalid("Invalid RDF language tag");
        if (key.equals("@value") || key.equals("@language")) { result.set(key, value.deepCopy()); return; }
        if (key.equals("@index") || key.equals("@direction") || key.equals("@json")) throw invalid("RDF export cannot preserve " + key);
        if (key.equals("@id") || key.equals("@type")) {
          boolean numeric = key.equals("@type") && node.path("@value").isNumber();
          if (value.isArray()) {
            ArrayNode a = MAPPER.createArrayNode();
            value.forEach(v -> { JsonNode p = protect(v); a.add(numeric ? v : p); });
            result.set(key, a);
          } else { JsonNode p = protect(value); result.set(key, numeric ? value : p); }
        } else {
          if (key.startsWith("@") && !Set.of("@graph", "@list", "@reverse", "@included").contains(key))
            throw invalid("Unsupported expanded RDF keyword: " + key);
          if (key.startsWith("_:")) throw invalid("RDF predicates must be IRIs");
          result.set(key.startsWith("@") ? key : protect(TextNode.valueOf(key)).asText(), walk(value));
        }
      });
      return result;
    }
  }

  private static String render(JsonNode instance, JsonNode template, boolean turtle) {
    JsonNode prepared = prepare(instance, template);
    JsonLdOptions options = new JsonLdOptions();
    options.setUndefinedTermsPolicy(JsonLdOptions.ProcessingPolicy.Fail);
    options.setDocumentLoader((url, loaderOptions) -> {
      throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Remote JSON-LD contexts are disabled");
    });
    try {
      // Even a node making no assertions must have a valid identifier and context.
      if (prepared.has("@id") && prepared.get("@id").isTextual()) iri(prepared.get("@id").asText());
      var document = JsonDocument.of(Json.createReader(new StringReader(prepared.toString())).read());
      JsonNode expanded = MAPPER.readTree(JsonLd.expand(document).options(options).get().toString());
      Terms terms = new Terms(expanded);
      JsonNode protectedDocument = terms.walk(expanded);
      StringWriter output = new StringWriter();
      NQuadsWriter writer = new NQuadsWriter(output);
      JsonLd.toRdf(JsonDocument.of(Json.createReader(new StringReader(protectedDocument.toString())).read()))
          .options(options).provide((subject, predicate, object, datatype, language, direction, graph) -> {
            if (turtle && graph != null) throw invalid("Turtle cannot preserve named graphs; use N-Quads");
            // Only resource objects are IRIs; lexical literal contents must never be substituted.
            return writer.quad(terms.restore(subject), terms.restore(predicate),
                datatype == null && language == null ? terms.restore(object) : object,
                terms.restore(datatype), language, direction, terms.restore(graph));
          });
      return output.toString();
    } catch (IllegalArgumentException e) { throw e; }
    catch (Exception e) { throw new IllegalArgumentException("Cannot export instance as RDF: " + e.getMessage(), e); }
  }

  private static IllegalArgumentException invalid(String message) { return new IllegalArgumentException(message); }
}
