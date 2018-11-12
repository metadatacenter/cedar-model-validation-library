package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
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
  public void shouldPassTextFieldWithMinMaxLengthConstraint() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field-minmax-length.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassTextFieldWithDefaultValueConstraint() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field-default-value.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassTextFieldWithQuestions() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field-with-questions.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassNumericFielddWithDecimalPlaceConstraint() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/numeric-field-decimal-place.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassNumericFielddWithMinMaxValueConstraint() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/numeric-field-minmax-value.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassNumericFielddWithNumberTypeConstraint() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/numeric-field-number-type.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassNumericFielddWithUnitOfMeasureConstraint() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/numeric-field-unit-of-measure.json");
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
  public void shouldPassSingleSelectionListField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/list-field-single-selection.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassMultiSelectionListField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/list-field-multi-selection.json");
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
  public void shouldPassControlledTextField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/controlled-text-field.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassControlledTextFieldWithDefaultValueConstraint() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/controlled-text-field-default-value.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassControlledTextFieldWithActions() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/controlled-text-field-actions.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassAttributeValueField() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/attribute-value-field.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassSectionBreak() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/section-break.json");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassPageBreak() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/page-break.json");
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
  public void shouldFailMissingSchemaName() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/schema:name");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['schema:name'])");
  }

  @Test
  public void shouldFailMissingSchemaDescription() {
    // Arrange
    String fieldString = TestResourcesUtils.getStringContent("fields/text-field.json");
    fieldString = JsonUtils.removeFieldFromDocument(fieldString, "/schema:description");
    // Act
    ValidationReport validationReport = runValidation(fieldString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['schema:description'])");
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

  @Ignore
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

  @Ignore
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
}
