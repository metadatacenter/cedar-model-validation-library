package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.metadatacenter.model.validation.report.ValidationReport;

public class TemplateValidationTest extends BaseValidationTest {

  private static ObjectMapper jsonObjectMapper = new ObjectMapper();

  private ModelValidator modelValidator;

  @BeforeEach
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
  public void shouldFailEmptyChildPropertyIri() throws Exception {
    JsonNode template = jsonObjectMapper.readTree(
        TestResourcesUtils.getStringContent("templates/single-field-template.json"));
    ArrayNode mapping = (ArrayNode) template.path("properties").path("@context").path("properties")
        .path("Study Name").path("enum");
    mapping.removeAll().add("");

    ValidationReport validationReport = modelValidator.validateTemplate(template);

    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "Property IRI for child 'Study Name' must be an absolute IRI");
  }

  @Test
  public void shouldFailEmptyDerivedFromAtRootAndInAChild() throws Exception {
    JsonNode template = jsonObjectMapper.readTree(
        TestResourcesUtils.getStringContent("templates/single-field-template.json"));
    ((com.fasterxml.jackson.databind.node.ObjectNode) template).put("pav:derivedFrom", "");
    ((com.fasterxml.jackson.databind.node.ObjectNode) template.path("properties").path("Study Name"))
        .put("pav:derivedFrom", "");

    ValidationReport validationReport = modelValidator.validateTemplate(template);

    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "pav:derivedFrom must be an absolute IRI when present");
  }

  @Test
  public void shouldPassAbsoluteDerivedFrom() throws Exception {
    JsonNode template = jsonObjectMapper.readTree(
        TestResourcesUtils.getStringContent("templates/single-field-template.json"));
    ((com.fasterxml.jackson.databind.node.ObjectNode) template).put("pav:derivedFrom",
        "https://repo.metadatacenter.org/templates/source");

    ValidationReport validationReport = modelValidator.validateTemplate(template);

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
  public void shouldRejectObjectShapedMultiSelectList() throws Exception {
    ObjectNode template = (ObjectNode) jsonObjectMapper.readTree(
        TestResourcesUtils.getStringContent("templates/multi-select-list-template.json"));
    ObjectNode properties = (ObjectNode) template.path("properties");
    JsonNode arrayField = properties.path("Pick multiple");
    properties.set("Pick multiple", arrayField.path("items").deepCopy());

    ValidationReport validationReport = modelValidator.validateTemplate(template);

    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport,
        "Checkbox, attribute-value, and multiple-choice list fields must be declared as arrays");
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
  public void shouldFailStaticFieldWithoutModelVersion() {
    // A static field is a model specification like any other definition, so it answers for the model
    // it was written against. The meta-schema did not ask it to until the property was declared there.
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/static-field-template.json");
    templateString = JsonUtils.removeFieldFromDocument(templateString,
        "/properties/About Study Form/schema:schemaVersion");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
  }

  @Test
  public void shouldFailAFieldNamingControlledTermAsItsInputType() {
    // A controlled term is not an input type. It is a text field whose value is an IRI — properties
    // of @id, rdfs:label and @type — carrying an ontology, value set, class or branch constraint,
    // which is how cedar-artifact-library recognises one and renders it as controlled-term-field.
    // The IRI enum listed it beside genuine input types, and only a retired editor path wrote it;
    // four production templates were unreadable as a result.
    String templateString = TestResourcesUtils.getStringContent("templates/single-field-template.json")
        .replace("\"inputType\": \"textfield\"", "\"inputType\": \"controlled-term\"");
    ValidationReport validationReport = runValidation(templateString);
    assertValidationStatus(validationReport, "false");
  }

  @Test
  public void shouldFailAPermittedValueWithNoLabel() {
    // A literal's label is the value an instance stores, so a blank one offers a choice whose answer
    // cannot be told from no answer. Optionality is requiredValue's to express, not the value set's.
    String templateString = TestResourcesUtils.getStringContent("templates/multi-select-list-template.json")
        .replace("\"literals\": [", "\"literals\": [ { \"label\": \"\" }, ");
    ValidationReport validationReport = runValidation(templateString);
    assertValidationStatus(validationReport, "false");
  }

  @Test
  public void shouldFailTemplateWithAVersionTheLibraryCannotParse() {
    // pav:version is held as three integers, so a value with fewer parts has no YAML representation
    // at all: a JSON read returns the stored bytes unexamined and a YAML read transcodes them and
    // fails. A bare non-empty string let every such artifact through.
    for (String stated : new String[] { "0.9", "1", "1.0.0-rc1", "requestJson", "" }) {
      String templateString = TestResourcesUtils.getStringContent("templates/empty-template.json")
          .replace("\"pav:version\": \"0.0.1\"", "\"pav:version\": \"" + stated + "\"");
      ValidationReport validationReport = runValidation(templateString);
      assertValidationStatus(validationReport, "false");
    }
  }

  @Test
  public void shouldPassTemplateWithAThreePartVersion() {
    for (String stated : new String[] { "0.0.1", "1.0.0", "12.34.56" }) {
      String templateString = TestResourcesUtils.getStringContent("templates/empty-template.json")
          .replace("\"pav:version\": \"0.0.1\"", "\"pav:version\": \"" + stated + "\"");
      ValidationReport validationReport = runValidation(templateString);
      assertValidationStatus(validationReport, "true");
    }
  }

  @Test
  public void shouldFailTemplateDeclaringAnotherModelVersion() {
    // The meta-schemas define one model, so an artifact naming another is answering for a model they
    // do not describe. A bare non-empty string let every such artifact through.
    // Arrange
    String templateString = TestResourcesUtils.getStringContent("templates/static-field-template.json")
        .replace("\"schema:schemaVersion\": \"1.6.0\"", "\"schema:schemaVersion\": \"1.5.0\"");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport,
        "/schema:schemaVersion: does not have a value in the enumeration ['1.6.0']");
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
  public void shouldRejectLegacyEmptyDerivedFromInRADxMetadataTemplate() {
    String templateString = TestResourcesUtils.getStringContent("templates/RADxMetadataSpecification.json");
    ValidationReport validationReport = runValidation(templateString);
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "pav:derivedFrom must be an absolute IRI when present");
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

  @Disabled("Known validation gap: nested field required arrays are currently accepted when absent")
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

  @Disabled("Known validation gap: nested field value slots are currently accepted when absent")
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
  public void shouldFailFieldNameUsingAt() {
    // Arrange: a child key JSON-LD reserves, which no reader opens.
    String templateString = TestResourcesUtils.getStringContent("templates/check-characters/using-at.json");
    // Act
    ValidationReport validationReport = runValidation(templateString);
    // Assert
    assertValidationStatus(validationReport, "false");
  }
}
