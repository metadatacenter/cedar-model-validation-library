package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.metadatacenter.model.validation.report.ValidationReport;

public class TemplateValidationTest extends BaseValidationTest {

  private static ObjectMapper jsonObjectMapper = new ObjectMapper();

  private ModelValidator modelValidator;

  @Before
  public void createNewValidator() {
    modelValidator = new CedarValidator();
  }

  private ValidationReport runValidation(String templateDocument) {
    try {
      JsonNode templateNode = jsonObjectMapper.readTree(templateDocument);
      return modelValidator.validateTemplate(templateNode);
    } catch (Exception e) {
      throw new RuntimeException("Programming error", e);
    }
  }

  @Test
  public void shouldPassEmptyTemplate() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/empty-template.json");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassSingleFieldTemplate() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/single-field-template.json");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassTemplateWithAnnotations() {
    String templateString = TestResourcesUtils.getStringContent("templates/template-allowing-annotations.json");
    ValidationReport validationReport = runValidation(templateString);
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassTemplateWithCheckbox() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/checkbox-template.json");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassTemplateWithMultiSelectList() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/multi-select-list-template.json");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassStaticFieldTemplate() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/static-field-template.json");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassManyFieldsTemplate() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassMultipleFieldItemsTemplate() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/multiple-field-items-template.json");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassMultipleElementItemsTemplate() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/multiple-element-items-template.json");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassNestedElementTemplate() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/nested-element-template.json");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassAttributeValueTemplate() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/attribute-value-template.json");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassRADxMetadataTemplate() {
    String templateString = TestResourcesUtils.getStringContent("templates/RADxMetadataSpecification.json");
    ValidationReport validationReport = runValidation(templateString);
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassDataCiteTemplate() {
    String templateString = TestResourcesUtils.getStringContent("templates/DataCiteTemplate.json");
    ValidationReport validationReport = runValidation(templateString);
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassADVANCETemplate() {
    String templateString = TestResourcesUtils.getStringContent("templates/ADVANCETemplate.json");
    ValidationReport validationReport = runValidation(templateString);
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassSampleBlockTemplate() {
    String templateString = TestResourcesUtils.getStringContent("templates/SampleBlock.json");
    ValidationReport validationReport = runValidation(templateString);
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassSampleBlockSection() {
    String templateString = TestResourcesUtils.getStringContent("templates/SampleSection.json");
    ValidationReport validationReport = runValidation(templateString);
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassSampleBlockSuspension() {
    String templateString = TestResourcesUtils.getStringContent("templates/SampleSuspension.json");
    ValidationReport validationReport = runValidation(templateString);
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldFailMissingContext() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/@context");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@context'])");
  }

  @Test
  public void shouldFailMissingId() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/@id");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@id'])");
  }

  @Test
  public void shouldFailMissingType() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/@type");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@type'])");
  }

  @Test
  public void shouldFailMissingJsonType() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/type");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['type'])");
  }

  @Test
  public void shouldFailMissingTitle() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/title");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['title'])");
  }

  @Test
  public void shouldFailMissingDescription() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/description");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['description'])");
  }

  @Test
  public void shouldFailMissingUi() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/_ui");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['_ui'])");
  }

  @Test
  public void shouldFailMissingSchemaName() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/schema:name");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['schema:name'])");
  }

  @Test
  public void shouldFailMissingSchemaDescription() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/schema:description");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['schema:description'])");
  }

  @Test
  public void shouldFailMissingUi_Order() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/_ui/order");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['order'])");
  }

  @Test
  public void shouldFailMissingUi_PropertyLabels() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/_ui/propertyLabels");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['propertyLabels'])");
  }

  @Test
  public void shouldFailMissingProperties() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['properties'])");
  }

  @Test
  public void shouldFailMissingRequired() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/required");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['required'])");
  }

  @Test
  public void shouldFailMissingCreatedOn() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/pav:createdOn");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:createdOn'])");
  }

  @Test
  public void shouldFailMissingCreatedBy() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/pav:createdBy");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:createdBy'])");
  }

  @Test
  public void shouldFailMissingLastUpdatedOn() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/pav:lastUpdatedOn");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:lastUpdatedOn'])");
  }

  @Test
  public void shouldFailMissingModifiedBy() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/oslc:modifiedBy");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['oslc:modifiedBy'])");
  }

  @Test
  public void shouldPassMissingAdditionalProperties() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/additionalProperties");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldFailMissingSchema() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/$schema");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['$schema'])");
  }

  @Test
  public void shouldFailMissingProperties_Context() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/@context");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@context'])");
  }

  @Test
  public void shouldFailMissingProperties_Id() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/@id");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@id'])");
  }

  @Test
  public void shouldFailMissingProperties_Type() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/@type");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@type'])");
  }

  @Test
  public void shouldFailMissingProperties_IsBasedOn() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/schema:isBasedOn");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['schema:isBasedOn'])");
  }

  @Test
  public void shouldFailMissingProperties_Name() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/schema:name");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['schema:name'])");
  }

  @Test
  public void shouldFailMissingProperties_Description() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/schema:description");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['schema:description'])");
  }

  @Test
  public void shouldFailMissingProperties_CreatedOn() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/pav:createdOn");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:createdOn'])");
  }

  @Test
  public void shouldFailMissingProperties_CreatedBy() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/pav:createdBy");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:createdBy'])");
  }

  @Test
  public void shouldFailMissingProperties_LastUpdatedOn() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/pav:lastUpdatedOn");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:lastUpdatedOn'])");
  }

  @Test
  public void shouldFailMissingProperties_ModifiedBy() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/oslc:modifiedBy");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['oslc:modifiedBy'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_Type() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/Study Name/@type");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@type'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_Context() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/Study Name/@context");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@context'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_JsonType() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/Study Name/type");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['type'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_Title() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/Study Name/title");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['title'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_Description() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/Study Name/description");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['description'])");
  }

  @Ignore
  @Test
  public void shouldFailMissingProperties_Field_Required() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/Study Name/required");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['required'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_CreatedOn() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/Study Name/pav:createdOn");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:createdOn'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_CreatedBy() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/Study Name/pav:createdBy");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:createdBy'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_LastUpdatedOn() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/Study Name/pav:lastUpdatedOn");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:lastUpdatedOn'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_ModifiedBy() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/Study Name/oslc:modifiedBy");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['oslc:modifiedBy'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_AdditionalProperties() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/Study Name/additionalProperties");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['additionalProperties'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_Id() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/Study Name/@id");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@id'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_Schema() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/Study Name/$schema");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['$schema'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_Properties() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/Study Name/properties");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['properties'])");
  }

  @Test
  public void shouldFailMissingProperties_Field_Properties_Type() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/Study Name/properties/@type");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@type'])");
  }

  @Ignore
  @Test
  public void shouldFailMissingProperties_Field_Properties_Value() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString, "/properties/Study Name/properties/@value");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@value'])");
  }

  @Test
  public void shouldPassFieldNameUsingColon() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/check-characters/using-colon.json");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldFailFieldNameUsingPeriod() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/check-characters/using-period.json");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object instance has properties which are not allowed by the schema: " +
        "['study.name']");
  }

  @Test
  public void shouldFailFieldNameUsingDollar() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/check-characters/using-dollar.json");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object instance has properties which are not allowed by the schema: " +
        "['$studyname']");
  }

  @Test
  public void shouldPassFieldNameUsingSlash() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/check-characters/using-slash.json");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassFieldNameUsingParentheses() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/check-characters/using-parentheses.json");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassFieldNameUsingDoubleQuotes() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/check-characters/using-quotes.json");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassFieldNameUsingAt() {
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/check-characters/using-at.json");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "true");
  }
}
