package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import org.metadatacenter.model.validation.report.CedarValidationReport;
import org.metadatacenter.model.validation.report.ErrorItem;

import java.math.BigDecimal;

/** CEDAR numeric bounds constrain string-valued JSON-LD literals, not JSON Schema numbers. */
final class NumericRangeValidation {
  private NumericRangeValidation() {}

  static void validateInstance(JsonNode instance, JsonNode schema, String path, CedarValidationReport report) {
    if (schema == null || !schema.isObject() || instance == null) return;
    if (schema.path("items").isObject()) {
      if (instance.isArray()) {
        for (int i = 0; i < instance.size(); i++)
          validateInstance(instance.get(i), schema.get("items"), path + "/" + i, report);
      }
      return; // JSON Schema owns malformed literal and container shapes.
    }
    if (!instance.isObject()) return;
    if (schema.path("_ui").path("inputType").asText().equals("numeric")) {
      JsonNode constraints = schema.path("_valueConstraints");
      if (constraints.has("minValue") || constraints.has("maxValue")) {
        validateValue(instance.path("@value"), constraints, path + "/@value", report);
      }
    }
    schema.path("properties").fields().forEachRemaining(child -> {
      JsonNode field = child.getValue();
      if (field.path("_ui").isObject() || field.path("items").path("_ui").isObject())
        validateInstance(instance.get(child.getKey()), field,
            path + "/" + child.getKey().replace("~", "~0").replace("/", "~1"), report);
    });
  }

  private static void validateValue(JsonNode value, JsonNode constraints, String path, CedarValidationReport report) {
    BigDecimal min, max;
    try {
      min = bound(constraints, "minValue");
      max = bound(constraints, "maxValue");
      if (min != null && max != null && min.compareTo(max) > 0) throw new NumberFormatException();
    } catch (NumberFormatException invalidConstraint) {
      report.addError(new ErrorItem("The template contains an invalid numeric range.", path));
      return;
    }
    // Missing/null/empty answers follow the existing incomplete-metadata policy.
    if (!value.isTextual() || value.textValue().isEmpty()) return;
    BigDecimal number;
    try {
      // Compare exact decimal values: converting through double loses integer and boundary precision.
      number = new BigDecimal(value.textValue());
    } catch (NumberFormatException invalidNumber) {
      report.addError(new ErrorItem("Not a valid number.", path));
      return;
    }
    if (min != null && number.compareTo(min) < 0)
      report.addError(new ErrorItem("Below the minimum " + min + ".", path));
    if (max != null && number.compareTo(max) > 0)
      report.addError(new ErrorItem("Above the maximum " + max + ".", path));
  }

  private static BigDecimal bound(JsonNode constraints, String key) {
    JsonNode bound = constraints.get(key);
    if (bound == null) return null;
    if (!bound.isNumber()) throw new NumberFormatException();
    return bound.decimalValue();
  }
}
