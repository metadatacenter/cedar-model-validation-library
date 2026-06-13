package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.metadatacenter.model.validation.report.ValidationReport;

/**
 * Tests for {@link ModelValidator#validateElementInstance}. A CEDAR element artifact is
 * itself the JSON Schema its instances validate against in situ; the element-instance
 * fixture here is the nested element object extracted from a known-valid template
 * instance, validated against the matching element schema embedded in the same template.
 */
public class ElementInstanceValidationTest extends BaseValidationTest {
  private static ObjectMapper jsonObjectMapper = new ObjectMapper();
  private static String tradeGoodElement;
  private static JsonNode tradeGoodInstance;
  private ModelValidator modelValidator;

  @BeforeClass
  public static void loadExamples() throws Exception {
    // The element schema and the element-instance object must be a real pair — the schema
    // pins each child's property IRI as an enum inside @context, so an element authored
    // separately would (correctly) fail. Extract both sides from the same template fixtures:
    // the embedded 'Trade Good' element schema, and the nested 'Trade Good' instance object.
    String templateString = TestResourcesUtils.getStringContent("templates/nested-element-template.json");
    tradeGoodElement = jsonObjectMapper.readTree(templateString).get("properties").get("Trade Good").toString();
    String instanceString = TestResourcesUtils.getStringContent("instances/nested-element-instance.jsonld");
    tradeGoodInstance = jsonObjectMapper.readTree(instanceString).get("Trade Good");
  }

  @Before
  public void createNewValidator() {
    modelValidator = new CedarValidator();
  }

  private ValidationReport runValidation(JsonNode elementInstance, String elementSchema) {
    try {
      JsonNode schemaNode = jsonObjectMapper.readTree(elementSchema);
      return modelValidator.validateElementInstance(elementInstance, schemaNode);
    } catch (Exception e) {
      throw new RuntimeException("Programming error", e);
    }
  }

  @Test
  public void shouldPassNestedElementObjectAgainstItsElement() {
    ValidationReport validationReport = runValidation(tradeGoodInstance, tradeGoodElement);
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldFailOnUndeclaredChild() {
    ObjectNode mutated = tradeGoodInstance.deepCopy();
    mutated.set("Bogus Child", jsonObjectMapper.createObjectNode().put("@value", "x"));
    ValidationReport validationReport = runValidation(mutated, tradeGoodElement);
    assertValidationStatus(validationReport, "false");
  }

  @Test
  public void shouldFailOnMissingRequiredChild() {
    ObjectNode mutated = tradeGoodInstance.deepCopy();
    mutated.remove("Product Name");
    ValidationReport validationReport = runValidation(mutated, tradeGoodElement);
    assertValidationStatus(validationReport, "false");
  }
}
