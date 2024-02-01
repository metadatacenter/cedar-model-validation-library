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
  private static String manyFieldsTemplate;
  private static String multipleFieldItemsTemplate;
  private static String multipleElementItemsTemplate;
  private static String allowingAnnotationsTemplate;
  private static String nestedElementTemplate;
  private static String attributeValueTemplate;
  private ModelValidator modelValidator;

  @BeforeClass
  public static void loadTemplateExamples() {
    singleFieldTemplate = TestResourcesUtils.getStringContent("templates/single-field-template.json");
    manyFieldsTemplate = TestResourcesUtils.getStringContent("templates/many-fields-template.json");
    multipleFieldItemsTemplate = TestResourcesUtils.getStringContent("templates/multiple-field-items-template.json");
    multipleElementItemsTemplate = TestResourcesUtils.getStringContent("templates/multiple-element-items-template.json");
    allowingAnnotationsTemplate = TestResourcesUtils.getStringContent("templates/template-allowing-annotations.json");
    nestedElementTemplate = TestResourcesUtils.getStringContent("templates/nested-element-template.json");
    attributeValueTemplate = TestResourcesUtils.getStringContent("templates/attribute-value-template.json");
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
    String instanceString = TestResourcesUtils.getStringContent("instances/single-field-instance.jsonld");
    ValidationReport validationReport = runValidation(instanceString, singleFieldTemplate);
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassInstanceWithAnnotations() {
    String instanceString = TestResourcesUtils.getStringContent("instances/instance-with-annotations.jsonld");
    ValidationReport validationReport = runValidation(instanceString, allowingAnnotationsTemplate);
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassManyFieldsInstance() {
    String instanceString = TestResourcesUtils.getStringContent("instances/many-fields-instance.jsonld");
    ValidationReport validationReport = runValidation(instanceString, manyFieldsTemplate);
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassMultipleFieldItemsInstance() {
    String instanceString = TestResourcesUtils.getStringContent("instances/multiple-field-items-instance.jsonld");
    ValidationReport validationReport = runValidation(instanceString, multipleFieldItemsTemplate);
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassMultipleElementItemsInstance() {
    String instanceString = TestResourcesUtils.getStringContent("instances/multiple-element-items-instance.jsonld");
    ValidationReport validationReport = runValidation(instanceString, multipleElementItemsTemplate);
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassNestedElementInstance() {
    String instanceString = TestResourcesUtils.getStringContent("instances/nested-element-instance.jsonld");
    ValidationReport validationReport = runValidation(instanceString, nestedElementTemplate);
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldPassAttributeValueInstance() {
    String instanceString = TestResourcesUtils.getStringContent("instances/attribute-value-instance.jsonld");
    ValidationReport validationReport = runValidation(instanceString, attributeValueTemplate);
    assertValidationStatus(validationReport, "true");
  }

  @Test
  public void shouldFailMissingContext() {
    String instanceString = TestResourcesUtils.getStringContent("instances/many-fields-instance.jsonld");
    instanceString = JsonUtils.removeFieldFromDocument(instanceString, "/@context");
    ValidationReport validationReport = runValidation(instanceString, manyFieldsTemplate);
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@context'])");
  }

  @Test
  public void shouldFailMissingId() {
    String instanceString = TestResourcesUtils.getStringContent("instances/many-fields-instance.jsonld");
    instanceString = JsonUtils.removeFieldFromDocument(instanceString, "/@id");
    ValidationReport validationReport = runValidation(instanceString, manyFieldsTemplate);
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['@id'])");
  }

  @Test
  public void shouldFailMissingName() {
    String instanceString = TestResourcesUtils.getStringContent("instances/many-fields-instance.jsonld");
    instanceString = JsonUtils.removeFieldFromDocument(instanceString, "/schema:name");
    ValidationReport validationReport = runValidation(instanceString, manyFieldsTemplate);
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['schema:name'])");
  }

  @Test
  public void shouldFailMissingDescription() {
    String instanceString = TestResourcesUtils.getStringContent("instances/many-fields-instance.jsonld");
    instanceString = JsonUtils.removeFieldFromDocument(instanceString, "/schema:description");
    ValidationReport validationReport = runValidation(instanceString, manyFieldsTemplate);
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['schema:description'])");
  }

  @Test
  public void shouldFailMissingCreatedOn() {
    String instanceString = TestResourcesUtils.getStringContent("instances/many-fields-instance.jsonld");
    instanceString = JsonUtils.removeFieldFromDocument(instanceString, "/pav:createdOn");
    ValidationReport validationReport = runValidation(instanceString, manyFieldsTemplate);
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:createdOn'])");
  }

  @Test
  public void shouldFailMissingCreatedBy() {
    String instanceString = TestResourcesUtils.getStringContent("instances/many-fields-instance.jsonld");
    instanceString = JsonUtils.removeFieldFromDocument(instanceString, "/pav:createdBy");
    ValidationReport validationReport = runValidation(instanceString, manyFieldsTemplate);
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:createdBy'])");
  }

  @Test
  public void shouldFailMissingLastUpdatedOn() {
    String instanceString = TestResourcesUtils.getStringContent("instances/many-fields-instance.jsonld");
    instanceString = JsonUtils.removeFieldFromDocument(instanceString, "/pav:lastUpdatedOn");
    ValidationReport validationReport = runValidation(instanceString, manyFieldsTemplate);
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['pav:lastUpdatedOn'])");
  }

  @Test
  public void shouldFailMissingModifiedBy() {
    String instanceString = TestResourcesUtils.getStringContent("instances/many-fields-instance.jsonld");
    instanceString = JsonUtils.removeFieldFromDocument(instanceString, "/oslc:modifiedBy");
    ValidationReport validationReport = runValidation(instanceString, manyFieldsTemplate);
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['oslc:modifiedBy'])");
  }

  @Test
  public void shouldFailMissingFields() {
    String instanceString = TestResourcesUtils.getStringContent("instances/many-fields-instance.jsonld");
    instanceString = JsonUtils.removeFieldFromDocument(instanceString, "/Study Name");
    ValidationReport validationReport = runValidation(instanceString, manyFieldsTemplate);
    assertValidationStatus(validationReport, "false");
    assertValidationMessage(validationReport, "object has missing required properties (['Study Name'])");
  }
}
