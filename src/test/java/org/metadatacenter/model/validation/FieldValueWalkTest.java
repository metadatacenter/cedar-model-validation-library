package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.metadatacenter.model.validation.report.CedarValidationReport;
import org.metadatacenter.model.validation.report.ErrorItem;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The walks that apply the field-value rules at every declared occurrence: through arrays and nested
 * elements, to defaults in a schema and to the answers in an instance.
 */
class FieldValueWalkTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static JsonNode json(String text) throws Exception {
    return MAPPER.readTree(text.replace('\'', '"'));
  }

  private static Set<String> codes(CedarValidationReport report) {
    return report.getFindings().stream().map(ValidationFinding::code).collect(Collectors.toSet());
  }

  private static Set<String> paths(CedarValidationReport report, String code) {
    return report.getFindings().stream().filter(f -> f.code().equals(code)).map(ValidationFinding::path)
        .collect(Collectors.toSet());
  }

  private static CedarValidationReport schema(JsonNode schema) {
    var report = CedarValidationReport.newEmptyReport();
    FieldValueValidation.validateSchema(schema, "", report);
    return report;
  }

  private static CedarValidationReport instance(JsonNode instance, JsonNode schema) {
    var report = CedarValidationReport.newInstanceReport();
    FieldValueValidation.validateInstance(instance, schema, "", report);
    return report;
  }

  private static final String TEXT = "{'_ui': {'inputType': 'textfield'}, '_valueConstraints': {}}";
  private static final String LINK = "{'_ui': {'inputType': 'link'}, '_valueConstraints': {},"
      + " 'properties': {'@id': {'type': 'string'}}}";
  private static final String TERM = "{'_ui': {'inputType': 'textfield'}, '_valueConstraints': {'requiredValue': true,"
      + " 'ontologies': [{'acronym': 'DOID'}]}, 'properties': {'@id': {'type': 'string', 'format': 'uri'}}}";

  @Test void aSchemaThatIsNotAnObjectHasNothingToCheck() {
    var report = CedarValidationReport.newEmptyReport();
    FieldValueValidation.validateSchema(null, "", report);
    FieldValueValidation.validateSchema(MAPPER.getNodeFactory().textNode("x"), "", report);
    assertTrue(report.getFindings().isEmpty());
  }

  @Test void aRepeatedChildWhoseMinimumExceedsItsMaximumIsRefused() throws Exception {
    var report = schema(json("{'type': 'array', 'minItems': 3, 'maxItems': 2, 'items': " + TEXT + "}"));
    assertEquals(Set.of("/maxItems"), paths(report, "templateConstraint"));
    assertTrue(schema(json("{'type': 'array', 'minItems': 2, 'maxItems': 2, 'items': " + TEXT + "}"))
        .getFindings().isEmpty());
    assertTrue(schema(json("{'type': 'array', 'minItems': 2, 'items': " + TEXT + "}")).getFindings().isEmpty());
  }

  @Test void aRepeatedChildsItemsAreCheckedAtTheirOwnPath() throws Exception {
    var report = schema(json("{'type': 'array', 'items': {'_ui': {'inputType': 'textfield'},"
        + " '_valueConstraints': {'minLength': -1}}}"));
    assertEquals(Set.of("/items/_valueConstraints/minLength"), paths(report, "templateConstraint"));
  }

  @Test void aControlledDefaultNeedsAnIriAndALabel() throws Exception {
    for (String value : List.of("'https://example.org/t'", "{'rdfs:label': 'Label'}", "{'termUri': '', 'rdfs:label': 'Label'}",
        "{'termUri': 'https://example.org/t'}", "{'termUri': 'https://example.org/t', 'rdfs:label': ''}",
        "{'termUri': 7, 'rdfs:label': 'Label'}")) {
      var field = (com.fasterxml.jackson.databind.node.ObjectNode) json(TERM);
      field.withObject("/_valueConstraints").set("defaultValue", json(value));
      assertEquals(Set.of("controlledStructure"), codes(schema(field)), value);
    }
  }

  @Test void aCompleteControlledDefaultIsCheckedAsAnIriAndMarkedUnverified() throws Exception {
    var field = (com.fasterxml.jackson.databind.node.ObjectNode) json(TERM);
    field.withObject("/_valueConstraints").set("defaultValue", json("{'termUri': 'not an iri', 'rdfs:label': 'Label'}"));
    var report = schema(field);
    assertEquals(Set.of("iriMalformed", "semanticNotChecked"), codes(report));
    assertEquals(Set.of("/_valueConstraints/defaultValue/termUri"), paths(report, "iriMalformed"));
    field.withObject("/_valueConstraints").set("defaultValue", json("{'termUri': 'https://example.org/t', 'rdfs:label': 'Label'}"));
    assertEquals(Set.of("semanticNotChecked"), codes(schema(field)));
  }

  @Test void aLiteralDefaultIsCheckedWhetherItIsTextOrANumber() throws Exception {
    var numeric = json("{'_ui': {'inputType': 'numeric'}, '_valueConstraints': {'numberType': 'xsd:int',"
        + " 'maxValue': 10, 'defaultValue': 11}}");
    assertEquals(Set.of("maxValue"), codes(schema(numeric)));
    var text = json("{'_ui': {'inputType': 'textfield'}, '_valueConstraints': {'maxLength': 1, 'defaultValue': 'ab'}}");
    assertEquals(Set.of("maxLength"), codes(schema(text)));
  }

  @Test void aDefaultOfNoValueOrOfAnotherKindIsNotChecked() throws Exception {
    for (String value : List.of("null", "''", "true", "{'@value': 'x'}"))
      assertTrue(schema(json("{'_ui': {'inputType': 'textfield'}, '_valueConstraints': {'minLength': 3,"
          + " 'defaultValue': " + value + "}}")).getFindings().isEmpty(), value);
  }

  @Test void anAuthorityDefaultIsMarkedUnverifiedOnlyWhenItHoldsSomething() throws Exception {
    var orcid = "{'_ui': {'inputType': 'ext-orcid'}, '_valueConstraints': {'defaultValue': %s}}";
    assertEquals(Set.of("semanticNotChecked"),
        codes(schema(json(orcid.formatted("'https://orcid.org/0000-0001-5109-3700'")))));
    assertTrue(schema(json(orcid.formatted("''"))).getFindings().isEmpty());
  }

  @Test void nestedChildrenAreReachedAndNamesAreEscapedInPointers() throws Exception {
    var element = json("{'properties': {'@context': {'type': 'object'}, 'a/b~c': {'_ui': {'inputType': 'textfield'},"
        + " '_valueConstraints': {'maxLength': 1, 'defaultValue': 'xy'}}}}");
    assertEquals(Set.of("/properties/a~1b~0c/_valueConstraints/defaultValue"), paths(schema(element), "maxLength"));
    var answer = json("{'a/b~c': {'@value': 'xy'}}");
    assertEquals(Set.of("/a~1b~0c/@value"), paths(instance(answer, element), "maxLength"));
  }

  @Test void anInstanceWithoutASchemaOrAValueHasNothingToCheck() throws Exception {
    var report = CedarValidationReport.newInstanceReport();
    FieldValueValidation.validateInstance(json("{}"), null, "", report);
    FieldValueValidation.validateInstance(null, json(TEXT), "", report);
    FieldValueValidation.validateInstance(json("'text'"), json(TEXT), "", report);
    assertTrue(report.getFindings().isEmpty());
  }

  @Test void repeatedAnswersAreCheckedOneByOneAndAnUnrepeatedOneIsLeftToTheSchema() throws Exception {
    var field = json("{'type': 'array', 'items': {'_ui': {'inputType': 'textfield'}, '_valueConstraints': {'maxLength': 1}}}");
    assertEquals(Set.of("/1/@value"), paths(instance(json("[{'@value': 'a'}, {'@value': 'ab'}]"), field), "maxLength"));
    assertTrue(instance(json("{'@value': 'ab'}"), field).getFindings().isEmpty());
  }

  @Test void aStaticFieldTakesNoAnswer() throws Exception {
    var field = json("{'_ui': {'inputType': 'section-break'}, '_valueConstraints': {'requiredValue': true}}");
    assertTrue(instance(json("{}"), field).getFindings().isEmpty());
  }

  @Test void aLiteralFieldNeedsAValueAndNoIri() throws Exception {
    assertEquals(Set.of("valueShape"), codes(instance(json("{}"), json(TEXT))));
    assertEquals(Set.of("valueShape"), codes(instance(json("{'@value': 'x', '@id': 'https://example.org/x'}"), json(TEXT))));
    assertTrue(instance(json("{'@value': 'x'}"), json(TEXT)).getFindings().isEmpty());
  }

  @Test void anIriFieldRefusesALiteralButNotAnEmptySlot() throws Exception {
    assertEquals(Set.of("valueShape"), codes(instance(json("{'@value': 'x'}"), json(LINK))));
    assertTrue(instance(json("{'@value': null}"), json(LINK)).getFindings().isEmpty());
    assertTrue(instance(json("{}"), json(LINK)).getFindings().isEmpty());
  }

  @Test void aStoredIriMustBeANonemptyString() throws Exception {
    assertEquals(Set.of("/@id"), paths(instance(json("{'@id': ''}"), json(LINK)), "iriMalformed"));
    assertEquals(Set.of("/@id"), paths(instance(json("{'@id': 7}"), json(LINK)), "iriMalformed"));
    assertEquals(Set.of("/@id"), paths(instance(json("{'@id': 'not an iri'}"), json(LINK)), "iriMalformed"));
  }

  @Test void anUnfilledRequiredAnswerIsIncompleteNotMalformed() throws Exception {
    var field = json("{'_ui': {'inputType': 'textfield'}, '_valueConstraints': {'requiredValue': true}}");
    for (String slot : List.of("{'@value': null}", "{'@value': ''}")) {
      var report = instance(json(slot), field);
      assertEquals(Set.of("required"), codes(report), slot);
      assertTrue(report.canSave(), slot);
      assertFalse(report.isValid(), slot);
    }
    assertEquals(Set.of("/@id"), paths(instance(json("{}"), json(TERM)), "required"));
  }

  @Test void aControlledLabelWithoutAnIriNamesNoTerm() throws Exception {
    assertEquals(Set.of("required", "controlledStructure"), codes(instance(json("{'rdfs:label': 'Flu'}"), json(TERM))));
    assertEquals(Set.of("required"), codes(instance(json("{'rdfs:label': ''}"), json(TERM))));
    assertEquals(Set.of("semanticNotChecked"),
        codes(instance(json("{'@id': 'https://example.org/flu', 'rdfs:label': 'Flu'}"), json(TERM))));
  }

  @Test void aTypedAnswerMustStateItsFieldsDatatypeInEitherSpelling() throws Exception {
    var numeric = json("{'_ui': {'inputType': 'numeric'}, '_valueConstraints': {'numberType': 'xsd:int'}}");
    assertTrue(instance(json("{'@value': '5', '@type': 'xsd:int'}"), numeric).getFindings().isEmpty());
    assertTrue(instance(json("{'@value': '5', '@type': 'http://www.w3.org/2001/XMLSchema#int'}"), numeric)
        .getFindings().isEmpty());
    assertEquals(Set.of("/@type"), paths(instance(json("{'@value': '5', '@type': 'xsd:long'}"), numeric), "valueType"));
    assertEquals(Set.of("/@type"), paths(instance(json("{'@value': '5'}"), numeric), "valueType"));
    assertTrue(instance(json("{'@value': null}"), numeric).getFindings().isEmpty());
    var temporal = json("{'_ui': {'inputType': 'temporal'}, '_valueConstraints': {'temporalType': 'xsd:date'}}");
    assertEquals(Set.of("/@type"), paths(instance(json("{'@value': '2024-02-29', '@type': 'xsd:dateTime'}"), temporal),
        "valueType"));
    // A numeric field declaring no datatype is the template's defect, and the answer is not held to one.
    var untyped = json("{'_ui': {'inputType': 'numeric'}, '_valueConstraints': {}}");
    assertEquals(Set.of("templateConstraint"), codes(instance(json("{'@value': '5'}"), untyped)));
  }

  @Test void theRegexWalkReachesRepeatedAndNestedChildren() throws Exception {
    var schema = json("{'properties': {'@context': {'type': 'object'}, 'Codes': {'type': 'array', 'items':"
        + " {'_ui': {'inputType': 'textfield'}, '_valueConstraints': {'regex': '[A-Z]+', 'defaultValue': 'abc'}}}}}");
    var report = CedarValidationReport.newEmptyReport();
    FieldRegexValidation.validateSchema(schema, "", report);
    assertEquals(Set.of("/properties/Codes/items/_valueConstraints/defaultValue"), paths(report, "regex"));
    report = CedarValidationReport.newInstanceReport();
    FieldRegexValidation.validateInstance(json("{'Codes': [{'@value': 'ABC'}, {'@value': 'abc'}, {'@value': ''},"
        + " {'@value': null}]}"), schema, "", report);
    assertEquals(Set.of("/Codes/1/@value"), paths(report, "regex"));
  }

  @Test void theRegexWalkSkipsAbsentShapesAndReportsAnInvalidExpressionAtEachAnswer() throws Exception {
    var field = json("{'_ui': {'inputType': 'textfield'}, '_valueConstraints': {'regex': '('}}");
    var report = CedarValidationReport.newInstanceReport();
    FieldRegexValidation.validateInstance(json("{'@value': 'x'}"), field, "/Answer", report);
    assertTrue(report.getErrors().contains(new ErrorItem("The template contains an invalid regular expression.",
        "/Answer/@value")), report.toString());
    var quiet = CedarValidationReport.newInstanceReport();
    FieldRegexValidation.validateInstance(json("'x'"), field, "", quiet);
    FieldRegexValidation.validateInstance(json("{'@value': 'x'}"), json("{'type': 'array', 'items': " + TEXT + "}"), "", quiet);
    FieldRegexValidation.validateInstance(null, field, "", quiet);
    FieldRegexValidation.validateSchema(null, "", quiet);
    assertTrue(quiet.getFindings().isEmpty() && quiet.getErrors().isEmpty(), quiet.toString());
    var schemaReport = CedarValidationReport.newEmptyReport();
    FieldRegexValidation.validateSchema(json("{'_ui': {'inputType': 'textfield'}, '_valueConstraints': {'regex': '(',"
        + " 'defaultValue': 'x'}}"), "", schemaReport);
    assertEquals(1, schemaReport.getErrors().size(), schemaReport.toString());
  }
}
