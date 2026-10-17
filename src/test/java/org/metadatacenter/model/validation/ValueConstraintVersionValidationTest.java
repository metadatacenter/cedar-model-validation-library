package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.metadatacenter.model.validation.report.ValidationReport;

/**
 * Validates that the additive, source-explicit value-constraint fields (iri/sourceSystem/version,
 * VERSIONING-ROADMAP "The Model" §6) pass the template meta-schema, while {@code additionalProperties:false} is
 * still enforced for genuinely unknown fields. Uses DataCiteTemplate (a passing fixture with an
 * ontology constraint) and injects the fields into its first ontologies entry.
 */
public class ValueConstraintVersionValidationTest extends BaseValidationTest {

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

  @Test public void pinnedVersionConstraintPassesValidation() {
    JsonNode template = dataCiteTemplate();
    assertValidationStatus(validate(template), "true"); // baseline: the fixture validates

    ObjectNode ontology = firstOntologyEntry(template);
    ontology.put("iri", "http://purl.obolibrary.org/obo/doid");
    ontology.put("sourceSystem", "BioPortal");
    ObjectNode version = ontology.putObject("version");
    version.put("id", "63ef56dff672");
    version.put("effectiveDate", "2026-07-01");
    version.put("declaredVersion", "2026-06-30");

    assertValidationStatus(validate(template), "true"); // the additive fields are accepted
  }

  @Test public void explicitLatestVersionStringPassesValidation() {
    JsonNode template = dataCiteTemplate();
    firstOntologyEntry(template).put("version", "latest"); // polymorphic string form
    assertValidationStatus(validate(template), "true");
  }

  @Test public void unknownConstraintFieldStillFailsValidation() {
    // additionalProperties:false is still enforced — only the named additive fields are allowed.
    JsonNode template = dataCiteTemplate();
    firstOntologyEntry(template).put("totallyBogusField", "x");
    assertValidationStatus(validate(template), "false");
  }

  @Test public void versionTripleMissingIdFailsValidation() {
    // The version object requires id (the authoritative content hash).
    JsonNode template = dataCiteTemplate();
    ObjectNode version = firstOntologyEntry(template).putObject("version");
    version.put("effectiveDate", "2026-07-01"); // no id
    assertValidationStatus(validate(template), "false");
  }

  /** Where DataCiteTemplate's first ontology constraint keeps its version id. */
  private static final String VERSION_ID_LOCATION = "/properties/language/_valueConstraints/ontologies/0/version/id";

  /** The meta-schema's rule for a version id: a character that Java's Character.isWhitespace does not count as whitespace. */
  private static final String VERSION_ID_PATTERN =
      "[^\\u0009-\\u000D\\u001C-\\u0020\\u1680\\u2000-\\u2006\\u2008-\\u200A\\u2028\\u2029\\u205F\\u3000]";

  // Four cases: a space, the five whitespace controls, two information separators, and two Unicode spaces.
  @ParameterizedTest
  @ValueSource(strings = {" ", "\t\n\u000B\f\r", "\u001C\u001F", "\u2003\u3000"})
  public void blankVersionIdFailsValidation(String id) {
    // A blank id identifies no release any more than an empty one does, and the model libraries refuse both.
    JsonNode template = dataCiteTemplate();
    firstOntologyEntry(template).putObject("version").put("id", id);

    ValidationReport report = validate(template);

    assertValidationStatus(report, "false");
    assertValidationMessage(report, VERSION_ID_LOCATION + ": does not match the regex pattern " + VERSION_ID_PATTERN);
  }

  // Two cases: whitespace around the hash, and the three no-break spaces Java does not count as whitespace.
  @ParameterizedTest
  @ValueSource(strings = {" 63ef56dff672 ", "\u00A0\u2007\u202F"})
  public void versionIdNotBlankInJavaPassesValidation(String id) {
    // The meta-schema refuses an id only where String.isBlank is true of it, so an id the libraries read is valid.
    JsonNode template = dataCiteTemplate();
    firstOntologyEntry(template).putObject("version").put("id", id);

    assertValidationStatus(validate(template), "true");
  }
}
