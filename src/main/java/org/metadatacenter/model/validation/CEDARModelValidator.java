package org.metadatacenter.model.validation;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ConsoleProcessingReport;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Optional;

public class CEDARModelValidator
{
  private static final ObjectMapper MAPPER = JacksonUtils.newMapper();
  private final JsonValidator jsonSchemaValidator;

  private static final String JSON_SCHEMA_PROPERTIES_FIELD_NAME = "properties";

  private static final String JSON_LD_TYPE_FIELD_NAME = "@type";
  private static final String JSON_LD_ID_FIELD_NAME = "@id";

  private static final String CEDAR_TEMPLATE_TYPE_URI = "https://schema.metadatacenter.org/core/Template";
  private static final String CEDAR_TEMPLATE_ELEMENT_TYPE_URI = "https://schema.metadatacenter.org/core/TemplateElement";
  private static final String CEDAR_TEMPLATE_FIELD_TYPE_URI = "https://schema.metadatacenter.org/core/TemplateField";

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
  {
    JsonPointer path = JsonPointer.compile("/");
    for (int resourceNameIndex = 0; resourceNameIndex < CORE_SCHEMA_RESOURCE_NAMES.length; resourceNameIndex++) {
      String resourceName = CORE_SCHEMA_RESOURCE_NAMES[resourceNameIndex];
      Optional<ProcessingReport> report = validateArtifact(resourceName, templateNode, path);

      if (report.isPresent())
        return report;
    }

    Optional<ProcessingReport> propertiesReport = validateJSONSchemaPropertiesNode(templateNode, path);

    if (propertiesReport.isPresent())
      return propertiesReport;

    for (int resourceNameIndex = 0; resourceNameIndex < TEMPLATE_SCHEMA_RESOURCE_NAMES.length; resourceNameIndex++) {
      String resourceName = TEMPLATE_SCHEMA_RESOURCE_NAMES[resourceNameIndex];
      Optional<ProcessingReport> report = validateArtifact(resourceName, templateNode, path);

      if (report.isPresent())
        return report;
    }

    return Optional.empty();
  }

  public Optional<ProcessingReport> validateTemplateElementNode(JsonNode templateElementNode)
  {
    JsonPointer path = JsonPointer.compile("/");
    return validateTemplateElementNode(templateElementNode, path);
  }

  public Optional<ProcessingReport> validateTemplateElementNode(JsonNode templateElementNode, JsonPointer basePath)
  {
    for (int resourceNameIndex = 0; resourceNameIndex < CORE_SCHEMA_RESOURCE_NAMES.length; resourceNameIndex++) {
      String resourceName = CORE_SCHEMA_RESOURCE_NAMES[resourceNameIndex];
      Optional<ProcessingReport> report = validateArtifact(resourceName, templateElementNode, basePath);

      if (report.isPresent())
        return report;
    }

    Optional<ProcessingReport> propertiesReport = validateJSONSchemaPropertiesNode(templateElementNode, basePath);

    if (propertiesReport.isPresent())
      return propertiesReport;

    for (int resourceNameIndex = 0;
         resourceNameIndex < TEMPLATE_ELEMENT_SCHEMA_RESOURCE_NAMES.length; resourceNameIndex++) {
      String resourceName = TEMPLATE_ELEMENT_SCHEMA_RESOURCE_NAMES[resourceNameIndex];
      Optional<ProcessingReport> report = validateArtifact(resourceName, templateElementNode, basePath);

      if (report.isPresent())
        return report;
    }

    return Optional.empty();
  }

  public Optional<ProcessingReport> validateTemplateFieldNode(JsonNode templateFieldNode, JsonPointer path)
  {
    for (int resourceNameIndex = 0; resourceNameIndex < CORE_SCHEMA_RESOURCE_NAMES.length; resourceNameIndex++) {
      String resourceName = CORE_SCHEMA_RESOURCE_NAMES[resourceNameIndex];
      Optional<ProcessingReport> report = validateArtifact(resourceName, templateFieldNode, path);

      if (report.isPresent())
        return report;
    }

    for (int resourceNameIndex = 0;
         resourceNameIndex < TEMPLATE_FIELD_SCHEMA_RESOURCE_NAMES.length; resourceNameIndex++) {
      String resourceName = TEMPLATE_FIELD_SCHEMA_RESOURCE_NAMES[resourceNameIndex];
      Optional<ProcessingReport> report = validateArtifact(resourceName, templateFieldNode, path);

      if (report.isPresent())
        return report;
    }

    return Optional.empty();
  }

  private Optional<ProcessingReport> validateJSONSchemaPropertiesNode(JsonNode artifactNode, JsonPointer basePath)
  {
    JsonNode jsonSchemaPropertiesNode = artifactNode.get(JSON_SCHEMA_PROPERTIES_FIELD_NAME);

    if (jsonSchemaPropertiesNode == null)
      return Optional.of(generateProcessingReport("No JSON Schema properties field in artifact at path " + basePath));

    if (!jsonSchemaPropertiesNode.isObject())
      return Optional
        .of(generateProcessingReport("Non-object JSON Schema properties field in artifact at path " + basePath));

    if (!jsonSchemaPropertiesNode.fieldNames().hasNext())
      return Optional
        .of(generateProcessingReport("Empty JSON Schema properties field in artifact at path " + basePath));

    Iterator<String> fieldNames = jsonSchemaPropertiesNode.fieldNames();
    while (fieldNames.hasNext()) {
      String fieldName = fieldNames.next();
      JsonPointer tailPath = JsonPointer.compile("/properties/" + fieldName);
      JsonPointer currentPath = basePath.append(tailPath);

      JsonNode childNode = jsonSchemaPropertiesNode.get(fieldName);
      if (childNode.isNull()) {
        return Optional
          .of(generateProcessingReport("Null value for JSON Schema properties field artifact at path " + currentPath));
      } else if (childNode.isObject()) {

        ObjectNode jsonSchemaPropertiesValueNode = (ObjectNode)childNode;
        Optional<ProcessingReport> report = validateJSONSchemaPropertiesValueNode(jsonSchemaPropertiesValueNode,
          currentPath);

        if (report.isPresent())
          return report;
      } else if (childNode.isArray()) {
        int arrayIndex = 0;
        for (JsonNode arrayNode : childNode) {
          JsonPointer arrayPath = currentPath.append(JsonPointer.compile("[" + arrayIndex + "]"));
          if (!arrayNode.isObject()) {
            return Optional.of(generateProcessingReport(
              "Non object value in array for JSON Schema properties field artifact at path " + arrayPath));
          } else if (arrayNode.isNull()) {
            return Optional.of(generateProcessingReport(
              "Null value in array for JSON Schema properties field artifact at path " + arrayPath));
          } else {
            ObjectNode jsonSchemaPropertiesValueNode = (ObjectNode)arrayNode;
            Optional<ProcessingReport> report = validateJSONSchemaPropertiesValueNode(jsonSchemaPropertiesValueNode,
              arrayPath);

            if (report.isPresent())
              return report;
          }
          arrayIndex++;
        }
      } else
        return Optional.of(generateProcessingReport(
          "Non object or array value for JSON Schema properties field in artifact at path " + currentPath));
    }
    return Optional.empty();
  }

  private Optional<ProcessingReport> validateJSONSchemaPropertiesValueNode(ObjectNode jsonSchemaPropertiesValueNode,
    JsonPointer currentPath)
  {
    // JSON Schema validation handles other property fields, such as provenance fields and the like.
    if (jsonSchemaPropertiesValueNode.has(JSON_LD_TYPE_FIELD_NAME)) {
      JsonNode jsonLDTypeFieldValueNode = jsonSchemaPropertiesValueNode.get(JSON_LD_TYPE_FIELD_NAME);
      if (jsonLDTypeFieldValueNode == null)
        return Optional
          .of(generateProcessingReport("Null value for JSON-LD @type field in artifact at path " + currentPath));

      if (!jsonLDTypeFieldValueNode.isTextual())
        return Optional
          .of(generateProcessingReport("Non textual value for JSON-LD @type field in artifact at path " + currentPath));

      String jsonLDArtifactType = jsonLDTypeFieldValueNode.asText();

      if (jsonLDArtifactType.equals(CEDAR_TEMPLATE_TYPE_URI)) {
        return Optional
          .of(generateProcessingReport("Not expecting nested template in artifact at path " + currentPath));
      } else if (jsonLDArtifactType.equals(CEDAR_TEMPLATE_ELEMENT_TYPE_URI)) {
        Optional<ProcessingReport> report = validateTemplateElementNode(jsonSchemaPropertiesValueNode, currentPath);

        if (report.isPresent())
          return report;
      } else if (jsonLDArtifactType.equals(CEDAR_TEMPLATE_FIELD_TYPE_URI)) {
        Optional<ProcessingReport> report = validateTemplateFieldNode(jsonSchemaPropertiesValueNode, currentPath);

        if (report.isPresent())
          return report;
      } else
        return Optional.of(generateProcessingReport(
          "Unexpected nested artifact type " + jsonLDArtifactType + " in artifact at path " + currentPath));
    }
    return Optional.empty();
  }

  public Optional<ProcessingReport> validateTemplateFieldNode(JsonNode templateFieldNode)
    throws ProcessingException, IOException, URISyntaxException, IllegalArgumentException
  {
    JsonPointer path = JsonPointer.compile("/");
    return validateTemplateFieldNode(templateFieldNode, path);
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
   * Validate a JSON node against a JSON Schema specification loaded from a resource
   *
   * @param schemaResourceName The name of a resource containing a JSON Schema document
   * @param instanceNode       The instance to validate against the schema
   * @return A processing report
   * @throws ProcessingException If a processing exception occurs during processing
   * @throws IOException         If an IO exception occurs during processing
   * @throws URISyntaxException  If a URI syntax exception occurs during processing
   */
  private Optional<ProcessingReport> validateArtifact(String schemaResourceName, JsonNode instanceNode,
    JsonPointer path)
  {
    try {
      JsonNode jsonSchemaNode = loadJsonNodeFromResource(schemaResourceName);
      ProcessingReport report = jsonSchemaValidate(jsonSchemaNode, instanceNode);

      if (!report.isSuccess()) {
        ProcessingMessage processingMessage = createProcessingMessage(
          "Error validating artifact at path " + path.toString() + " against CEDAR sub-schema " + schemaResourceName);
        report.error(processingMessage);
        return Optional.of(report);
      } else
        return Optional.empty();
    } catch (ProcessingException | URISyntaxException | IOException e) {
      return Optional.of(generateProcessingReport("internal error at path " + path.toString() + ":" + e.getMessage()));
    }
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

  private ProcessingReport generateProcessingReport(String message)
  {
    ProcessingReport report = new ConsoleProcessingReport();

    if (!report.isSuccess()) {
      ProcessingMessage processingMessage = new ProcessingMessage();
      processingMessage.setMessage(message);
      try {
        report.error(processingMessage);
      } catch (ProcessingException e) {
        // TODO Not sure what to do here.
      }
    }

    return report;
  }

  private ProcessingMessage createProcessingMessage(String messageText)
  {
    ProcessingMessage processingMessage = new ProcessingMessage();
    processingMessage.setMessage(messageText);

    return processingMessage;
  }
}
