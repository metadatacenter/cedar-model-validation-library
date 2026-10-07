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
 * parent, a link default that is not an IRI, and a static field whose version is not one. An
 * identifier outside a field's value is an absolute IRI with no space separator, a version has no
 * leading zero and is not 0.0.0, and a child key has a visible character.
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
  @ValueSource(strings = {"https://example.org/a b", "https://example.org/%xx", "https://example.org/\ue000",
      "relative/path", "https://example.org/Niger\u00a0NER"})
  public void shouldFailLinkDefaultThatIsNotAnIri(String value) throws Exception {
    ObjectNode field = (ObjectNode) resource("fields/link-field.json");
    ((ObjectNode) field.get("_valueConstraints")).put("defaultValue", value);
    ValidationReport report = validator.validateTemplateField(field);
    assertValidationStatus(report, "false");
    assertValidationMessage(report, "A link field's default value must be an IRI");
  }

  @ParameterizedTest
  @ValueSource(strings = {"https://example.org/caf\u00e9", ""})
  public void shouldPassLinkDefaultThatIsAnIriOrEmpty(String value) throws Exception {
    // The readers take an empty default as no default.
    ObjectNode field = (ObjectNode) resource("fields/link-field.json");
    ((ObjectNode) field.get("_valueConstraints")).put("defaultValue", value);
    assertValidationStatus(validator.validateTemplateField(field), "true");
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "relative/path", "https://example.org/Niger\u00a0NER"})
  public void shouldFailSchemaIdentifierThatIsNotAnAbsoluteIri(String value) throws Exception {
    ObjectNode template = (ObjectNode) resource("templates/attribute-value-template.json");
    template.put("@id", value);
    ValidationReport report = validator.validateTemplate(template);
    assertValidationStatus(report, "false");
    assertValidationMessage(report, "@id must be an absolute IRI");
  }

  @Test
  public void shouldPassTemporaryChildIdentifier() throws Exception {
    // The server replaces a child's temporary identifier after validating a write.
    ObjectNode template = (ObjectNode) resource("templates/attribute-value-template.json");
    ((ObjectNode) template.get("properties").get("Name")).put("@id", "tmp-1542056961440-10793276");
    assertValidationStatus(validator.validateTemplate(template), "true");
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "relative/path"})
  public void shouldFailConstraintIriThatIsNotAnAbsoluteIri(String value) throws Exception {
    ObjectNode field = (ObjectNode) resource("fields/controlled-text-field-actions.json");
    ((ObjectNode) field.get("_valueConstraints").get("branches").get(0)).put("uri", value);
    ValidationReport report = validator.validateTemplateField(field);
    assertValidationStatus(report, "false");
    assertValidationMessage(report, "A branches constraint's uri must be an absolute IRI");
  }

  @Test
  public void shouldPassActionSourceUriOfTemplate() throws Exception {
    // The legacy editor writes "template" for a class the template itself supplies.
    ObjectNode field = (ObjectNode) resource("fields/controlled-text-field-actions.json");
    ((ObjectNode) field.get("_valueConstraints").get("actions").get(0)).put("sourceUri", "template");
    assertValidationStatus(validator.validateTemplateField(field), "true");
    ((ObjectNode) field.get("_valueConstraints").get("actions").get(0)).put("termUri", "relative/path");
    assertValidationMessage(validator.validateTemplateField(field), "An action's termUri must be an absolute IRI");
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "relative/path"})
  public void shouldFailInstanceBasedOnThatIsNotAnAbsoluteIri(String value) throws Exception {
    ObjectNode instance = (ObjectNode) resource("instances/attribute-value-instance.jsonld");
    instance.put("schema:isBasedOn", value);
    ValidationReport report = validator.validateTemplateInstance(instance,
        resource("templates/attribute-value-template.json"));
    assertValidationStatus(report, "false");
    assertValidationMessage(report, "schema:isBasedOn must be an absolute IRI");
  }

  @Test
  public void shouldFailBlankChildKey() throws Exception {
    JsonNode template = rename(resource("templates/attribute-value-template.json"), "Name", " ");
    ValidationReport report = validator.validateTemplate(template);
    assertValidationStatus(report, "false");
    assertValidationMessage(report, "Child keys must not be blank");
  }

  @ParameterizedTest
  @ValueSource(strings = {"01.2.3", "1.02.3", "1.2.03"})
  public void shouldFailVersionWithALeadingZero(String version) throws Exception {
    ObjectNode template = (ObjectNode) resource("templates/attribute-value-template.json");
    template.put("pav:version", version);
    assertValidationStatus(validator.validateTemplate(template), "false");
  }

  @Test
  public void shouldFailVersionZero() throws Exception {
    ObjectNode template = (ObjectNode) resource("templates/attribute-value-template.json");
    template.put("pav:version", "0.0.0");
    ValidationReport report = validator.validateTemplate(template);
    assertValidationStatus(report, "false");
    assertValidationMessage(report, "pav:version must not be 0.0.0");
  }

  @ParameterizedTest
  @ValueSource(strings = {"2147483648.0.0", "99999999999.0.0"})
  public void shouldFailVersionPartPastAnInt(String version) throws Exception {
    ObjectNode template = (ObjectNode) resource("templates/attribute-value-template.json");
    template.put("pav:version", version);
    ValidationReport report = validator.validateTemplate(template);
    assertValidationStatus(report, "false");
    assertValidationMessage(report, "Each part of pav:version must fit an int");
  }

  @Test
  public void shouldPassLargestVersion() throws Exception {
    ObjectNode template = (ObjectNode) resource("templates/attribute-value-template.json");
    template.put("pav:version", "2147483647.0.10");
    assertValidationStatus(validator.validateTemplate(template), "true");
  }

  @ParameterizedTest
  @ValueSource(strings = {"banana", "1.0", "1.2.3-rc1", ""})
  public void shouldFailStaticFieldVersionThatIsNotAVersion(String version) throws Exception {
    ObjectNode field = (ObjectNode) resource("fields/section-break.json");
    field.put("pav:version", version);
    assertValidationStatus(validator.validateTemplateField(field), "false");
  }
}
