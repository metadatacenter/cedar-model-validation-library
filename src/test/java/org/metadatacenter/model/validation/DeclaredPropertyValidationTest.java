package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.metadatacenter.model.validation.report.ValidationReport;

/**
 * Top-level keys the model libraries read and write, which the meta-schemas now declare.
 *
 * <p>An element's SKOS labels, a static field's preferred label and status, a field's provenance and
 * a template's own annotations all survive both libraries, and production holds each of them. The
 * meta-schemas left them out, so the validator took any value in their place. Each is now typed
 * wherever the artifact can appear: on its own and embedded in a template or an element.
 */
public class DeclaredPropertyValidationTest extends BaseValidationTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String DOI = "https://datacite.com/doi";
  private final ModelValidator validator = new CedarValidator();

  private static ObjectNode resource(String path) throws Exception {
    return (ObjectNode) MAPPER.readTree(TestResourcesUtils.getStringContent(path));
  }

  private static ObjectNode child(ObjectNode parent, String name) {
    JsonNode declared = parent.get("properties").get(name);
    return (ObjectNode) (declared.has("items") ? declared.get("items") : declared);
  }

  @Test
  public void shouldTypeAnElementsLabels() throws Exception {
    ObjectNode element = resource("elements/empty-element.json");
    element.put("skos:prefLabel", "Label");
    element.putArray("skos:altLabel").add("Alternate");
    assertValidationStatus(validator.validateTemplateElement(element), "true");

    element.put("skos:prefLabel", 5);
    assertValidationStatus(validator.validateTemplateElement(element), "false");
    element.put("skos:prefLabel", "Label").put("skos:altLabel", "Alternate");
    assertValidationStatus(validator.validateTemplateElement(element), "false");
  }

  @Test
  public void shouldTypeAnEmbeddedElementsLabels() throws Exception {
    ObjectNode element = resource("elements/nested-element.json");
    child(element, "Quantity").put("skos:prefLabel", "Quantity");
    assertValidationStatus(validator.validateTemplateElement(element), "true");
    child(element, "Quantity").put("skos:prefLabel", 5);
    assertValidationStatus(validator.validateTemplateElement(element), "false");

    ObjectNode template = resource("templates/multiple-element-items-template.json");
    child(template, "Participant").put("skos:prefLabel", "Participant");
    assertValidationStatus(validator.validateTemplate(template), "true");
    child(template, "Participant").put("skos:prefLabel", 5);
    assertValidationStatus(validator.validateTemplate(template), "false");
  }

  @Test
  public void shouldTypeAStaticFieldsLabelAndStatus() throws Exception {
    ObjectNode field = resource("fields/section-break.json");
    field.put("skos:prefLabel", "preamble").put("bibo:status", "bibo:draft");
    assertValidationStatus(validator.validateTemplateField(field), "true");

    field.put("bibo:status", "bibo:archived");
    assertValidationStatus(validator.validateTemplateField(field), "false");
    field.put("bibo:status", "bibo:draft").put("skos:prefLabel", 5);
    assertValidationStatus(validator.validateTemplateField(field), "false");
  }

  @Test
  public void shouldTypeAnEmbeddedStaticFieldsStatus() throws Exception {
    ObjectNode template = resource("templates/static-field-template.json");
    child(template, "About Study Form").put("bibo:status", "bibo:draft");
    assertValidationStatus(validator.validateTemplate(template), "true");
    child(template, "About Study Form").put("bibo:status", "bibo:archived");
    assertValidationStatus(validator.validateTemplate(template), "false");
  }

  @Test
  public void shouldTypeAFieldsProvenance() throws Exception {
    ObjectNode field = resource("fields/text-field.json");
    field.put("pav:previousVersion", "https://repo.metadatacenter.org/template-fields/previous")
        .put("pav:derivedFrom", "https://repo.metadatacenter.org/template-fields/source");
    assertValidationStatus(validator.validateTemplateField(field), "true");

    // A number, not an empty string: the identifier rule already refuses that, so only the declared
    // type can refuse this.
    field.put("pav:previousVersion", 5);
    assertValidationStatus(validator.validateTemplateField(field), "false");
    field.put("pav:previousVersion", "https://repo.metadatacenter.org/template-fields/previous").put("pav:derivedFrom", 5);
    assertValidationStatus(validator.validateTemplateField(field), "false");
  }

  @Test
  public void shouldTypeAnEmbeddedFieldsProvenance() throws Exception {
    ObjectNode template = resource("templates/many-fields-template.json");
    child(template, "Study Name").put("pav:previousVersion", "https://repo.metadatacenter.org/template-fields/previous");
    assertValidationStatus(validator.validateTemplate(template), "true");
    child(template, "Study Name").put("pav:previousVersion", 5);
    assertValidationStatus(validator.validateTemplate(template), "false");
  }

  @Test
  public void shouldTypeATemplatesAnnotations() throws Exception {
    ObjectNode template = resource("templates/attribute-value-template.json");
    template.putObject("_annotations").putObject(DOI).put("@id", "https://doi.org/10.60745/k2wv-x835");
    ValidationReport report = validator.validateTemplate(template);
    assertValidationStatus(report, "true");

    ((ObjectNode) template.get("_annotations")).putObject(DOI).put("@id", 5);
    assertValidationStatus(validator.validateTemplate(template), "false");
  }
}
