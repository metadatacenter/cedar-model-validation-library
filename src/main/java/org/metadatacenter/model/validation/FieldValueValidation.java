package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import org.metadatacenter.model.validation.report.CedarValidationReport;
import java.util.Set;
import static org.metadatacenter.model.validation.ValidationFinding.Category.*;

/** Applies local rules at every declared occurrence, including defaults in imported schemas. */
final class FieldValueValidation {
  private FieldValueValidation() {}
  private static final Set<String> STATIC = Set.of("page-break", "section-break", "richtext", "image", "youtube", "attribute-value");

  static void validateSchema(JsonNode schema, String path, CedarValidationReport report) {
    if (schema == null || !schema.isObject()) return;
    if (schema.path("items").isObject()) {
      if (schema.path("minItems").isNumber() && schema.path("maxItems").isNumber()
          && schema.get("minItems").decimalValue().compareTo(schema.get("maxItems").decimalValue()) > 0)
        report.addFinding(new ValidationFinding("templateConstraint", STRUCTURAL, path + "/maxItems",
            "The minimum number of occurrences exceeds the maximum."));
      validateSchema(schema.get("items"), path + "/items", report);
      return;
    }
    FieldValuePolicy.validateConfiguration(schema, path).forEach(report::addFinding);
    JsonNode c = schema.path("_valueConstraints");
    if (c.has("defaultValue") && !c.get("defaultValue").isNull()) {
      JsonNode value = c.get("defaultValue");
      String valuePath = path + "/_valueConstraints/defaultValue";
      if (FieldValuePolicy.isControlled(schema)) {
        if (!value.isObject() || !value.path("termUri").isTextual() || value.path("termUri").asText().isEmpty()
            || !value.path("rdfs:label").isTextual() || value.path("rdfs:label").asText().isEmpty())
          report.addFinding(new ValidationFinding("controlledStructure", STRUCTURAL, valuePath,
              "A controlled-term default requires an IRI and a label."));
        else {
          FieldValuePolicy.validate(value.get("termUri").textValue(), schema, valuePath + "/termUri", true)
              .forEach(report::addFinding);
          notChecked(schema, valuePath + "/termUri", report);
        }
      } else if (value.isTextual() || value.isNumber()) {
        FieldValuePolicy.validate(value.asText(), schema, valuePath, true).forEach(report::addFinding);
        if (!value.asText().isEmpty()) notChecked(schema, valuePath, report);
      }
    }
    schema.path("properties").fields().forEachRemaining(child -> {
      if (isChild(child.getValue()))
        validateSchema(child.getValue(), path + "/properties/" + escape(child.getKey()), report);
    });
  }

  static void validateInstance(JsonNode instance, JsonNode schema, String path, CedarValidationReport report) {
    if (schema == null || !schema.isObject() || instance == null) return;
    if (schema.path("items").isObject()) {
      if (instance.isArray()) for (int i = 0; i < instance.size(); i++)
        validateInstance(instance.get(i), schema.get("items"), path + "/" + i, report);
      return;
    }
    if (!instance.isObject()) return;
    String input = schema.path("_ui").path("inputType").asText();
    if (!input.isEmpty() && !STATIC.contains(input)) {
      FieldValuePolicy.validateConfiguration(schema, path).forEach(report::addFinding);
      boolean iri = FieldValuePolicy.isIriField(schema);
      String key = iri ? "@id" : "@value";
      if (!iri && (!instance.has("@value") || instance.has("@id")))
        report.addFinding(new ValidationFinding("valueShape", STRUCTURAL, path, "This field requires a literal value."));
      if (iri && instance.has("@value") && !instance.get("@value").isNull())
        report.addFinding(new ValidationFinding("valueShape", STRUCTURAL, path, "This field requires an IRI value."));
      if (instance.has("@id") && (!instance.get("@id").isTextual() || instance.get("@id").asText().isEmpty()))
        report.addFinding(new ValidationFinding("iriMalformed", STRUCTURAL, path + "/@id", "The stored IRI is empty or malformed."));
      JsonNode value = instance.path(key);
      boolean empty = value.isMissingNode() || value.isNull() || value.isTextual() && value.textValue().isEmpty();
      if (empty && schema.path("_valueConstraints").path("requiredValue").asBoolean())
        report.addFinding(new ValidationFinding("required", COMPLETENESS, path + "/" + key,
            "A value is required."));
      if (FieldValuePolicy.isControlled(schema)) {
        JsonNode label = instance.path("rdfs:label");
        boolean labelPresent = label.isTextual() && !label.textValue().isEmpty();
        if (empty && labelPresent)
          report.addFinding(new ValidationFinding("controlledStructure", STRUCTURAL, path,
              "A controlled-term label requires an IRI."));
      }
      if (value.isTextual() && !value.textValue().isEmpty()) {
        FieldValuePolicy.validate(value.textValue(), schema, path + "/" + key, false).forEach(report::addFinding);
        notChecked(schema, path + "/" + key, report);
      }
      if ((input.equals("numeric") || input.equals("temporal")) && !empty) {
        String expected = schema.path("_valueConstraints").path(input.equals("numeric") ? "numberType" : "temporalType").asText();
        String actual = instance.path("@type").asText();
        if (!expected.isEmpty() && !actual.equals(expected)
            && !actual.equals(expected.replace("xsd:", "http://www.w3.org/2001/XMLSchema#")))
          report.addFinding(new ValidationFinding("valueType", STRUCTURAL, path + "/@type",
              "The literal datatype does not match " + expected + "."));
      }
    }
    schema.path("properties").fields().forEachRemaining(child -> {
      if (isChild(child.getValue()))
        validateInstance(instance.get(child.getKey()), child.getValue(), path + "/" + escape(child.getKey()), report);
    });
  }

  private static void notChecked(JsonNode field, String path, CedarValidationReport report) {
    if (FieldValuePolicy.isControlled(field) || field.path("_ui").path("inputType").asText().startsWith("ext-"))
      report.addFinding(new ValidationFinding("semanticNotChecked", VERIFICATION, path,
          "Semantic membership has not been checked.", ValidationFinding.VerificationStatus.NOT_CHECKED));
  }

  static boolean isChild(JsonNode schema) {
    return schema.path("_ui").isObject() || schema.path("items").path("_ui").isObject();
  }
  static String escape(String key) { return key.replace("~", "~0").replace("/", "~1"); }
}
