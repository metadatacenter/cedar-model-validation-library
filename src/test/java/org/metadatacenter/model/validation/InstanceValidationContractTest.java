package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.metadatacenter.model.validation.report.CedarValidationReport;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InstanceValidationContractTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  @Test void normativeConfigurationCorpus() throws Exception {
    try (var input = getClass().getResourceAsStream("/concordance/configuration-validation.json")) {
      for (var row : MAPPER.readTree(input).get("cases")) {
        var findings = FieldValuePolicy.validateConfiguration(row.get("field"), "");
        List<String> codes = new ArrayList<>(), categories = new ArrayList<>(), paths = new ArrayList<>();
        row.get("codes").forEach(code -> codes.add(code.asText()));
        row.get("categories").forEach(category -> categories.add(category.asText()));
        row.get("paths").forEach(path -> paths.add(path.asText()));
        assertEquals(codes, findings.stream().map(ValidationFinding::code).toList(), row.path("id").asText());
        assertEquals(categories, findings.stream().map(f -> f.category().value()).toList(), row.path("id").asText());
        assertEquals(paths, findings.stream().map(ValidationFinding::path).toList(), row.path("id").asText());
      }
    }
  }
  @Test void normativeLocalValueCorpus() throws Exception {
    try (var input = getClass().getResourceAsStream("/concordance/instance-validation.json")) {
      for (var row : MAPPER.readTree(input).get("cases")) {
        var findings = FieldValuePolicy.validate(row.path("value").asText(), row.get("field"), "/answer/@value",
            row.path("defaultValue").asBoolean());
        List<String> codes = new ArrayList<>(), categories = new ArrayList<>();
        row.get("codes").forEach(code -> codes.add(code.asText()));
        row.get("categories").forEach(category -> categories.add(category.asText()));
        assertEquals(codes, findings.stream().map(ValidationFinding::code).toList(), row.path("id").asText());
        assertEquals(categories, findings.stream().map(f -> f.category().value()).toList(), row.path("id").asText());
        var report = row.path("defaultValue").asBoolean() ? CedarValidationReport.newEmptyReport()
            : CedarValidationReport.newInstanceReport();
        findings.forEach(report::addFinding);
        assertEquals(row.path("defaultValue").asBoolean() ? findings.isEmpty()
            : findings.stream().noneMatch(f -> f.category() == ValidationFinding.Category.STRUCTURAL), report.canSave());
        assertEquals(findings.isEmpty(), report.isValid());
      }
    }
  }

  @Test void schemaDefaultsAndInstanceConstraintsUseDifferentSavePolicies() throws Exception {
    var field = resource("fields/numeric-field.json");
    field.withObject("/_valueConstraints").put("minValue", 0).put("maxValue", 10).put("defaultValue", "11");
    var validator = new CedarValidator();
    var invalidDefault = validator.validateTemplateField(field);
    assertFalse(invalidDefault.canSave(), invalidDefault.toString());
    assertTrue(invalidDefault.getFindings().stream().anyMatch(f -> f.code().equals("maxValue")
        && f.path().equals("/_valueConstraints/defaultValue")));
    field.withObject("/_valueConstraints").remove("defaultValue");
    var template = resource("templates/single-field-template.json");
    template.withObject("/properties").set("Study Name", field);
    var instance = resource("instances/single-field-instance.jsonld");
    var value = instance.withObject("/Study Name"); value.remove("@language");
    value.put("@type", "xsd:decimal").put("@value", "11");
    var report = validator.validateTemplateInstance(instance, template);
    assertTrue(report.canSave(), report.toString());
    assertFalse(report.isValid());
    assertTrue(report.getErrors().isEmpty());
    assertTrue(report.getFindings().stream().anyMatch(f -> f.code().equals("maxValue")));
    value.put("@value", "bad");
    assertFalse(validator.validateTemplateInstance(instance, template).canSave());
    value.remove("@value");
    report = validator.validateTemplateInstance(instance, template);
    assertFalse(report.canSave());
    assertTrue(report.getFindings().stream().anyMatch(f -> f.code().equals("missingProperty")));
    value.putNull("@value");
    field.withObject("/_valueConstraints").put("requiredValue", true);
    report = validator.validateTemplateInstance(instance, template);
    assertTrue(report.canSave(), report.toString());
    assertTrue(report.getFindings().stream().anyMatch(f -> f.code().equals("required")));
  }

  @Test void cardinalityQualityNeverDowngradesTypeOrMissingPropertyFailures() throws Exception {
    var template = resource("templates/multiple-field-items-template.json");
    var instance = resource("instances/multiple-field-items-instance.jsonld");
    template.withObject("/properties/Topic").put("maxItems", 1);
    var validator = new CedarValidator();
    var report = validator.validateTemplateInstance(instance, template);
    assertTrue(report.canSave(), report.toString());
    assertTrue(report.getFindings().stream().anyMatch(f -> f.code().equals("maxItems")));
    instance.put("Topic", "wrong container");
    assertFalse(validator.validateTemplateInstance(instance, template).canSave());
    instance.remove("Topic");
    assertFalse(validator.validateTemplateInstance(instance, template).canSave());
  }
  @Test void semanticSuccessDoesNotEraseAnUnavailableIndependentCheck() {
    var report = CedarValidationReport.newInstanceReport();
    report.addFinding(new ValidationFinding("regexEvaluation", ValidationFinding.Category.VERIFICATION,
        "/answer/@id", "Regex unavailable", ValidationFinding.VerificationStatus.UNAVAILABLE));
    report.addFinding(new ValidationFinding("semanticNotChecked", ValidationFinding.Category.VERIFICATION,
        "/answer/@id", "Membership not checked", ValidationFinding.VerificationStatus.NOT_CHECKED));
    report.resolveVerification(new ValidationFinding("controlledTermVerified", ValidationFinding.Category.VERIFICATION,
        "/answer/@id", "Term verified", ValidationFinding.VerificationStatus.PASSED));
    assertTrue(report.canSave());
    assertFalse(report.isValid());
    assertEquals(2, report.getFindings().size());
    assertEquals(1, report.getWarnings().size());
  }

  private static ObjectNode resource(String name) throws Exception {
    return (ObjectNode) MAPPER.readTree(TestResourcesUtils.getStringContent(name));
  }
}
