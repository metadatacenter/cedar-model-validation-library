package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.metadatacenter.model.validation.report.CedarValidationReport;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class NumericRangeValidationTest extends BaseValidationTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private final CedarValidator validator = new CedarValidator();

  private static ObjectNode resource(String name) throws Exception {
    return (ObjectNode) MAPPER.readTree(TestResourcesUtils.getStringContent(name));
  }

  @Test void tighteningBoundsReportsQualityWithoutChangingEitherDocument() throws Exception {
    ObjectNode template = resource("templates/single-field-template.json");
    ObjectNode field = resource("fields/numeric-field.json");
    ((ObjectNode) template.get("properties")).set("Study Name", field);
    ObjectNode instance = resource("instances/single-field-instance.jsonld");
    ObjectNode answer = (ObjectNode) instance.get("Study Name");
    answer.remove("@language");
    answer.put("@value", "10").put("@type", "xsd:decimal");
    assertValidationStatus(validator.validateTemplateInstance(instance, template), "true");
    field.withObject("/_valueConstraints").put("minValue", 20).put("maxValue", 30);
    assertValidationStatus(validator.validateTemplate(template), "true");
    JsonNode beforeTemplate = template.deepCopy(), beforeInstance = instance.deepCopy();
    var report = validator.validateTemplateInstance(instance, template);
    assertTrue(report.canSave());
    assertFalse(report.isValid());
    assertTrue(report.getWarnings().stream().anyMatch(e -> e.getLocation().equals("/Study Name/@value")
        && e.getMessage().equals("Below the minimum 20.")), report.toString());
    assertEquals(beforeTemplate, template);
    assertEquals(beforeInstance, instance);
    answer.put("@value", "30");
    assertValidationStatus(validator.validateTemplateInstance(instance, template), "true");
    answer.put("@value", "30.01");
    assertFalse(validator.validateTemplateInstance(instance, template).isValid());
  }

  static Stream<Arguments> ranges() {
    return Stream.of(
        Arguments.of("0", "10", "0", true), Arguments.of("0", "10", "10", true),
        Arguments.of("0", "10", "-0.001", false), Arguments.of("0", "10", "10.001", false),
        Arguments.of("-10", "-1", "-5.5", true), Arguments.of("-10", "-1", "0", false),
        Arguments.of("0.1", "0.3", "0.30000000000000001", false),
        Arguments.of("0.1", "0.3", "+.30", true),
        Arguments.of("100", "100", "1e2", false),
        Arguments.of("9007199254740993", null, "9007199254740992", false),
        Arguments.of(null, "9007199254740992", "9007199254740993", false),
        Arguments.of(null, "0", "-1e1000", false), Arguments.of("0", null, "1e1000", false),
        Arguments.of("0", "10", "", true), Arguments.of("0", "10", "NaN", false),
        Arguments.of("0", "10", "Infinity", false), Arguments.of("0", "10", "bad", false),
        Arguments.of("0", "10", " ", false), Arguments.of("0", "10", "0x1", false));
  }

  @ParameterizedTest @MethodSource("ranges")
  void comparesInclusiveBoundsWithoutCoercionOrPrecisionLoss(String min, String max, String value, boolean valid)
      throws Exception {
    ObjectNode schema = resource("fields/numeric-field.json");
    ObjectNode constraints = schema.withObject("/_valueConstraints");
    if (min != null) constraints.put("minValue", new java.math.BigDecimal(min));
    if (max != null) constraints.put("maxValue", new java.math.BigDecimal(max));
    ObjectNode instance = MAPPER.createObjectNode().put("@value", value).put("@type", "xsd:decimal");
    var report = CedarValidationReport.newEmptyReport();
    FieldValueValidation.validateInstance(instance, schema, "", report);
    assertEquals(valid, report.getErrors().isEmpty(), report.toString());
    assertTrue(report.getErrors().stream().allMatch(e -> e.getLocation().equals("/@value")));
  }

  @Test void incompleteAnswersRemainSaveableButMalformedLiteralShapesStillFail() throws Exception {
    ObjectNode template = resource("templates/single-field-template.json");
    ObjectNode field = resource("fields/numeric-field-minmax-value.json");
    field.withObject("/_valueConstraints").put("requiredValue", true);
    ((ObjectNode) template.get("properties")).set("Study Name", field);
    ObjectNode instance = resource("instances/single-field-instance.jsonld");
    ObjectNode answer = (ObjectNode) instance.get("Study Name");
    answer.remove("@language");
    answer.put("@type", "xsd:decimal").putNull("@value");
    assertValidationStatus(validator.validateTemplateInstance(instance, template), "true");
    answer.put("@value", "");
    assertValidationStatus(validator.validateTemplateInstance(instance, template), "true");
    answer.remove("@value");
    assertValidationStatus(validator.validateTemplateInstance(instance, template), "false");
    answer.put("@value", 10);
    assertValidationStatus(validator.validateTemplateInstance(instance, template), "false");
  }

  @Test void reportsRepeatedFieldsAndNestedRepeatedElementsAtTheirExactLocations() throws Exception {
    ObjectNode template = resource("templates/multiple-field-items-template.json");
    ObjectNode instance = resource("instances/multiple-field-items-instance.jsonld");
    ((ObjectNode) template.at("/properties/Topic")).set("items", resource("fields/numeric-field-minmax-value.json"));
    for (int i = 0; i < instance.get("Topic").size(); i++) {
      ObjectNode answer = (ObjectNode) instance.get("Topic").get(i);
      answer.remove("@language");
      answer.put("@type", "xsd:decimal").put("@value", i == 1 ? "100" : "99");
    }
    var report = validator.validateTemplateInstance(instance, template);
    assertEquals(1, report.getWarnings().size(), report.toString());
    assertEquals("/Topic/1/@value", report.getWarnings().iterator().next().getLocation());

    template = resource("templates/multiple-element-items-template.json");
    instance = resource("instances/multiple-element-items-instance.jsonld");
    ((ObjectNode) template.at("/properties/Participant/items/properties"))
        .set("First Name", resource("fields/numeric-field-minmax-value.json"));
    for (int i = 0; i < instance.get("Participant").size(); i++) {
      ObjectNode answer = (ObjectNode) instance.at("/Participant/" + i + "/First Name");
      answer.remove("@language");
      answer.put("@type", "xsd:decimal").put("@value", i == 1 ? "0" : "1");
    }
    report = validator.validateTemplateInstance(instance, template);
    assertEquals(1, report.getFindings().stream().filter(f -> f.code().equals("minValue")).count(), report.toString());
    assertTrue(report.getFindings().stream().anyMatch(f -> f.code().equals("minValue")
        && f.path().equals("/Participant/1/First Name/@value")));
    var element = validator.validateElementInstance(instance.at("/Participant/1"), template.at("/properties/Participant/items"));
    assertTrue(element.getWarnings().stream().anyMatch(e -> e.getLocation().equals("/First Name/@value")));
  }

  @Test void escapesPathsAndIgnoresOtherKindsAndUndeclaredContent() throws Exception {
    JsonNode schema = MAPPER.readTree("""
        {"properties":{"a/b~c":{"_ui":{"inputType":"numeric"},"_valueConstraints":{"numberType":"xsd:decimal","maxValue":1}},
        "text":{"_ui":{"inputType":"textfield"},"_valueConstraints":{"maxValue":1}}}}
        """);
    JsonNode instance = MAPPER.readTree("""
        {"a/b~c":{"@type":"xsd:decimal","@value":"2"},"text":{"@value":"hello"},"other":{"@value":"100"}}
        """);
    var report = CedarValidationReport.newEmptyReport();
    FieldValueValidation.validateInstance(instance, schema, "", report);
    assertEquals(1, report.getErrors().size());
    assertEquals("/a~1b~0c/@value", report.getErrors().iterator().next().getLocation());
  }

  @Test void malformedStoredRangesProduceFindingsRatherThanDisablingValidation() throws Exception {
    for (String constraints : new String[]{"{\"minValue\":2,\"maxValue\":1}", "{\"maxValue\":\"oops\"}", "{\"minValue\":null}"}) {
      ObjectNode schema = resource("fields/numeric-field.json");
      schema.set("_valueConstraints", MAPPER.readTree(constraints));
      var report = CedarValidationReport.newEmptyReport();
      FieldValueValidation.validateInstance(MAPPER.readTree("{\"@value\":\"1\"}"), schema, "", report);
      assertTrue(report.getFindings().stream().anyMatch(f -> f.code().equals("templateConstraint")), report.toString());
    }
  }
}
