package org.metadatacenter.model.validation.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Keeps the documented report in step with the one Jackson serializes.
 *
 * <p>The interface names the status {@code validationStatus} and this implementation renames it to
 * {@code validates}, so a schema derived from the interface documents a key no client ever sees.
 * Two services answer with this report, and each described it separately until it was described
 * here once.
 */
class ValidationReportSchemaTest {

  @Test
  void theDocumentedKeysAreTheKeysJacksonWrites() {
    JsonNode serialized = new ObjectMapper().valueToTree(CedarValidationReport.newEmptyReport());

    Set<String> written = new LinkedHashSet<>();
    serialized.fieldNames().forEachRemaining(written::add);
    assertEquals(Set.of("validates", "warnings", "errors"), written);

    for (String key : written) {
      assertTrue(documentedNames().contains(key), key + " is written but not documented");
    }
  }

  @Test
  void theDocumentedStatusesAreTheOnesTheReportProduces() throws Exception {
    Method status = CedarValidationReport.class.getMethod("getValidationStatus");
    Set<String> documented = new LinkedHashSet<>(
        Arrays.asList(status.getAnnotation(Schema.class).allowableValues()));

    assertEquals(Set.of(CedarValidationReport.IS_VALID, CedarValidationReport.IS_INVALID), documented);
    assertEquals(CedarValidationReport.IS_VALID, CedarValidationReport.newEmptyReport().getValidationStatus());
  }

  @Test
  void theSchemaIsNamedTheSameThingInEveryServiceThatPublishesIt() {
    assertEquals("ValidationReport", CedarValidationReport.class.getAnnotation(Schema.class).name());
  }

  private static Set<String> documentedNames() {
    Set<String> names = new LinkedHashSet<>();
    for (Method method : CedarValidationReport.class.getMethods()) {
      Schema schema = method.getAnnotation(Schema.class);
      if (schema != null && !schema.name().isEmpty()) {
        names.add(schema.name());
      }
    }
    return names;
  }
}
