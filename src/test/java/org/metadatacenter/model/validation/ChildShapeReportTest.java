package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.metadatacenter.model.validation.report.ErrorItem;
import org.metadatacenter.model.validation.report.ValidationReport;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A defective child of a template or element is reported on its own terms. The meta-schema lets a
 * child take several shapes, and a child matching none of them used to be reported as every shape's
 * failure: around a hundred errors for one wrong value, most of them about properties the child was
 * never meant to have, at places it does not have.
 */
class ChildShapeReportTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private final CedarValidator validator = new CedarValidator();

  private static ObjectNode resource(String name) throws Exception {
    return (ObjectNode) MAPPER.readTree(TestResourcesUtils.getStringContent(name));
  }

  private static ObjectNode at(JsonNode root, String pointer) {
    return (ObjectNode) root.at(pointer);
  }

  private static List<ErrorItem> errors(ValidationReport report) {
    return List.copyOf(report.getErrors());
  }

  private static void assertOneError(ValidationReport report, String location, String... phrases) {
    List<ErrorItem> errors = errors(report);
    assertEquals(1, errors.size(), errors.toString());
    assertEquals(location, errors.get(0).getLocation(), errors.toString());
    for (String phrase : phrases) {
      assertTrue(errors.get(0).getMessage().contains(phrase), errors.get(0).getMessage());
    }
    assertFalse(report.canSave());
  }

  private static boolean hasFinding(ValidationReport report, String code) {
    return ((org.metadatacenter.model.validation.report.CedarValidationReport) report).getFindings().stream()
        .anyMatch(finding -> finding.code().equals(code));
  }

  @Test void aChildWithoutAnInputTypeIsOneErrorSayingSo() throws Exception {
    ObjectNode template = resource("templates/single-field-template.json");
    at(template, "/properties/Study Name").putObject("_ui");
    ValidationReport report = validator.validateTemplate(template);
    assertOneError(report, "/properties/Study Name/_ui", "'Study Name' is not a field or element", "no inputType");
    assertTrue(hasFinding(report, "childShape"));
  }

  @Test void aChildWithoutAUiIsOneErrorSayingSo() throws Exception {
    ObjectNode template = resource("templates/single-field-template.json");
    at(template, "/properties/Study Name").remove("_ui");
    assertOneError(validator.validateTemplate(template), "/properties/Study Name", "it has no _ui");
  }

  @Test void aChildOfAnUnknownInputTypeIsOneErrorNamingIt() throws Exception {
    ObjectNode template = resource("templates/single-field-template.json");
    at(template, "/properties/Study Name/_ui").put("inputType", "bogus");
    assertOneError(validator.validateTemplate(template), "/properties/Study Name/_ui/inputType", "'bogus'",
        "is not an input type");
  }

  @Test void aChildWithoutATypeOrOfAnUnknownTypeIsOneErrorSayingSo() throws Exception {
    ObjectNode template = resource("templates/single-field-template.json");
    at(template, "/properties/Study Name").remove("@type");
    assertOneError(validator.validateTemplate(template), "/properties/Study Name", "it has no @type");
    at(template, "/properties/Study Name").put("@type", "https://example.org/Thing");
    assertOneError(validator.validateTemplate(template), "/properties/Study Name/@type",
        "is not a field or element type");
  }

  @Test void aFieldWithOneWrongValueIsThatOneError() throws Exception {
    ObjectNode template = resource("templates/single-field-template.json");
    at(template, "/properties/Study Name/_valueConstraints").put("requiredValue", "yes");
    assertOneError(validator.validateTemplate(template), "/properties/Study Name/_valueConstraints/requiredValue",
        "boolean expected");
  }

  @Test void aFieldTwoElementsDeepIsReportedWhereItIs() throws Exception {
    ObjectNode template = resource("templates/nested-element-template.json");
    String field = "/properties/Trade Good/properties/Quantity/properties/Value";
    at(template, field + "/_valueConstraints").put("requiredValue", "yes");
    assertOneError(validator.validateTemplate(template), field + "/_valueConstraints/requiredValue", "boolean expected");
    at(template, field + "/_valueConstraints").put("requiredValue", true);
    at(template, field).putObject("_ui");
    assertOneError(validator.validateTemplate(template), field + "/_ui", "'Value' is not a field or element");
  }

  @Test void aListsItemIsReportedWhereItIs() throws Exception {
    ObjectNode template = resource("templates/multiple-field-items-template.json");
    at(template, "/properties/Topic/items/_valueConstraints").put("requiredValue", "yes");
    assertOneError(validator.validateTemplate(template), "/properties/Topic/items/_valueConstraints/requiredValue",
        "boolean expected");
    at(template, "/properties/Topic/items/_valueConstraints").put("requiredValue", true);
    at(template, "/properties/Topic").put("minItems", "0");
    assertOneError(validator.validateTemplate(template), "/properties/Topic/minItems", "integer expected");
  }

  @Test void aListWithoutItsItemIsOneErrorSayingSo() throws Exception {
    ObjectNode template = resource("templates/multiple-field-items-template.json");
    at(template, "/properties/Topic").remove("items");
    assertOneError(validator.validateTemplate(template), "/properties/Topic", "'Topic' is not a field or element",
        "a list without an items object");
  }

  @Test void aStaticFieldInAListIsOneErrorSayingItCanNotBeRepeated() throws Exception {
    ObjectNode template = resource("templates/static-field-template.json");
    JsonNode staticField = template.at("/properties/About Study Form").deepCopy();
    ObjectNode list = at(template, "/properties").putObject("About Study Form");
    list.put("type", "array").put("minItems", 0).set("items", staticField);
    ValidationReport report = validator.validateTemplate(template);
    assertTrue(errors(report).stream().anyMatch(error -> error.getLocation().equals("/properties/About Study Form/items/@type")
        && error.getMessage().contains("a static field can not be repeated")), errors(report).toString());
    assertTrue(errors(report).size() <= 2, errors(report).toString());
  }

  @Test void anElementsOwnChildIsReportedTheSameWay() throws Exception {
    ObjectNode element = resource("elements/nested-element.json");
    at(element, "/properties/Product Name").putObject("_ui");
    assertOneError(validator.validateTemplateElement(element), "/properties/Product Name/_ui",
        "'Product Name' is not a field or element");
  }

  @Test void aSoundTemplateStillValidates() throws Exception {
    for (String name : List.of("templates/single-field-template.json", "templates/nested-element-template.json",
        "templates/multiple-field-items-template.json", "templates/static-field-template.json")) {
      assertTrue(validator.validateTemplate(resource(name)).getErrors().isEmpty(), name);
    }
  }
}
