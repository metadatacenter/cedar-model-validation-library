package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

public class CEDARModelValidator
{
  private static final ObjectMapper MAPPER = JacksonUtils.newMapper();
  private final JsonValidator jsonSchemaValidator;

  private static final String CORE_JSON_SCHEMA_FIELDS_SCHEMA_RESOURCE_NAME = "coreJSONSchemaFields.json";
  private static final String JSON_LD_ID_FIELD_SCHEMA_RESOURCE_NAME = "jsonLDIDField.json";
  private static final String JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME = "jsonLDTypeField.json";
  private static final String PROVENANCE_FIELDS_SCHEMA_RESOURCE_NAME = "provenanceFields.json";

  private static final String CORE_SCHEMA_RESOURCE_NAMES[] = { CORE_JSON_SCHEMA_FIELDS_SCHEMA_RESOURCE_NAME,
    JSON_LD_ID_FIELD_SCHEMA_RESOURCE_NAME, JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME,
    PROVENANCE_FIELDS_SCHEMA_RESOURCE_NAME };

  private static final String CORE_JSON_SCHEMA_TEMPLATE_FIELDS_SCHEMA_RESOURCE_NAME = "coreJSONSchemaTemplateFields.json";
  private static final String TEMPLATE_JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME = "templateJSONLDTypeField.json";
  private static final String TEMPLATE_JSON_LD_CONTEXT_FIELD_SCHEMA_RESOURCE_NAME = "templateJSONLDContextField.json";
  private static final String TEMPLATE_UI_FIELD_SCHEMA_RESOURCE_NAME = "templateUIField.json";
  private static final String TEMPLATE_REQUIRED_FIELD_SCHEMA_RESOURCE_NAME = "templateRequiredField.json";
  private static final String TEMPLATE_PROPERTIES_FIELD_SCHEMA_RESOURCE_NAME = "templatePropertiesField.json";
  private static final String TEMPLATE_SCHEMA_RESOURCE_NAME = "template.json";

  private static final String TEMPLATE_SCHEMA_RESOURCE_NAMES[] = {
    CORE_JSON_SCHEMA_TEMPLATE_FIELDS_SCHEMA_RESOURCE_NAME, TEMPLATE_JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME,
    TEMPLATE_JSON_LD_CONTEXT_FIELD_SCHEMA_RESOURCE_NAME, TEMPLATE_UI_FIELD_SCHEMA_RESOURCE_NAME,
    TEMPLATE_REQUIRED_FIELD_SCHEMA_RESOURCE_NAME, TEMPLATE_PROPERTIES_FIELD_SCHEMA_RESOURCE_NAME,
    TEMPLATE_SCHEMA_RESOURCE_NAME };

  private static final String CORE_JSON_SCHEMA_TEMPLATE_ELEMENT_FIELDS_SCHEMA_RESOURCE_NAME = "coreJSONSchemaTemplateElementFields.json";
  private static final String TEMPLATE_ELEMENT_JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME = "templateElementJSONLDTypeField.json";
  private static final String TEMPLATE_ELEMENT_JSON_LD_CONTEXT_FIELD_SCHEMA_RESOURCE_NAME = "templateElementJSONLDContextField.json";
  private static final String TEMPLATE_ELEMENT_PROPERTIES_FIELD_SCHEMA_RESOURCE_NAME = "templateElementPropertiesField.json";
  private static final String TEMPLATE_ELEMENT_UI_FIELD_SCHEMA_RESOURCE_NAME = "templateElementUIField.json";
  private static final String TEMPLATE_ELEMENT_SCHEMA_RESOURCE_NAME = "templateElement.json";

  private static final String TEMPLATE_ELEMENT_SCHEMA_RESOURCE_NAMES[] = {
    CORE_JSON_SCHEMA_TEMPLATE_ELEMENT_FIELDS_SCHEMA_RESOURCE_NAME,
    TEMPLATE_ELEMENT_JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME,
    TEMPLATE_ELEMENT_JSON_LD_CONTEXT_FIELD_SCHEMA_RESOURCE_NAME, TEMPLATE_ELEMENT_PROPERTIES_FIELD_SCHEMA_RESOURCE_NAME,
    TEMPLATE_ELEMENT_UI_FIELD_SCHEMA_RESOURCE_NAME, TEMPLATE_ELEMENT_SCHEMA_RESOURCE_NAME };

  private static final String TEMPLATE_FIELD_JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME = "templateFieldJSONLDTypeField.json";
  private static final String TEMPLATE_FIELD_JSON_LD_CONTEXT_FIELD_SCHEMA_RESOURCE_NAME = "templateFieldJSONLDContextField.json";
  private static final String VALUE_CONSTRAINTS_FIELD_SCHEMA_RESOURCE_NAME = "valueConstraintsField.json";
  private static final String TEMPLATE_FIELD_SINGLE_VALUE_CONTENT_FIELD_SCHEMA_RESOURCE_NAME = "templateFieldSingleValueContent.json";
  private static final String TEMPLATE_FIELD_UI_FIELD_SCHEMA_RESOURCE_NAME = "templateFieldUIField.json";
  private static final String TEMPLATE_FIELD_SCHEMA_RESOURCE_NAME = "templateField.json";

  private static final String TEMPLATE_FIELD_SCHEMA_RESOURCE_NAMES[] = {
    TEMPLATE_FIELD_JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME, TEMPLATE_FIELD_JSON_LD_CONTEXT_FIELD_SCHEMA_RESOURCE_NAME,
    VALUE_CONSTRAINTS_FIELD_SCHEMA_RESOURCE_NAME, TEMPLATE_FIELD_SINGLE_VALUE_CONTENT_FIELD_SCHEMA_RESOURCE_NAME,
    TEMPLATE_FIELD_UI_FIELD_SCHEMA_RESOURCE_NAME, TEMPLATE_FIELD_SCHEMA_RESOURCE_NAME };

  public CEDARModelValidator()
  {
    JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
    this.jsonSchemaValidator = factory.getValidator();
  }

  public Optional<ProcessingReport> validateTemplateNode(JsonNode templateNode)
    throws ProcessingException, IOException, URISyntaxException
  {
    for (int resourceNameIndex = 0; resourceNameIndex < CORE_SCHEMA_RESOURCE_NAMES.length; resourceNameIndex++) {
      String resourceName = CORE_SCHEMA_RESOURCE_NAMES[resourceNameIndex];
      JsonNode jsonSchemaNode = loadJsonNodeFromResource(resourceName);
      ProcessingReport report = jsonSchemaValidate(jsonSchemaNode, templateNode);

      if (!report.isSuccess())
        return Optional.of(report);
    }

    for (int resourceNameIndex = 0; resourceNameIndex < TEMPLATE_SCHEMA_RESOURCE_NAMES.length; resourceNameIndex++) {
      String resourceName = TEMPLATE_SCHEMA_RESOURCE_NAMES[resourceNameIndex];
      JsonNode jsonSchemaNode = loadJsonNodeFromResource(resourceName);
      ProcessingReport report = jsonSchemaValidate(jsonSchemaNode, templateNode);

      if (!report.isSuccess())
        return Optional.of(report);
    }

    return Optional.empty();
  }

  public Optional<ProcessingReport> validateTemplateElementNode(JsonNode templateElementNode)
    throws ProcessingException, IOException, URISyntaxException
  {
    for (int resourceNameIndex = 0; resourceNameIndex < CORE_SCHEMA_RESOURCE_NAMES.length; resourceNameIndex++) {
      String resourceName = CORE_SCHEMA_RESOURCE_NAMES[resourceNameIndex];
      JsonNode jsonSchemaNode = loadJsonNodeFromResource(resourceName);
      ProcessingReport report = jsonSchemaValidate(jsonSchemaNode, templateElementNode);

      if (!report.isSuccess())
        return Optional.of(report);
    }

    for (int resourceNameIndex = 0;
         resourceNameIndex < TEMPLATE_ELEMENT_SCHEMA_RESOURCE_NAMES.length; resourceNameIndex++) {
      String resourceName = TEMPLATE_ELEMENT_SCHEMA_RESOURCE_NAMES[resourceNameIndex];
      JsonNode jsonSchemaNode = loadJsonNodeFromResource(resourceName);
      ProcessingReport report = jsonSchemaValidate(jsonSchemaNode, templateElementNode);

      if (!report.isSuccess())
        return Optional.of(report);
    }

    return Optional.empty();
  }

  public Optional<ProcessingReport> validateTemplateFieldNode(JsonNode templateFieldNode)
    throws ProcessingException, IOException, URISyntaxException
  {
    for (int resourceNameIndex = 0; resourceNameIndex < CORE_SCHEMA_RESOURCE_NAMES.length; resourceNameIndex++) {
      String resourceName = CORE_SCHEMA_RESOURCE_NAMES[resourceNameIndex];
      JsonNode jsonSchemaNode = loadJsonNodeFromResource(resourceName);
      ProcessingReport report = jsonSchemaValidate(jsonSchemaNode, templateFieldNode);

      if (!report.isSuccess())
        return Optional.of(report);
    }

    for (int resourceNameIndex = 0;
         resourceNameIndex < TEMPLATE_FIELD_SCHEMA_RESOURCE_NAMES.length; resourceNameIndex++) {
      String resourceName = TEMPLATE_FIELD_SCHEMA_RESOURCE_NAMES[resourceNameIndex];
      JsonNode jsonSchemaNode = loadJsonNodeFromResource(resourceName);
      ProcessingReport report = jsonSchemaValidate(jsonSchemaNode, templateFieldNode);

      if (!report.isSuccess())
        return Optional.of(report);
    }

    return Optional.empty();
  }

  /**
   * Validate a JSON node against a JSON Schema node
   *
   * @param schemaNode   A node containing a JSON Schema document
   * @param instanceNode A node containing a JSON document
   * @return A processing report
   * @throws ProcessingException If a processing exception occurs during processing
   * @throws IOException         If an IO exception occurs during processing
   * @throws URISyntaxException  If a URI syntax exception occurs during processing
   */
  public ProcessingReport jsonSchemaValidate(JsonNode schemaNode, JsonNode instanceNode)
    throws ProcessingException, IOException, URISyntaxException
  {
    JsonSchema schema = JsonSchemaFactory.byDefault().getJsonSchema(schemaNode);
    ProcessingReport processingReport = schema.validate(instanceNode, false);

    return processingReport;
  }

  /**
   * Load JSON Schema from a resource
   *
   * @param resourceName
   * @return
   * @throws IOException
   * @throws URISyntaxException
   * @throws ProcessingException
   */
  private JsonSchema loadJsonSchemaFromResource(String resourceName)
    throws IOException, URISyntaxException, ProcessingException
  {
    ClassLoader classLoader = getClass().getClassLoader();
    String url = classLoader.getResource(resourceName).toString();
    URI schemaURI = new URI(url);

    return JsonSchemaFactory.byDefault().getJsonSchema(schemaURI.toString());
  }

  /**
   * Load a JSON Node from a resource
   *
   * @param resourceName
   * @return
   * @throws IOException
   * @throws URISyntaxException
   * @throws ProcessingException
   */
  private JsonNode loadJsonNodeFromResource(String resourceName)
    throws IOException, URISyntaxException, ProcessingException
  {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource(resourceName);

    return MAPPER.readTree(resource);
  }
}
