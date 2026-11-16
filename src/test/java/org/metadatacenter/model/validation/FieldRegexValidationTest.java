package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.metadatacenter.model.validation.report.CedarValidationReport;
import org.metadatacenter.model.validation.report.ErrorItem;
import org.metadatacenter.model.validation.report.WarningItem;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FieldRegexValidationTest extends BaseValidationTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private final CedarValidator validator = new CedarValidator();

  private static ObjectNode resource(String name) throws Exception {
    return (ObjectNode) MAPPER.readTree(TestResourcesUtils.getStringContent(name));
  }

  private static void regex(JsonNode field, String expression) {
    ((ObjectNode) field).withObject("/_valueConstraints").put("regex", expression);
  }

  // Expected results from the browser's new RegExp(`^(?:${regex})$`).test(value).
  static Stream<Arguments> browserCases() {
    return Stream.of(
        Arguments.of("[A-Z]+", "ABC", true),
        Arguments.of("^[A-Z]+$", "lowercase", false),
        Arguments.of("[A-Z]+", "xABCx", false),
        Arguments.of("[A-Z]+", "ABC\n", false),
        Arguments.of("A|BC", "ABC", false),
        Arguments.of("A|BC", "BC", true),
        Arguments.of("\\s+", "\u00a0", true),
        Arguments.of("\\w+", "é", false),
        Arguments.of(".", "😀", false),
        Arguments.of("[^]", "\n", true),
        Arguments.of("(?=A)A", "A", true),
        Arguments.of("(A)\\1", "AA", true));
  }

  @ParameterizedTest @MethodSource("browserCases")
  void matchesBrowserSemantics(String expression, String value, boolean expected) throws Exception {
    ObjectNode template = resource("templates/single-field-template.json");
    ObjectNode instance = resource("instances/single-field-instance.jsonld");
    regex(template.at("/properties/Study Name"), expression);
    ((ObjectNode) instance.get("Study Name")).put("@value", value);
    JsonNode originalTemplate = template.deepCopy(), originalInstance = instance.deepCopy();
    var report = validator.validateTemplateInstance(instance, template);
    assertTrue(report.canSave(), report.toString());
    assertEquals(expected, report.isValid(), report.toString());
    if (!expected) assertTrue(report.getWarnings().contains(
        new WarningItem("Does not match " + expression + ".", "/Study Name/@value")), report.toString());
    assertEquals(originalTemplate, template);
    assertEquals(originalInstance, instance);
  }

  /**
   * The shared regex contract, run through the two calls production makes: the template's configuration
   * check and the check of one answer. The TypeScript library reads the same corpus.
   */
  @Test void normativeRegexCorpus() throws Exception {
    try (var input = getClass().getResourceAsStream("/concordance/regex-policy.json")) {
      for (var row : MAPPER.readTree(input).get("cases")) {
        String id = row.get("id").asText();
        String expected = row.get("expected").asText();
        var field = MAPPER.createObjectNode();
        field.putObject("_ui").put("inputType", "textfield");
        field.putObject("_valueConstraints").put("regex", row.get("pattern").asText());

        var configuration = FieldValuePolicy.validateConfiguration(field, "");
        if (expected.equals("invalid")) {
          assertEquals(List.of(new ValidationFinding("templateConstraint", ValidationFinding.Category.STRUCTURAL,
              "/_valueConstraints/regex", "The template contains an invalid regular expression.")), configuration, id);
        } else {
          assertEquals(List.of(), configuration, id);
        }
        if (row.get("value").isNull()) continue;

        var answer = FieldValuePolicy.validate(row.get("value").asText(), field, "/answer/@value", false);
        switch (expected) {
          case "match" -> assertEquals(List.of(), answer, id);
          case "mismatch" -> assertEquals(List.of(new ValidationFinding("regex", ValidationFinding.Category.CONSTRAINT,
              "/answer/@value", "Does not match " + row.get("pattern").asText() + ".")), answer, id);
          case "invalid" -> assertEquals(List.of(new ValidationFinding("templateConstraint",
              ValidationFinding.Category.STRUCTURAL, "/answer/@value",
              "The template contains an invalid regular expression.")), answer, id);
          default -> fail(id + ": an answer row expects " + expected);
        }
      }
    }
  }

  @Test void validatesAgainstTheCurrentTemplateWithoutCachingAnOldConstraint() throws Exception {
    ObjectNode template = resource("templates/single-field-template.json");
    ObjectNode instance = resource("instances/single-field-instance.jsonld");
    assertValidationStatus(validator.validateTemplateInstance(instance, template), "true");
    regex(template.at("/properties/Study Name"), "[A-Z]+");
    assertValidationStatus(validator.validateTemplate(template), "true");
    assertFalse(validator.validateTemplateInstance(instance, template).isValid());
    ((ObjectNode) instance.get("Study Name")).put("@value", "FIXED");
    assertValidationStatus(validator.validateTemplateInstance(instance, template), "true");
  }

  @Test void incompleteAnswersRemainSaveable() throws Exception {
    ObjectNode template = resource("templates/single-field-template.json");
    ObjectNode instance = resource("instances/single-field-instance.jsonld");
    regex(template.at("/properties/Study Name"), "[A-Z]+");
    ObjectNode answer = (ObjectNode) instance.get("Study Name");
    answer.put("@value", "");
    assertValidationStatus(validator.validateTemplateInstance(instance, template), "true");
    answer.putNull("@value");
    assertValidationStatus(validator.validateTemplateInstance(instance, template), "true");
    answer.remove("@value");
    // A malformed literal is still a structural error, not a regex error.
    var report = validator.validateTemplateInstance(instance, template);
    assertValidationStatus(report, "false");
    assertTrue(report.getErrors().stream().noneMatch(e -> e.getMessage().startsWith("Does not match")));
  }

  @Test void identifiesRepeatedFieldAndElementOccurrences() throws Exception {
    ObjectNode template = resource("templates/multiple-field-items-template.json");
    ObjectNode instance = resource("instances/multiple-field-items-instance.jsonld");
    regex(template.at("/properties/Topic/items"), "Topic [AC]");
    var report = validator.validateTemplateInstance(instance, template);
    assertEquals(1, report.getWarnings().size(), report.toString());
    assertEquals("/Topic/1/@value", report.getWarnings().iterator().next().getLocation());

    template = resource("templates/multiple-element-items-template.json");
    instance = resource("instances/multiple-element-items-instance.jsonld");
    regex(template.at("/properties/Participant/items/properties/First Name"), "[A-Z]+");
    ((ObjectNode) instance.at("/Participant/0/First Name")).put("@value", "JOHN");
    report = validator.validateTemplateInstance(instance, template);
    assertEquals(1, report.getFindings().stream().filter(f -> f.code().equals("regex")).count(), report.toString());
    assertTrue(report.getFindings().stream().anyMatch(f -> f.code().equals("regex")
        && f.path().equals("/Participant/1/First Name/@value")));
    var elementReport = validator.validateElementInstance(instance.at("/Participant/1"),
        template.at("/properties/Participant/items"));
    assertTrue(elementReport.getWarnings().stream().anyMatch(e -> e.getLocation().equals("/First Name/@value")));
  }

  @Test void escapesPathsAndIgnoresUndeclaredData() throws Exception {
    JsonNode schema = MAPPER.readTree("""
        {"properties":{"a/b~c":{"_ui":{"inputType":"textfield"},"_valueConstraints":{"regex":"[A-Z]+"}}}}
        """);
    JsonNode instance = MAPPER.readTree("""
        {"a/b~c":{"@value":"bad"},"unmodeled":{"_valueConstraints":{"regex":"["},"@value":"kept"}}
        """);
    var report = CedarValidationReport.newInstanceReport();
    FieldValueValidation.validateInstance(instance, schema, "", report);
    assertEquals(List.of(new ValidationFinding("regex", ValidationFinding.Category.CONSTRAINT, "/a~1b~0c/@value",
        "Does not match [A-Z]+.")), List.copyOf(report.getFindings()), report.toString());
  }

  @Test void invalidPatternsGiveActionableReportsInsteadOfCrashingOrDisablingValidation() throws Exception {
    ObjectNode template = resource("templates/single-field-template.json");
    ObjectNode instance = resource("instances/single-field-instance.jsonld");
    JsonNode field = template.at("/properties/Study Name");
    regex(field, "[");
    var report = validator.validateTemplate(template);
    assertTrue(report.getErrors().contains(new ErrorItem("The template contains an invalid regular expression.",
        "/properties/Study Name/_valueConstraints/regex")), report.toString());
    assertValidationStatus(validator.validateTemplateField(field), "false");
    report = validator.validateTemplateInstance(instance, template);
    assertTrue(report.getErrors().contains(new ErrorItem("The template contains an invalid regular expression.",
        "/Study Name/@value")), report.toString());
    regex(field, "");
    assertValidationStatus(validator.validateTemplateInstance(instance, template), "true");
  }

  @Test void boundsPathologicalBacktrackingAndRecoversForTheNextRequest() throws Exception {
    ObjectNode template = resource("templates/single-field-template.json");
    ObjectNode instance = resource("instances/single-field-instance.jsonld");
    regex(template.at("/properties/Study Name"), "(a+)+");
    ((ObjectNode) instance.get("Study Name")).put("@value", "a".repeat(40) + "!");
    var report = assertTimeoutPreemptively(java.time.Duration.ofSeconds(5),
        () -> validator.validateTemplateInstance(instance, template));
    assertTrue(report.canSave());
    assertFalse(report.isValid());
    assertTrue(report.getFindings().stream().anyMatch(f -> f.code().equals("regexEvaluation")
        && f.verificationStatus() == ValidationFinding.VerificationStatus.UNAVAILABLE));
    assertTrue(report.getWarnings().stream().anyMatch(e -> e.getMessage().contains("validation work limit")
        && e.getLocation().equals("/Study Name/@value")), report.toString());
    ((ObjectNode) instance.get("Study Name")).put("@value", "aaa");
    assertValidationStatus(validator.validateTemplateInstance(instance, template), "true");
  }
}
