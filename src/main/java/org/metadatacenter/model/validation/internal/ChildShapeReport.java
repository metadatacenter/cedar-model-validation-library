package org.metadatacenter.model.validation.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.ValidationMessage;
import org.metadatacenter.model.core.CedarModelVocabulary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * The meta-schema's errors about a template's or element's children, reported child by child.
 *
 * <p>A child of a template or element may be any of several shapes, and the meta-schema says so with
 * a {@code oneOf}. A child that matches none of them fails every alternative, and the validator
 * reports each alternative's failures: a hundred errors for one wrong value, most of them about
 * properties the child was never meant to have, at places it does not have. This finds the
 * alternative a child declares itself to be, by its {@code @type}, {@code _ui.inputType} and whether
 * it is a list, and reports only what that alternative says is wrong, descending into elements and
 * lists. A child that declares no alternative gets one error saying what it lacks.
 *
 * <p>The verdict never changes: a child with errors keeps at least one, its own alternative's when
 * that alternative finds any and the original ones otherwise.
 */
public final class ChildShapeReport {

  /** One error at an absolute location, as the report records it. */
  public record Located(String location, String keyword, String message, String schemaPointer) {}

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String USER_SPECIFIED = "userSpecifiedFields";
  private static final String LIST = "multiInstanceTemplateFieldOrElement";
  private static final Map<String, JsonSchema> ALTERNATIVE_SCHEMAS = new ConcurrentHashMap<>();

  private final JsonNode metaSchema;
  private final String metaSchemaName;
  private final JsonSchemaFactory factory;
  private final SchemaValidatorsConfig config;
  private final Predicate<String> userSpecified;

  public ChildShapeReport(JsonNode metaSchema, String metaSchemaName, JsonSchemaFactory factory,
                          SchemaValidatorsConfig config, Predicate<String> userSpecified) {
    this.metaSchema = metaSchema;
    this.metaSchemaName = metaSchemaName;
    this.factory = factory;
    this.config = config;
    this.userSpecified = userSpecified;
  }

  /** The errors about {@code node}, at {@code location}, with each defective child's reported on its own terms. */
  public List<Located> report(JsonNode node, String location, List<Located> errors) {
    if (!metaSchema.path("definitions").has(USER_SPECIFIED) && !itemAlternatives().has("oneOf")) {
      return errors;
    }
    List<Located> remaining = new ArrayList<>(errors);
    for (Child child : children(node, location)) {
      List<Located> under = remaining.stream().filter(e -> isAtOrUnder(e.location(), child.location())).toList();
      if (under.isEmpty()) {
        continue;
      }
      remaining.removeAll(under);
      Declared declared = declared(child.node(), child.inList());
      if (declared.definition() == null) {
        remaining.add(new Located(child.location() + declared.pointer(), "childShape",
            "'" + child.name() + "' is not a field or element: " + declared.problem() + ".", null));
        continue;
      }
      List<Located> own = report(child.node(), child.location(), validate(declared.definition(), child.node(),
          child.location()));
      remaining.addAll(own.isEmpty() ? under : own);
    }
    return remaining;
  }

  private record Child(String name, JsonNode node, String location, boolean inList) {}

  /** The user-specified children of a template or element, or the item of a list. */
  private List<Child> children(JsonNode node, String location) {
    List<Child> children = new ArrayList<>();
    if (isList(node)) {
      if (node.get("items").isObject()) {
        String name = location.isEmpty() ? node.path("schema:name").asText("the list") : nameOf(location);
        children.add(new Child(name, node.get("items"), location + "/items", true));
      }
      return children;
    }
    JsonNode properties = node.path("properties");
    if (!properties.isObject() || !isContainer(node)) {
      return children;
    }
    for (Iterator<Map.Entry<String, JsonNode>> it = properties.fields(); it.hasNext(); ) {
      Map.Entry<String, JsonNode> entry = it.next();
      if (userSpecified.test(entry.getKey())) {
        children.add(new Child(entry.getKey(), entry.getValue(),
            location + "/properties/" + escape(entry.getKey()), false));
      }
    }
    return children;
  }

  private static boolean isContainer(JsonNode node) {
    String type = node.path("@type").asText();
    return type.equals("https://schema.metadatacenter.org/core/Template")
        || type.equals("https://schema.metadatacenter.org/core/TemplateElement");
  }

  private static boolean isList(JsonNode node) {
    return node.isObject() && node.path("type").asText().equals("array") && node.has("items");
  }

  /** The alternative a child declares itself to be, or what it lacks to declare one. */
  private record Declared(String definition, String problem, String pointer) {}

  private Declared declared(JsonNode child, boolean inList) {
    List<String> alternatives = alternatives(inList ? itemAlternatives() : definition(USER_SPECIFIED));
    if (!child.isObject()) {
      return new Declared(null, "it is not an object", "");
    }
    if (isList(child)) {
      return alternatives.contains(LIST) ? new Declared(LIST, null, null)
          : new Declared(null, "a list can not hold a list", "");
    }
    if (child.path("type").asText().equals("array")) {
      return new Declared(null, "it is a list without an items object", "");
    }
    JsonNode type = child.get("@type");
    if (type == null || !type.isTextual()) {
      return new Declared(null, "it has no @type", "");
    }
    List<String> byType = alternatives.stream().filter(a -> typesOf(a).contains(type.asText())).toList();
    if (byType.isEmpty()) {
      boolean elsewhere = alternatives(inList ? definition(USER_SPECIFIED) : itemAlternatives()).stream()
          .anyMatch(a -> typesOf(a).contains(type.asText()));
      return new Declared(null, elsewhere ? (inList ? "a static field can not be repeated"
          : "its @type " + type.asText() + " can only appear in a list") : "its @type " + type.asText()
          + " is not a field or element type", "/@type");
    }
    List<String> needInput = byType.stream().filter(a -> !inputTypesOf(a).isEmpty()).toList();
    if (needInput.isEmpty()) {
      return new Declared(byType.get(0), null, null);
    }
    JsonNode ui = child.get(CedarModelVocabulary.UI);
    if (ui == null || !ui.isObject()) {
      return new Declared(null, "it has no _ui", "");
    }
    JsonNode input = ui.get(CedarModelVocabulary.INPUT_TYPE);
    if (input == null || !input.isTextual()) {
      return new Declared(null, "its _ui has no inputType", "/_ui");
    }
    for (String alternative : needInput) {
      if (inputTypesOf(alternative).contains(input.asText())) {
        return new Declared(alternative, null, null);
      }
    }
    boolean elsewhere = alternatives(inList ? definition(USER_SPECIFIED) : itemAlternatives()).stream()
        .anyMatch(a -> inputTypesOf(a).contains(input.asText()));
    String problem = !elsewhere ? "its _ui.inputType '" + input.asText() + "' is not an input type"
        : inList ? "its _ui.inputType '" + input.asText() + "' can not be repeated"
        : "its _ui.inputType '" + input.asText() + "' must be a list";
    return new Declared(null, problem, "/_ui/inputType");
  }

  /** The shapes a list's item may take: in a template or element meta-schema, a list child's; in a list field's, its own. */
  private JsonNode itemAlternatives() {
    JsonNode list = definition(LIST);
    return (list.isMissingNode() ? metaSchema : list).path("properties").path("items");
  }

  private JsonNode definition(String name) {
    return metaSchema.path("definitions").path(name);
  }

  private static List<String> alternatives(JsonNode oneOfOwner) {
    List<String> names = new ArrayList<>();
    for (JsonNode alternative : oneOfOwner.path("oneOf")) {
      String ref = alternative.path("$ref").asText();
      if (ref.startsWith("#/definitions/")) {
        names.add(ref.substring("#/definitions/".length()));
      }
    }
    return names;
  }

  /** The {@code @type} values an alternative accepts. */
  private Set<String> typesOf(String alternative) {
    return enumOf(resolve(definition(alternative).path("properties").path("@type")));
  }

  /** The {@code _ui.inputType} values an alternative accepts, or none when it does not constrain one. */
  private Set<String> inputTypesOf(String alternative) {
    JsonNode ui = resolve(definition(alternative).path("properties").path(CedarModelVocabulary.UI));
    return enumOf(resolve(ui.path("properties").path(CedarModelVocabulary.INPUT_TYPE)));
  }

  private JsonNode resolve(JsonNode node) {
    String ref = node.path("$ref").asText();
    return ref.startsWith("#/definitions/") ? resolve(definition(ref.substring("#/definitions/".length()))) : node;
  }

  private static Set<String> enumOf(JsonNode node) {
    Set<String> values = new LinkedHashSet<>();
    node.path("enum").forEach(value -> values.add(value.asText()));
    return values;
  }

  /** What one alternative of the meta-schema says about a child, at the child's location. */
  private List<Located> validate(String definition, JsonNode child, String location) {
    JsonSchema schema = ALTERNATIVE_SCHEMAS.computeIfAbsent(metaSchemaName + "#" + definition, key -> {
      ObjectNode root = MAPPER.createObjectNode();
      root.set("$schema", metaSchema.get("$schema"));
      root.putArray("allOf").addObject().put("$ref", "#/definitions/" + definition);
      root.set("definitions", metaSchema.get("definitions"));
      return factory.getSchema(root, config);
    });
    List<Located> located = new ArrayList<>();
    for (ValidationMessage message : schema.validate(child)) {
      for (ParsedProcessingMessage.ReportItem item : new ParsedProcessingMessage(message, metaSchemaName).getReportItems()) {
        String relative = item.getLocation();
        String absolute = location + (relative.equals("/") ? "" : relative);
        // networknt begins a message with the location it was found at, which here is the child's.
        String text = item.getMessage() == null ? null : item.getMessage().replaceFirst(
            "^" + java.util.regex.Pattern.quote(relative.equals("/") ? "" : relative) + ":",
            java.util.regex.Matcher.quoteReplacement(absolute) + ":");
        located.add(new Located(absolute, message.getType(), text, item.getSchemaPointer()));
      }
    }
    return located;
  }

  private static boolean isAtOrUnder(String location, String prefix) {
    return location.equals(prefix) || location.startsWith(prefix + "/");
  }

  private static String nameOf(String location) {
    String last = location.substring(location.lastIndexOf('/') + 1);
    return last.replace("~1", "/").replace("~0", "~");
  }

  private static String escape(String key) {
    return key.replace("~", "~0").replace("/", "~1");
  }
}
