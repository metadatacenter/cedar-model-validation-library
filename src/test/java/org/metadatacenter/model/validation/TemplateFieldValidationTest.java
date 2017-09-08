package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.metadatacenter.model.validation.report.ValidationReport;

public class TemplateFieldValidationTest extends BaseValidationTest {

  private static ObjectMapper jsonObjectMapper = new ObjectMapper();

  private ModelValidator modelValidator;

  @Before
  public void createNewValidator() {
    modelValidator = new CedarValidator();
  }

  private ValidationReport runValidation(String templateDocument) {
    try {
      JsonNode templateNode = jsonObjectMapper.readTree(templateDocument);
      return modelValidator.validateTemplateField(templateNode);
    } catch (Exception e) {
      throw new RuntimeException("Programming error", e);
    }
  }

  @Test
  public void shouldPassTextField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassCheckboxField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/checkbox-field.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassDateField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/date-field.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassEmailField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/email-field.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassImageField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/image-field.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassListField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/list-field.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassNumericField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/numeric-field.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassParagraphField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/paragraph-field.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassPhoneField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/phone-field.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassRadioField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/radio-field.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassRichTextField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/rich-text-field.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassVideoField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/video-field.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassLinkField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/link-field.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassConstrainedTextField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/constrained-text-field.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldFailMissingContext() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/@context");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@context'])");
  }

  @Test
  public void shouldFailMissingId() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/@id");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@id'])");
  }

  @Test
  public void shouldFailMissingType() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/@type");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@type'])");
  }

  @Test
  public void shouldFailMissingJsonType() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/type");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['type'])");
  }

  @Test
  public void shouldFailMissingTitle() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/title");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['title'])");
  }

  @Test
  public void shouldFailMissingDescription() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/description");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['description'])");
  }

  @Test
  public void shouldFailMissingUi() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/_ui");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['_ui'])");
  }

  @Test
  public void shouldFailMissingUi_Title() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/_ui/title");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['title'])");
  }

  @Test
  public void shouldFailMissingUi_Description() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/_ui/description");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['description'])");
  }

  @Test
  public void shouldFailMissingUi_InputType() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/_ui/inputType");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['inputType'])");
  }

  @Test
  public void shouldFailMissingProperties() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/properties");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['properties'])");
  }

  @Test
  public void shouldFailMissingRequired() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/required");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['required'])");
  }

  @Test
  public void shouldFailMissingValueConstraints() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/_valueConstraints");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['_valueConstraints'])");
  }

  @Test
  public void shouldFailMissingCreatedOn() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/pav:createdOn");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:createdOn'])");
  }

  @Test
  public void shouldFailMissingCreatedBy() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/pav:createdBy");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:createdBy'])");
  }

  @Test
  public void shouldFailMissingLastUpdatedOn() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/pav:lastUpdatedOn");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:lastUpdatedOn'])");
  }

  @Test
  public void shouldFailMissingModifiedBy() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/oslc:modifiedBy");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['oslc:modifiedBy'])");
  }

  @Test
  public void shouldFailMissingAdditionalProperties() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/additionalProperties");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['additionalProperties'])");
  }

  @Test
  public void shouldFailMissingSchema() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/$schema");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['$schema'])");
  }

  @Test
  public void shouldFailMissingProperties_Value() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/properties/@value");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@value'])");
  }

  @Test
  public void shouldFailMissingProperties_Id() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/constrained-text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/properties/@id");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@id'])");
  }

  @Test
  public void shouldFailMissingProperties_Type() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/properties/@type");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@type'])");
  }

  @Test
  public void shouldFailMissingProperties_RdfsLabel() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/properties/rdfs:label");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['rdfs:label'])");
  }

  @Test
  public void shouldFailMisplacedIdProperty_InTextField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/value-constraints/invalid-text-field-1.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@value'])");
  }

  @Test
  public void shouldFailMisplacedValueProperty_InConstrainedTextField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent
        ("fields/value-constraints/invalid-constrained-text-field-1.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@id'])");
  }

  @Test
  public void shouldFailMisplaceValueIdProperty_InTextField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/value-constraints/invalid-text-field-2.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has invalid properties (['@id'])");
  }

  @Test
  public void shouldFailMisplaceValueIdProperty_InConstrainedTextField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent
        ("fields/value-constraints/invalid-constrained-text-field-2.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has invalid properties (['@value'])");
  }

  @Test
  public void shouldFailMisplacedIdProperty_InRadioField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/value-constraints/invalid-radio-field.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@value'])");
  }
}
