package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.metadatacenter.model.validation.report.ValidationReport;

/**
 * Rules the artifact library's readers hold, which the validator holds too.
 *
 * <p>The validator gates what the server stores, and a reader opens what was stored. Where only the
 * reader held a rule, the server stored artifacts that nothing could open afterwards: a child keyed
 * as a JSON-LD keyword or an object internal, an attribute-value field keyed as YAML metadata of its
 * parent, a link default that is not an IRI, and a static field whose version is not one.
 */
public class ReaderRuleValidationTest extends BaseValidationTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private final ModelValidator validator = new CedarValidator();

  /** A copy with every key and string equal to {@code from} replaced by {@code to}. */
  private static JsonNode rename(JsonNode node, String from, String to) {
    if (node.isTextual()) return node.asText().equals(from) ? TextNode.valueOf(to) : node;
    if (node.isArray()) {
      ArrayNode copy = MAPPER.createArrayNode();
      node.forEach(item -> copy.add(rename(item, from, to)));
      return copy;
    }
    if (node.isObject()) {
      ObjectNode copy = MAPPER.createObjectNode();
      node.fields().forEachRemaining(entry ->
          copy.set(entry.getKey().equals(from) ? to : entry.getKey(), rename(entry.getValue(), from, to)));
      return copy;
    }
    return node;
  }

  private static JsonNode resource(String path) throws Exception {
    return MAPPER.readTree(TestResourcesUtils.getStringContent(path));
  }

  @ParameterizedTest
  @ValueSource(strings = {"@studyName", "__proto__", "constructor", "prototype", "rdfs:label", "skos:notation"})
  public void shouldFailReservedChildKey(String key) throws Exception {
    JsonNode template = rename(resource("templates/attribute-value-template.json"), "Name", key);
    ValidationReport report = validator.validateTemplate(template);
    assertValidationStatus(report, "false");
    assertValidationMessage(report, "Child key '" + key + "' is reserved for instance metadata");
  }

  @ParameterizedTest
  @ValueSource(strings = {"name", "children", "isBasedOn", "annotations"})
  public void shouldFailAttributeValueFieldKeyedAsTemplateYamlMetadata(String key) throws Exception {
    JsonNode template = rename(resource("templates/attribute-value-template.json"), "Additional Information", key);
    ValidationReport report = validator.validateTemplate(template);
    assertValidationStatus(report, "false");
    assertValidationMessage(report, "Attribute-value field key '" + key
        + "' is reserved for the YAML metadata of its parent");
  }

  @Test
  public void shouldPassOrdinaryChildKeyNamedLikeYamlMetadata() throws Exception {
    // Only an attribute-value field is written beside its parent's metadata.
    JsonNode template = rename(resource("templates/attribute-value-template.json"), "Name", "name");
    assertValidationStatus(validator.validateTemplate(template), "true");
  }

  @ParameterizedTest
  @ValueSource(strings = {"__proto__", "constructor", "prototype", "schema:identifier"})
  public void shouldFailAttributeNameTheReadersReserve(String name) throws Exception {
    JsonNode instance = resource("instances/attribute-value-instance.jsonld");
    ((ArrayNode) instance.get("Additional Information")).removeAll().add(name);
    ValidationReport report = validator.validateTemplateInstance(instance,
        resource("templates/attribute-value-template.json"));
    assertValidationStatus(report, "false");
    assertValidationMessage(report, "Attribute-value name '" + name + "' is reserved for instance metadata");
  }

  @Test
  public void shouldCheckAttributeNamesInAnElementInstance() throws Exception {
    JsonNode schema = resource("templates/attribute-value-template.json");
    JsonNode instance = resource("instances/attribute-value-instance.jsonld");
    assertValidationStatus(validator.validateElementInstance(instance, schema), "true");
    ((ArrayNode) instance.get("Additional Information")).removeAll().add("@context");
    ValidationReport report = validator.validateElementInstance(instance, schema);
    assertValidationStatus(report, "false");
    assertValidationMessage(report, "Attribute-value name '@context' is reserved for instance metadata");
  }

  @ParameterizedTest
  @ValueSource(strings = {"https://example.org/a b", "https://example.org/%xx", "https://example.org/\ue000"})
  public void shouldFailLinkDefaultThatIsNotAnIri(String value) throws Exception {
    ObjectNode field = (ObjectNode) resource("fields/link-field.json");
    ((ObjectNode) field.get("_valueConstraints")).put("defaultValue", value);
    ValidationReport report = validator.validateTemplateField(field);
    assertValidationStatus(report, "false");
    assertValidationMessage(report, "A link field's default value must be an IRI");
  }

  @Test
  public void shouldPassLinkDefaultThatIsAnIri() throws Exception {
    ObjectNode field = (ObjectNode) resource("fields/link-field.json");
    ((ObjectNode) field.get("_valueConstraints")).put("defaultValue", "https://example.org/Niger\u00a0NER");
    assertValidationStatus(validator.validateTemplateField(field), "true");
  }

  @ParameterizedTest
  @ValueSource(strings = {"banana", "1.0", "1.2.3-rc1", ""})
  public void shouldFailStaticFieldVersionThatIsNotAVersion(String version) throws Exception {
    ObjectNode field = (ObjectNode) resource("fields/section-break.json");
    field.put("pav:version", version);
    assertValidationStatus(validator.validateTemplateField(field), "false");
  }
}
