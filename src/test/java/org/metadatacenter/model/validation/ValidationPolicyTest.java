package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.metadatacenter.model.validation.report.CedarValidationReport;
import org.metadatacenter.model.validation.report.ErrorItem;

import static org.junit.jupiter.api.Assertions.*;
import static org.metadatacenter.model.validation.ValidationFinding.Category.*;
import static org.metadatacenter.model.validation.ValidationFinding.VerificationStatus.*;

class ValidationPolicyTest {
  @Test void semanticResponseConcordanceCorpus() throws Exception {
    try (var input = getClass().getResourceAsStream("/concordance/semantic-verification.json")) {
      for (var row : new ObjectMapper().readTree(input).get("cases")) {
        String id = row.path("id").asText();
        String status = row.path("status").asText(), code = row.path("code").asText();
        boolean accepted = ValidationPolicy.acceptsSemanticResult(row.path("controlled").asBoolean(), status, code);
        assertEquals(row.path("accepted").asBoolean(), accepted, id);
        if (!accepted) { status = "unavailable"; code = "semanticUnavailable"; }
        var outcome = switch (status) {
          case "passed" -> PASSED;
          case "failed" -> FAILED;
          case "notChecked" -> NOT_CHECKED;
          default -> UNAVAILABLE;
        };
        assertEquals(row.path("category").asText(), ValidationPolicy.category(code).value(), id);
        var finding = new ValidationFinding(code, ValidationPolicy.category(code), "/answer/@id", "Checked", outcome);
        var template = CedarValidationReport.newEmptyReport();
        var instance = CedarValidationReport.newInstanceReport();
        template.addFinding(finding);
        instance.addFinding(finding);
        assertEquals(row.path("templateCanSave").asBoolean(), template.canSave(), id);
        assertEquals(row.path("instanceCanSave").asBoolean(), instance.canSave(), id);
        assertEquals(row.path("isValid").asBoolean(), template.isValid(), id);
        assertEquals(row.path("isValid").asBoolean(), instance.isValid(), id);
        assertEquals(Boolean.toString(template.canSave()), template.getValidationStatus(), id);
        assertEquals(Boolean.toString(instance.canSave()), instance.getValidationStatus(), id);
      }
    }
  }

  @Test void semanticFailuresHaveConstraintCategoriesAndContextSpecificSaveOutcomes() {
    for (String code : new String[]{"controlledTermMembership", "authorityIdentifier", "authorityMembership"}) {
      assertEquals(CONSTRAINT, ValidationPolicy.category(code), code);
      var finding = new ValidationFinding(code, ValidationPolicy.category(code), "/answer/@id", "Not a member", FAILED);
      var template = CedarValidationReport.newEmptyReport();
      var instance = CedarValidationReport.newInstanceReport();
      template.addFinding(finding);
      instance.addFinding(finding);
      assertFalse(template.canSave(), code);
      assertFalse(template.isValid(), code);
      assertEquals("false", template.getValidationStatus(), code);
      assertEquals(1, template.getErrors().size(), code);
      assertTrue(instance.canSave(), code);
      assertFalse(instance.isValid(), code);
      assertEquals("true", instance.getValidationStatus(), code);
      assertTrue(instance.getErrors().isEmpty(), code);
      assertEquals(1, instance.getWarnings().size(), code);
    }
  }

  @Test void absentOrUnavailableSemanticChecksAreNeverReportedAsPassedOrBlocking() {
    for (var status : new ValidationFinding.VerificationStatus[]{NOT_CHECKED, UNAVAILABLE}) {
      String code = status == NOT_CHECKED ? "semanticNotChecked" : "semanticUnavailable";
      assertEquals(VERIFICATION, ValidationPolicy.category(code));
      for (var report : new CedarValidationReport[]{CedarValidationReport.newEmptyReport(), CedarValidationReport.newInstanceReport()}) {
        report.addFinding(new ValidationFinding(code, ValidationPolicy.category(code), "/answer/@id", "Unverified", status));
        assertTrue(report.canSave());
        assertFalse(report.isValid());
        assertEquals("true", report.getValidationStatus());
        assertTrue(report.getErrors().isEmpty());
        assertEquals(1, report.getWarnings().size());
      }
    }
  }

  @Test void explicitlyPassedFactsNeverBecomeQualityProblemsWhateverTheirCategory() {
    assertEquals(VERIFICATION, ValidationPolicy.category("controlledTermVerified"));
    assertEquals(VERIFICATION, ValidationPolicy.category("authorityVerified"));
    for (var category : ValidationFinding.Category.values()) {
      var finding = new ValidationFinding("checked", category, "/answer/@id", "Passed", PASSED);
      for (var context : ValidationPolicy.Context.values()) assertFalse(ValidationPolicy.blocksSave(finding, context));
      var report = CedarValidationReport.newEmptyReport();
      report.addFinding(finding, new ErrorItem("Passed", "/answer/@id"));
      assertTrue(report.canSave());
      assertTrue(report.isValid());
      assertEquals("true", report.getValidationStatus());
      assertTrue(report.getErrors().isEmpty());
      assertTrue(report.getWarnings().isEmpty());
      assertEquals(1, report.getFindings().size());
    }
  }

  @Test void refreshedSemanticSuccessClearsItsFailureWithoutClearingOtherChecks() {
    var report = CedarValidationReport.newEmptyReport();
    String path = "/answer/@id";
    report.addFinding(new ValidationFinding("regexEvaluation", VERIFICATION, path, "Regex unavailable", UNAVAILABLE));
    report.addFinding(new ValidationFinding("iriMalformed", STRUCTURAL, path, "Malformed IRI"));
    report.resolveVerification(new ValidationFinding("authorityMembership", CONSTRAINT, path, "Missing authority record", FAILED));
    assertEquals(2, report.getErrors().size());
    report.resolveVerification(new ValidationFinding("authorityVerified", VERIFICATION, path, "Record exists", PASSED));
    assertEquals(1, report.getErrors().size());
    assertEquals(1, report.getWarnings().size());
    assertEquals(3, report.getFindings().size());
    assertFalse(report.canSave());
    assertFalse(report.isValid());
    assertTrue(report.getFindings().stream().noneMatch(f -> f.code().equals("authorityMembership")));
  }

  @Test void missingSlotsAndUnknownDiagnosticsRemainStructural() {
    for (String code : new String[]{"missingProperty", "numberType", "temporalType", "unrecognizedDiagnostic"}) {
      assertEquals(STRUCTURAL, ValidationPolicy.category(code), code);
      var finding = new ValidationFinding(code, ValidationPolicy.category(code), "/answer", "Invalid structure");
      assertTrue(ValidationPolicy.blocksSave(finding, ValidationPolicy.Context.INSTANCE), code);
    }
    assertEquals(COMPLETENESS, ValidationPolicy.category("required"));
    assertEquals(COMPLETENESS, ValidationPolicy.category("minItems"));
  }
}
