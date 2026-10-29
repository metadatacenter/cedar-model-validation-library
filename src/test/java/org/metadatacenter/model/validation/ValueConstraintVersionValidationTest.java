package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

  @Test public void linksValidateInsideEveryCoreConstraintKind() throws Exception {
    String[] kinds = {"ontologies", "branches", "classes", "valueSets"};
    String[] entries = {
        "{\"uri\":\"https://example.org/source\",\"acronym\":\"X\",\"name\":\"Source\"}",
        "{\"uri\":\"https://example.org/root\",\"source\":\"Source\",\"acronym\":\"X\",\"name\":\"Root\",\"maxDepth\":0}",
        "{\"uri\":\"https://example.org/term\",\"source\":\"X\",\"type\":\"OntologyClass\",\"label\":\"Term\",\"prefLabel\":\"Term\"}",
        "{\"uri\":\"https://example.org/set\",\"vsCollection\":\"X\",\"name\":\"Set\"}"};
    for (int i = 0; i < kinds.length; i++) {
      JsonNode template = dataCiteTemplate();
      ObjectNode constraints = firstConstraints(template);
      constraints.remove(java.util.List.of(kinds));
      ObjectNode entry = (ObjectNode) mapper.readTree(entries[i]);
      constraints.putArray(kinds[i]).add(entry);
      entry.putObject("links").put("landingPage", "https://example.org/page")
          .put("icon", "https://example.org/icon.svg");
      assertValidationStatus(validate(template), "true");
      ((ObjectNode) entry.get("links")).put("versionScope", "current");
      assertValidationStatus(validate(template), "false");
      entry.putObject("links").put("landingPage", "javascript:alert(1)");
      assertValidationStatus(validate(template), "false");
    }
  }

  private ObjectNode firstConstraints(JsonNode node) {
    if (node.isObject() && node.has("ontologies") && !node.get("ontologies").isEmpty())
      return (ObjectNode) node;
    for (JsonNode child : node) {
      ObjectNode found = firstConstraints(child);
      if (found != null) return found;
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
}
