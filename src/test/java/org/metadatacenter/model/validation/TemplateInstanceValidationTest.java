package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.metadatacenter.model.validation.report.ValidationReport;

public class TemplateInstanceValidationTest extends BaseValidationTest {

  private static ObjectMapper jsonObjectMapper = new ObjectMapper();

  private static String singleFieldTemplate;
  private static String multiFieldTemplate;
  private static String multivaluedFieldTemplate;
  private static String multivaluedElementTemplate;
  private static String nestedElementTemplate;

  private ModelValidator modelValidator;

  @BeforeClass
  public static void loadTemplateExamples() {
    singleFieldTemplate = TestResourcesUtils.getStringContent("templates/single-field-template.json");
    multiFieldTemplate = TestResourcesUtils.getStringContent("templates/multi-field-template.json");
    multivaluedFieldTemplate = TestResourcesUtils.getStringContent("templates/multivalued-field-template.json");
    multivaluedElementTemplate = TestResourcesUtils.getStringContent("templates/multivalued-element-template.json");
    nestedElementTemplate = TestResourcesUtils.getStringContent("templates/nested-element-template.json");
  }

  @Before
  public void createNewValidator() {
    modelValidator = new CedarValidator();
  }

  private ValidationReport runValidation(String templateDocument, String schemaDocument) {
    try {
      JsonNode instanceNode = jsonObjectMapper.readTree(templateDocument);
      JsonNode schemaNode = jsonObjectMapper.readTree(schemaDocument);
      return modelValidator.validateTemplateInstance(instanceNode, schemaNode);
    } catch (Exception e) {
      throw new RuntimeException("Programming error", e);
    }
  }

  @Test
  public void shouldPassSingleFieldInstance() {
    // Arrange
    String instanceString = TestResourcesUtils.getStringContent("instances/single-field-instance.jsonld");
    // Act
    ValidationReport validationReport = runValidation(instanceString, singleFieldTemplate);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassMultiFieldInstance() {
    // Arrange
    String instanceString = TestResourcesUtils.getStringContent("instances/multi-field-instance.jsonld");
    // Act
    ValidationReport validationReport = runValidation(instanceString, multiFieldTemplate);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassMultivaluedFieldInstance() {
    // Arrange
    String instanceString = TestResourcesUtils.getStringContent("instances/multivalued-field-instance.jsonld");
    // Act
    ValidationReport validationReport = runValidation(instanceString, multivaluedFieldTemplate);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassMultivaluedElementInstance() {
    // Arrange
    String instanceString = TestResourcesUtils.getStringContent("instances/multivalued-element-instance.jsonld");
    // Act
    ValidationReport validationReport = runValidation(instanceString, multivaluedElementTemplate);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassNestedElementInstance() {
    // Arrange
    String instanceString = TestResourcesUtils.getStringContent("instances/nested-element-instance.jsonld");
    // Act
    ValidationReport validationReport = runValidation(instanceString, nestedElementTemplate);
    // Assert
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldFailMissingContext() {
    // Arrange
    String instanceString = TestResourcesUtils.getStringContent("instances/multi-field-instance.jsonld");
    instanceString = JsonUtils.removeFieldFromDocument(instanceString, "/@context");
    // Act
    ValidationReport validationReport = runValidation(instanceString, multiFieldTemplate);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@context'])");
  }

  @Test
  public void shouldFailMissingId() {
    // Arrange
    String instanceString = TestResourcesUtils.getStringContent("instances/multi-field-instance.jsonld");
    instanceString = JsonUtils.removeFieldFromDocument(instanceString, "/@id");
    // Act
    ValidationReport validationReport = runValidation(instanceString, multiFieldTemplate);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@id'])");
  }

  @Test
  public void shouldFailMissingName() {
    // Arrange
    String instanceString = TestResourcesUtils.getStringContent("instances/multi-field-instance.jsonld");
    instanceString = JsonUtils.removeFieldFromDocument(instanceString, "/schema:name");
    // Act
    ValidationReport validationReport = runValidation(instanceString, multiFieldTemplate);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['schema:name'])");
  }

  @Test
  public void shouldFailMissingDescription() {
    // Arrange
    String instanceString = TestResourcesUtils.getStringContent("instances/multi-field-instance.jsonld");
    instanceString = JsonUtils.removeFieldFromDocument(instanceString, "/schema:description");
    // Act
    ValidationReport validationReport = runValidation(instanceString, multiFieldTemplate);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['schema:description'])");
  }

  @Test
  public void shouldFailMissingCreatedOn() {
    // Arrange
    String instanceString = TestResourcesUtils.getStringContent("instances/multi-field-instance.jsonld");
    instanceString = JsonUtils.removeFieldFromDocument(instanceString, "/pav:createdOn");
    // Act
    ValidationReport validationReport = runValidation(instanceString, multiFieldTemplate);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:createdOn'])");
  }

  @Test
  public void shouldFailMissingCreatedBy() {
    // Arrange
    String instanceString = TestResourcesUtils.getStringContent("instances/multi-field-instance.jsonld");
    instanceString = JsonUtils.removeFieldFromDocument(instanceString, "/pav:createdBy");
    // Act
    ValidationReport validationReport = runValidation(instanceString, multiFieldTemplate);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:createdBy'])");
  }

  @Test
  public void shouldFailMissingLastUpdatedOn() {
    // Arrange
    String instanceString = TestResourcesUtils.getStringContent("instances/multi-field-instance.jsonld");
    instanceString = JsonUtils.removeFieldFromDocument(instanceString, "/pav:lastUpdatedOn");
    // Act
    ValidationReport validationReport = runValidation(instanceString, multiFieldTemplate);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:lastUpdatedOn'])");
  }

  @Test
  public void shouldFailMissingModifiedBy() {
    // Arrange
    String instanceString = TestResourcesUtils.getStringContent("instances/multi-field-instance.jsonld");
    instanceString = JsonUtils.removeFieldFromDocument(instanceString, "/oslc:modifiedBy");
    // Act
    ValidationReport validationReport = runValidation(instanceString, multiFieldTemplate);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['oslc:modifiedBy'])");
  }

  @Test
  public void shouldFailMissingFields() {
    // Arrange
    String instanceString = TestResourcesUtils.getStringContent("instances/multi-field-instance.jsonld");
    instanceString = JsonUtils.removeFieldFromDocument(instanceString, "/studyName");
    // Act
    ValidationReport validationReport = runValidation(instanceString, multiFieldTemplate);
    // Assert
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['studyName'])");
  }
}