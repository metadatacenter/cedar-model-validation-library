package org.metadatacenter.model.validation;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ListProcessingReport;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import org.metadatacenter.model.validation.report.CedarValidationReport;
import org.metadatacenter.model.validation.report.ErrorItem;
import org.metadatacenter.model.validation.report.ValidationReport;
import org.metadatacenter.model.validation.report.WarningItem;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Optional;

public class CEDARModelValidator implements ModelValidator
{
  private static final ObjectMapper MAPPER = JacksonUtils.newMapper();
  private final JsonValidator jsonSchemaValidator;

  private static final String JSON_SCHEMA_PROPERTIES_FIELD_NAME = "properties";
  private static final String VALUE_CONSTRAINTS_FIELD_NAME = "_valueConstraints";
  private static final String ONTOLOGIES_CONSTRAINT_FIELD_NAME = "ontologies";
  private static final String BRANCHES_CONSTRAINT_FIELD_NAME = "branches";
  private static final String CLASSES_CONSTRAINT_FIELD_NAME = "classes";
  private static final String VALUE_SETS_CONSTRAINT_FIELD_NAME = "branches";

  private static final String JSON_LD_TYPE_FIELD_NAME = "@type";
  private static final String JSON_LD_VALUE_FIELD_NAME = "@value";
  private static final String JSON_LD_ID_FIELD_NAME = "@id";

  private static final String CEDAR_TEMPLATE_TYPE_URI = "https://schema.metadatacenter.org/core/Template";
  private static final String CEDAR_TEMPLATE_ELEMENT_TYPE_URI = "https://schema.metadatacenter.org/core/TemplateElement";
  private static final String CEDAR_TEMPLATE_FIELD_TYPE_URI = "https://schema.metadatacenter.org/core/TemplateField";
  private static final String CEDAR_STATIC_TEMPLATE_FIELD_TYPE_URI = "https://schema.metadatacenter.org/core/StaticTemplateField";

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
  private static final String TEMPLATE_PROPERTIES_FIELD_SCHEMA_RESOURCE_NAME = "templatePropertiesField.json";
  private static final String TEMPLATE_UI_FIELD_SCHEMA_RESOURCE_NAME = "templateUIField.json";
  private static final String TEMPLATE_REQUIRED_FIELD_SCHEMA_RESOURCE_NAME = "templateRequiredField.json";

  private static final String TEMPLATE_SCHEMA_RESOURCE_NAMES[] = {
    CORE_JSON_SCHEMA_TEMPLATE_FIELDS_SCHEMA_RESOURCE_NAME, TEMPLATE_JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME,
    TEMPLATE_JSON_LD_CONTEXT_FIELD_SCHEMA_RESOURCE_NAME, TEMPLATE_PROPERTIES_FIELD_SCHEMA_RESOURCE_NAME,
    TEMPLATE_UI_FIELD_SCHEMA_RESOURCE_NAME, TEMPLATE_REQUIRED_FIELD_SCHEMA_RESOURCE_NAME };

  private static final String CORE_JSON_SCHEMA_TEMPLATE_ELEMENT_FIELDS_SCHEMA_RESOURCE_NAME = "coreJSONSchemaTemplateElementFields.json";
  private static final String TEMPLATE_ELEMENT_JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME = "templateElementJSONLDTypeField.json";
  private static final String TEMPLATE_ELEMENT_JSON_LD_CONTEXT_FIELD_SCHEMA_RESOURCE_NAME = "templateElementJSONLDContextField.json";
  private static final String TEMPLATE_ELEMENT_PROPERTIES_FIELD_SCHEMA_RESOURCE_NAME = "templateElementPropertiesField.json";
  private static final String TEMPLATE_ELEMENT_UI_FIELD_SCHEMA_RESOURCE_NAME = "templateElementUIField.json";

  private static final String TEMPLATE_ELEMENT_SCHEMA_RESOURCE_NAMES[] = {
    CORE_JSON_SCHEMA_TEMPLATE_ELEMENT_FIELDS_SCHEMA_RESOURCE_NAME, TEMPLATE_ELEMENT_JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME,
    TEMPLATE_ELEMENT_JSON_LD_CONTEXT_FIELD_SCHEMA_RESOURCE_NAME, TEMPLATE_ELEMENT_PROPERTIES_FIELD_SCHEMA_RESOURCE_NAME,
    TEMPLATE_ELEMENT_UI_FIELD_SCHEMA_RESOURCE_NAME };

  private static final String CORE_JSON_SCHEMA_TEMPLATE_FIELD_FIELDS_SCHEMA_RESOURCE_NAME = "coreJSONSchemaTemplateFieldFields.json";
  private static final String TEMPLATE_FIELD_JSON_LD_ID_FIELD_SCHEMA_RESOURCE_NAME = "templateFieldJSONLDIDField.json";
  private static final String TEMPLATE_FIELD_JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME = "templateFieldJSONLDTypeField.json";
  private static final String TEMPLATE_FIELD_JSON_LD_CONTEXT_FIELD_SCHEMA_RESOURCE_NAME = "templateFieldJSONLDContextField.json";
  private static final String VALUE_CONSTRAINTS_FIELD_SCHEMA_RESOURCE_NAME = "valueConstraintsField.json";
  private static final String TEMPLATE_FIELD_SINGLE_VALUE_CONTENT_FIELD_SCHEMA_RESOURCE_NAME = "templateFieldSingleValueContent.json";
  private static final String TEMPLATE_FIELD_UI_FIELD_SCHEMA_RESOURCE_NAME = "templateFieldUIField.json";

  private static final String CORE_TEMPLATE_FIELD_SCHEMA_RESOURCE_NAMES[] = { CORE_JSON_SCHEMA_FIELDS_SCHEMA_RESOURCE_NAME,
    TEMPLATE_FIELD_JSON_LD_ID_FIELD_SCHEMA_RESOURCE_NAME, JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME,
    PROVENANCE_FIELDS_SCHEMA_RESOURCE_NAME};

  private static final String TEMPLATE_FIELD_SCHEMA_RESOURCE_NAMES[] = { CORE_JSON_SCHEMA_TEMPLATE_FIELD_FIELDS_SCHEMA_RESOURCE_NAME,
    TEMPLATE_FIELD_JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME, TEMPLATE_FIELD_JSON_LD_CONTEXT_FIELD_SCHEMA_RESOURCE_NAME,
    VALUE_CONSTRAINTS_FIELD_SCHEMA_RESOURCE_NAME, TEMPLATE_FIELD_SINGLE_VALUE_CONTENT_FIELD_SCHEMA_RESOURCE_NAME,
    TEMPLATE_FIELD_UI_FIELD_SCHEMA_RESOURCE_NAME };

  public CEDARModelValidator()
  {
    JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
    this.jsonSchemaValidator = factory.getValidator();
  }

  @Override
  public ValidationReport validateTemplate(JsonNode templateNode)
  {
    JsonPointer path = JsonPointer.compile("/");
    for (int resourceNameIndex = 0; resourceNameIndex < CORE_SCHEMA_RESOURCE_NAMES.length; resourceNameIndex++) {
      String resourceName = CORE_SCHEMA_RESOURCE_NAMES[resourceNameIndex];
      Optional<ProcessingReport> processingReport = validateArtifact(resourceName, templateNode, path);

      if (processingReport.isPresent())
        return newValidationReport(processingReport.get());
    }

    Optional<ProcessingReport> processingPropertiesReport = validateJSONSchemaPropertiesNode(templateNode, path);

    if (processingPropertiesReport.isPresent())
      return newValidationReport(processingPropertiesReport.get());

    for (int resourceNameIndex = 0; resourceNameIndex < TEMPLATE_SCHEMA_RESOURCE_NAMES.length; resourceNameIndex++) {
      String resourceName = TEMPLATE_SCHEMA_RESOURCE_NAMES[resourceNameIndex];
      Optional<ProcessingReport> processingReport = validateArtifact(resourceName, templateNode, path);

      if (processingReport.isPresent())
        return newValidationReport(processingReport.get());
    }

    return newEmptyValidationReport();
  }

  @Override
  public ValidationReport validateTemplateElement(JsonNode templateElementNode)
  {
    JsonPointer path = JsonPointer.compile("/");
    return validateTemplateElementNode(templateElementNode, path);
  }

  public ValidationReport validateTemplateElementNode(JsonNode templateElementNode, JsonPointer basePath)
  {
    for (int resourceNameIndex = 0; resourceNameIndex < CORE_SCHEMA_RESOURCE_NAMES.length; resourceNameIndex++) {
      String resourceName = CORE_SCHEMA_RESOURCE_NAMES[resourceNameIndex];
      Optional<ProcessingReport> processingReport = validateArtifact(resourceName, templateElementNode, basePath);

      if (processingReport.isPresent())
        return newValidationReport(processingReport.get());
    }

    Optional<ProcessingReport> processingPropertiesReport = validateJSONSchemaPropertiesNode(templateElementNode, basePath);

    if (processingPropertiesReport.isPresent())
      return newValidationReport(processingPropertiesReport.get());

    for (int resourceNameIndex = 0;
         resourceNameIndex < TEMPLATE_ELEMENT_SCHEMA_RESOURCE_NAMES.length; resourceNameIndex++) {
      String resourceName = TEMPLATE_ELEMENT_SCHEMA_RESOURCE_NAMES[resourceNameIndex];
      Optional<ProcessingReport> processingReport = validateArtifact(resourceName, templateElementNode, basePath);

      if (processingReport.isPresent())
        return newValidationReport(processingReport.get());
    }

    return newEmptyValidationReport();
  }

  private static CedarValidationReport newValidationReport(ProcessingReport processingReport) {
    CedarValidationReport validationReport = newEmptyValidationReport();
    for (ProcessingMessage processingMessage : processingReport) {
      LogLevel messageLevel = processingMessage.getLogLevel();
      if (messageLevel == LogLevel.ERROR) {
        ErrorItem errorItem = new ErrorItem(processingMessage.getMessage());
        validationReport.addError(errorItem);
      } else if (messageLevel == LogLevel.WARNING) {
        WarningItem warningItem = new WarningItem(processingMessage.getMessage());
        validationReport.addWarning(warningItem);
      }
    }
    return validationReport;
  }

  private static CedarValidationReport newEmptyValidationReport() {
    return CedarValidationReport.newEmptyReport();
  }

  @Override
  public ValidationReport validateTemplateField(JsonNode templateFieldNode)
      throws ProcessingException, IOException, URISyntaxException, IllegalArgumentException
  {
    JsonPointer path = JsonPointer.compile("/");
    return validateTemplateField(templateFieldNode, path);
  }

  public ValidationReport validateTemplateField(JsonNode templateFieldNode, JsonPointer basePath)
  {
    for (int resourceNameIndex = 0; resourceNameIndex < CORE_TEMPLATE_FIELD_SCHEMA_RESOURCE_NAMES.length; resourceNameIndex++) {
      String resourceName = CORE_TEMPLATE_FIELD_SCHEMA_RESOURCE_NAMES[resourceNameIndex];
      Optional<ProcessingReport> processingReport = validateArtifact(resourceName, templateFieldNode, basePath);

      if (processingReport.isPresent())
        return newValidationReport(processingReport.get());
    }

    for (int resourceNameIndex = 0;
         resourceNameIndex < TEMPLATE_FIELD_SCHEMA_RESOURCE_NAMES.length; resourceNameIndex++) {
      String resourceName = TEMPLATE_FIELD_SCHEMA_RESOURCE_NAMES[resourceNameIndex];
      Optional<ProcessingReport> processingReport = validateArtifact(resourceName, templateFieldNode, basePath);

      if (processingReport.isPresent())
        return newValidationReport(processingReport.get());
    }

    Optional<ProcessingReport> processingPropertiesReport = validateTemplateFieldPropertiesNode(templateFieldNode, basePath);
    if (processingPropertiesReport.isPresent()) {
      return newValidationReport(processingPropertiesReport.get());
    }

    return newEmptyValidationReport();
  }

  @Override
  public ValidationReport validateTemplateInstance(JsonNode templateInstanceNode, JsonNode templateSchema)
      throws URISyntaxException, IOException, ProcessingException {
    ProcessingReport processingReport = jsonSchemaValidate(templateSchema, templateInstanceNode);
    return newValidationReport(processingReport);
  }

  private Optional<ProcessingReport> validateJSONSchemaPropertiesNode(JsonNode artifactNode, JsonPointer basePath)
  {
    JsonNode jsonSchemaPropertiesNode = artifactNode.get(JSON_SCHEMA_PROPERTIES_FIELD_NAME);

    if (jsonSchemaPropertiesNode == null)
      return Optional.of(generateErrorProcessingReport("No JSON Schema properties field in artifact at path " + basePath));

    if (!jsonSchemaPropertiesNode.isObject())
      return Optional
        .of(generateErrorProcessingReport("Non-object JSON Schema properties field in artifact at path " + basePath));

    if (!jsonSchemaPropertiesNode.fieldNames().hasNext())
      return Optional
        .of(generateErrorProcessingReport("Empty JSON Schema properties field in artifact at path " + basePath));

    Iterator<String> fieldNames = jsonSchemaPropertiesNode.fieldNames();
    while (fieldNames.hasNext()) {
      String fieldName = fieldNames.next();
      JsonPointer tailPath = JsonPointer.compile("/properties/" + fieldName);
      JsonPointer currentPath = basePath.append(tailPath);

      JsonNode childNode = jsonSchemaPropertiesNode.get(fieldName);
      if (childNode.isNull()) {
        return Optional
          .of(generateErrorProcessingReport("Null value for JSON Schema properties field artifact at path " + currentPath));
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
            return Optional.of(generateErrorProcessingReport(
              "Non object value in array for JSON Schema properties field artifact at path " + arrayPath));
          } else if (arrayNode.isNull()) {
            return Optional.of(generateErrorProcessingReport(
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
        return Optional.of(generateErrorProcessingReport(
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
          .of(generateErrorProcessingReport("Null value for JSON-LD @type field in artifact at path " + currentPath));

      if (!jsonLDTypeFieldValueNode.isTextual())
        return Optional
          .of(generateErrorProcessingReport("Non textual value for JSON-LD @type field in artifact at path " + currentPath));

      String jsonLDArtifactType = jsonLDTypeFieldValueNode.asText();

      if (jsonLDArtifactType.equals(CEDAR_TEMPLATE_TYPE_URI)) {
        return Optional
          .of(generateErrorProcessingReport("Not expecting nested template in artifact at path " + currentPath));
      } else if (jsonLDArtifactType.equals(CEDAR_TEMPLATE_ELEMENT_TYPE_URI)) {
        Optional<ProcessingReport> report = validateInnerTemplateElementNode(jsonSchemaPropertiesValueNode, currentPath);

        if (report.isPresent())
          return report;
      } else if (jsonLDArtifactType.equals(CEDAR_TEMPLATE_FIELD_TYPE_URI)
          || jsonLDArtifactType.equals(CEDAR_STATIC_TEMPLATE_FIELD_TYPE_URI)) {
        Optional<ProcessingReport> report = validateInnerTemplateFieldNode(jsonSchemaPropertiesValueNode, currentPath);

        if (report.isPresent())
          return report;
      } else
        return Optional.of(generateErrorProcessingReport(
          "Unexpected nested artifact type " + jsonLDArtifactType + " in artifact at path " + currentPath));
    }
    return Optional.empty();
  }

  private Optional<ProcessingReport> validateInnerTemplateElementNode(ObjectNode templateElementNode, JsonPointer basePath) {
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

  private Optional<ProcessingReport> validateInnerTemplateFieldNode(ObjectNode templateFieldNode, JsonPointer basePath) {
    for (int resourceNameIndex = 0; resourceNameIndex < CORE_TEMPLATE_FIELD_SCHEMA_RESOURCE_NAMES.length; resourceNameIndex++) {
      String resourceName = CORE_TEMPLATE_FIELD_SCHEMA_RESOURCE_NAMES[resourceNameIndex];
      Optional<ProcessingReport> report = validateArtifact(resourceName, templateFieldNode, basePath);

      if (report.isPresent())
        return report;
    }

    for (int resourceNameIndex = 0;
         resourceNameIndex < TEMPLATE_FIELD_SCHEMA_RESOURCE_NAMES.length; resourceNameIndex++) {
      String resourceName = TEMPLATE_FIELD_SCHEMA_RESOURCE_NAMES[resourceNameIndex];
      Optional<ProcessingReport> report = validateArtifact(resourceName, templateFieldNode, basePath);

      if (report.isPresent())
        return report;
    }

    Optional<ProcessingReport> propertiesReport = validateTemplateFieldPropertiesNode(templateFieldNode, basePath);
    if (propertiesReport.isPresent()) {
      return propertiesReport;
    }

    return Optional.empty();
  }

  private Optional<ProcessingReport> validateTemplateFieldPropertiesNode(JsonNode templateFieldNode, JsonPointer basePath)
  {
    JsonNode propertiesNode = templateFieldNode.get(JSON_SCHEMA_PROPERTIES_FIELD_NAME);
    JsonPointer currentPath = basePath.append(JsonPointer.compile("/properties/"));

    Optional<ProcessingReport> report;
    if (hasValueConstraints(templateFieldNode)) {
      report = validateIdFieldIsRequired(propertiesNode, currentPath);
    } else {
      report = validateValueFieldIsRequired(propertiesNode, currentPath);
    }
    return report;
  }

  private Optional<ProcessingReport> validateIdFieldIsRequired(JsonNode propertiesNode, JsonPointer path) {
    ProcessingReport report = null;
    if (hasBoth(JSON_LD_ID_FIELD_NAME, JSON_LD_VALUE_FIELD_NAME, propertiesNode)) {
      report = generateErrorProcessingReport("object has invalid properties ([\"@value\"])");
    }
    if (hasMissing(JSON_LD_ID_FIELD_NAME, propertiesNode)) {
      report = generateErrorProcessingReport("object has missing required properties ([\"@id\"])");
    }
    return Optional.ofNullable(report);
  }

  private Optional<ProcessingReport> validateValueFieldIsRequired(JsonNode propertiesNode, JsonPointer path) {
    ProcessingReport report = null;
    if (hasBoth(JSON_LD_VALUE_FIELD_NAME, JSON_LD_ID_FIELD_NAME, propertiesNode)) {
      report = generateErrorProcessingReport("object has invalid properties ([\"@id\"])");
    }
    if (hasMissing(JSON_LD_VALUE_FIELD_NAME, propertiesNode)) {
      report = generateErrorProcessingReport("object has missing required properties ([\"@value\"])");
    }
    return Optional.ofNullable(report);
  }

  private boolean hasValueConstraints(JsonNode templateFieldNode)
  {
    JsonNode valueConstraintsNode = templateFieldNode.get(VALUE_CONSTRAINTS_FIELD_NAME);
    return hasOntologySources(valueConstraintsNode) ||
        hasBranchSources(valueConstraintsNode) ||
        hasClassSources(valueConstraintsNode) ||
        hasValueSetSources(valueConstraintsNode);
  }

  private boolean hasOntologySources(JsonNode valueConstraintsNode) {
    boolean hasSources = false;
    if (valueConstraintsNode.has(ONTOLOGIES_CONSTRAINT_FIELD_NAME)) {
      JsonNode ontologiesNode = valueConstraintsNode.get(ONTOLOGIES_CONSTRAINT_FIELD_NAME);
      if (hasElements(ontologiesNode)) {
        hasSources = true;
      }
    }
    return hasSources;
  }

  private boolean hasBranchSources(JsonNode valueConstraintsNode) {
    boolean hasSources = false;
    if (valueConstraintsNode.has(BRANCHES_CONSTRAINT_FIELD_NAME)) {
      JsonNode branchesNode = valueConstraintsNode.get(BRANCHES_CONSTRAINT_FIELD_NAME);
      if (hasElements(branchesNode)) {
        hasSources = true;
      }
    }
    return hasSources;
  }

  private boolean hasClassSources(JsonNode valueConstraintsNode) {
    boolean hasSources = false;
    if (valueConstraintsNode.has(CLASSES_CONSTRAINT_FIELD_NAME)) {
      JsonNode classesNode = valueConstraintsNode.get(CLASSES_CONSTRAINT_FIELD_NAME);
      if (hasElements(classesNode)) {
        hasSources = true;
      }
    }
    return hasSources;
  }

  private boolean hasValueSetSources(JsonNode valueConstraintsNode) {
    boolean hasSources = false;
    if (valueConstraintsNode.has(VALUE_SETS_CONSTRAINT_FIELD_NAME)) {
      JsonNode valueSetsNode = valueConstraintsNode.get(VALUE_SETS_CONSTRAINT_FIELD_NAME);
      if (hasElements(valueSetsNode)) {
        hasSources = true;
      }
    }
    return hasSources;
  }

  private static boolean hasBoth(String field1, String field2, JsonNode objectNode) {
    return objectNode.has(field1) && objectNode.has(field2);
  }

  private static boolean hasMissing(String field1, JsonNode objectNode) {
    return !objectNode.has(field1);
  }

  private static boolean hasElements(JsonNode node) {
    return (node.size() > 0) ? true : false;
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

  private ProcessingReport jsonSchemaValidate(JsonNode schemaNode, JsonNode instanceNode)
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
      return Optional.of(generateErrorProcessingReport("internal error at path " + path.toString() + ":" + e.getMessage()));
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

  private ProcessingReport generateErrorProcessingReport(String message)
  {
    ProcessingReport report = new ListProcessingReport();
    ProcessingMessage processingMessage = new ProcessingMessage();
    processingMessage.setMessage(message);
    try {
      report.error(processingMessage);
    } catch (ProcessingException e) {
      // TODO Not sure what to do here.
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
