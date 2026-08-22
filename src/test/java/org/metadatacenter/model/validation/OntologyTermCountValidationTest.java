package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metadatacenter.model.validation.report.ValidationReport;

/**
 * Pins the term count that a whole-ontology value constraint may carry.
 *
 * When the terminology layer reports no count for an ontology, the authoring surface serializes the
 * constraint with {@code numTerms: 0}. GAZ is the ontology that exposed this: the meta-schema required a
 * minimum of 1, so such a template could not be saved at all, and because field kinds are validated
 * through a {@code oneOf}, the single failure surfaced as a cascade of unrelated-looking errors. Zero is
 * now accepted, in both the {@code valueConstraintsOntologiesFieldItemContent} fragment and the five
 * meta-schemas generated from it.
 *
 * Zero is the floor rather than the absence of one, so a negative count is still refused and the count is
 * still an integer. Absence is accepted too, and remains the better way to say the count is unknown, since
 * a stored zero cannot be told apart from an ontology that genuinely has no terms.
 *
 * Uses DataCiteTemplate, a passing fixture whose single ontologies entry reports a real count.
 */
public class OntologyTermCountValidationTest extends BaseValidationTest {

  private final ObjectMapper mapper = new ObjectMapper();
  private ModelValidator validator;

  @BeforeEach public void createValidator() {
    validator = new CedarValidator();
  }

  private ValidationReport validate(JsonNode template) {
    try {
      return validator.validateTemplate(template);
    } catch (Exception e) {
      throw new RuntimeException("Programming error", e);
    }
  }

  private JsonNode dataCiteTemplate() {
    try {
      return mapper.readTree(TestResourcesUtils.getStringContent("templates/DataCiteTemplate.json"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** The first non-empty {@code ontologies[0]} entry anywhere in the template. */
  private ObjectNode firstOntologyEntry(JsonNode node) {
    if (node.isObject()) {
      JsonNode ontologies = node.get("ontologies");
      if (ontologies != null && ontologies.isArray() && ontologies.size() > 0 && ontologies.get(0).isObject()) {
        return (ObjectNode) ontologies.get(0);
      }
    }
    for (JsonNode child : node) {
      ObjectNode found = firstOntologyEntry(child);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  @Test public void unknownTermCountAsZeroPassesValidation() {
    JsonNode template = dataCiteTemplate();
    assertValidationStatus(validate(template), "true"); // baseline: the fixture validates

    firstOntologyEntry(template).put("numTerms", 0);

    assertValidationStatus(validate(template), "true");
  }

  @Test public void absentTermCountPassesValidation() {
    JsonNode template = dataCiteTemplate();
    firstOntologyEntry(template).remove("numTerms"); // only uri is required

    assertValidationStatus(validate(template), "true");
  }

  @Test public void negativeTermCountFailsValidation() {
    JsonNode template = dataCiteTemplate();
    firstOntologyEntry(template).put("numTerms", -1);

    assertValidationStatus(validate(template), "false");
  }

  @Test public void nonIntegerTermCountFailsValidation() {
    JsonNode template = dataCiteTemplate();
    firstOntologyEntry(template).put("numTerms", "many");

    assertValidationStatus(validate(template), "false");
  }
}
