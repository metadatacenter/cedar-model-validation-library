package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.metadatacenter.model.validation.report.ValidationReport;

public class TemplateElementValidationTest extends BaseValidationTest {

  private static ObjectMapper jsonObjectMapper = new ObjectMapper();

  private ModelValidator modelValidator;

  @Before
  public void createNewValidator() {
    modelValidator = new CedarValidator();
  }

  private ValidationReport runValidation(String templateDocument) {
    try {
      JsonNode templateNode = jsonObjectMapper.readTree(templateDocument);
      return modelValidator.validateTemplateElement(templateNode);
    } catch (Exception e) {
      throw new RuntimeException("Programming error", e);
    }
  }

  @Test
  public void shouldPassEmptyElement() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/empty-element.json");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassManyFieldsElement() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassNestedElement() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/nested-element.json");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldFailMissingContext() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/@context");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@context'])");
  }

  @Test
  public void shouldFailMissingId() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/@id");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@id'])");
  }

  @Test
  public void shouldFailMissingType() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/@type");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@type'])");
  }

  @Test
  public void shouldFailMissingJsonType() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/type");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['type'])");
  }

  @Test
  public void shouldFailMissingTitle() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/title");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['title'])");
  }

  @Test
  public void shouldFailMissingDescription() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/description");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['description'])");
  }

  @Test
  public void shouldFailMissingUi() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/_ui");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['_ui'])");
  }

  @Test
  public void shouldFailMissingUi_Title() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/_ui/title");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['title'])");
  }

  @Test
  public void shouldFailMissingUi_Description() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/_ui/description");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['description'])");
  }

  @Test
  public void shouldFailMissingUi_Order() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/_ui/order");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['order'])");
  }

  @Test
  public void shouldFailMissingUi_PropertyLabels() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/_ui/propertyLabels");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['propertyLabels'])");
  }

  @Test
  public void shouldFailMissingProperties() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/properties");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['properties'])");
  }

  @Test
  public void shouldFailMissingRequired() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/required");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['required'])");
  }

  @Test
  public void shouldFailMissingCreatedOn() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/pav:createdOn");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:createdOn'])");
  }

  @Test
  public void shouldFailMissingCreatedBy() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/pav:createdBy");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:createdBy'])");
  }

  @Test
  public void shouldFailMissingLastUpdatedOn() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/pav:lastUpdatedOn");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:lastUpdatedOn'])");
  }

  @Test
  public void shouldFailMissingModifiedBy() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/oslc:modifiedBy");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['oslc:modifiedBy'])");
  }

  @Test
  public void shouldPassMissingAdditionalProperties() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/additionalProperties");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldFailMissingSchema() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/$schema");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['$schema'])");
  }

  @Test
  public void shouldFailMissingProperties_Context() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/properties/@context");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@context'])");
  }

  @Test
  public void shouldFailMissingProperties_Id() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/properties/@id");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@id'])");
  }

  @Test
  public void shouldFailMissingProperties_Type() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/properties/@type");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@type'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_Type() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/properties/firstName/@type");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@type'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_Context() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/properties/firstName/@context");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@context'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_JsonType() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/properties/firstName/type");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['type'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_Title() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/properties/firstName/title");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['title'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_Description() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/properties/firstName/description");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['description'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_Required() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/properties/firstName/required");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['required'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_CreatedOn() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/properties/firstName/pav:createdOn");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:createdOn'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_CreatedBy() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/properties/firstName/pav:createdBy");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:createdBy'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_LastUpdatedOn() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/properties/firstName/pav:lastUpdatedOn");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:lastUpdatedOn'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_ModifiedBy() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/properties/firstName/oslc:modifiedBy");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['oslc:modifiedBy'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_AdditionalProperties() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/properties/firstName/additionalProperties");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['additionalProperties'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_Id() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/properties/firstName/@id");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@id'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_Schema() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/properties/firstName/$schema");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['$schema'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_Properties() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/properties/firstName/properties");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['properties'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_Properties_Type() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/properties/firstName/properties/@type");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@type'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_Properties_Value() {
    // Arrange
    String elementString = TestResourcesUtils.getStringContent("elements/many-fields-element.json");
    elementString = JsonUtils.removeFieldFromDocument(elementString, "/properties/firstName/properties/@value");
    // Act
    ValidationReport validationReport = runValidation(elementString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@value'])");
  }
}
