package org.metadatacenter.model.validation;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ListProcessingReport;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import org.metadatacenter.model.core.CedarConstants;
import org.metadatacenter.model.core.CedarModelVocabulary;
import org.metadatacenter.model.validation.internal.ParsedProcessingMessage;
import org.metadatacenter.model.validation.internal.SchemaResources;
import org.metadatacenter.model.validation.report.CedarValidationReport;
import org.metadatacenter.model.validation.report.ErrorItem;
import org.metadatacenter.model.validation.report.ValidationReport;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

public class CedarValidator implements ModelValidator {

  private static final ObjectMapper MAPPER = JacksonUtils.newMapper();

  private static final String JSON_SCHEMA_PROPERTIES = "properties";
  private static final String JSON_SCHEMA_TYPE = "type";
  private static final String JSON_SCHEMA_ITEMS = "items";

  private static final String JSON_SCHEMA_OBJECT = "object";
  private static final String JSON_SCHEMA_ARRAY = "array";

  private static final String JSON_LD_TYPE = "@type";
  private static final String JSON_LD_VALUE = "@value";
  private static final String JSON_LD_ID = "@id";

  private final JsonPointer startingLocation;

  public CedarValidator() {
    this("/");
  }

  public CedarValidator(String startingLocation) {
    checkNotNull(startingLocation);
    this.startingLocation = JsonPointer.compile(startingLocation);
  }

  @Override
  public ValidationReport validateTemplate(JsonNode templateNode) throws IOException {
    CedarValidationReport report = CedarValidationReport.newEmptyReport();
    try {
      doTemplateValidation(templateNode, startingLocation);
    } catch (CedarModelValidationException thrownException) {
      collectErrorMessages(thrownException, report);
    }
    return report;
  }

  @Override
  public ValidationReport validateTemplateElement(JsonNode elementNode) throws IOException {
    CedarValidationReport report = CedarValidationReport.newEmptyReport();
    try {
      doElementValidation(elementNode, startingLocation);
    } catch (CedarModelValidationException thrownException) {
      collectErrorMessages(thrownException, report);
    }
    return report;
  }

  @Override
  public ValidationReport validateTemplateField(JsonNode fieldNode) throws IOException {
    CedarValidationReport report = CedarValidationReport.newEmptyReport();
    try {
      doFieldValidation(fieldNode, startingLocation);
    } catch (CedarModelValidationException thrownException) {
      collectErrorMessages(thrownException, report);
    }
    return report;
  }

  @Override
  public ValidationReport validateTemplateInstance(JsonNode templateInstance, JsonNode instanceSchema)
      throws IOException {
    CedarValidationReport report = CedarValidationReport.newEmptyReport();
    try {
      doInstanceValidation(templateInstance, instanceSchema, startingLocation);
    } catch (CedarModelValidationException thrownException) {
      collectErrorMessages(thrownException, report);
    }
    return report;
  }

  private static void doValidation(JsonNode schemaNode, JsonNode instanceNode, JsonPointer currentLocation) throws
      CedarModelValidationException {
    try {
      JsonSchema schema = JsonSchemaFactory.byDefault().getJsonSchema(schemaNode);
      ProcessingReport processingReport = schema.validate(instanceNode, false);
      if (!processingReport.isSuccess()) {
        throw newCedarModelValidationException(processingReport, currentLocation);
      }
    } catch (ProcessingException e) {
      throw newCedarModelValidationException(e.getProcessingMessage());
    }
  }

  private void doTemplateValidation(JsonNode templateNode, JsonPointer currentLocation)
      throws CedarModelValidationException, IOException {
    validateNodeStructureAgainstTemplateSchema(templateNode, currentLocation);
    checkUserSpecifiedPropertiesMemberFields(templateNode, currentLocation);
  }

  private void doElementValidation(JsonNode elementNode, JsonPointer currentLocation)
      throws CedarModelValidationException, IOException {
    validateNodeStructureAgainstElementSchema(elementNode, currentLocation);
    checkUserSpecifiedPropertiesMemberFields(elementNode, currentLocation);
  }

  private void doFieldValidation(JsonNode fieldNode, JsonPointer currentLocation)
      throws CedarModelValidationException, IOException {
    validateNodeStructureAgainstFieldSchema(fieldNode, currentLocation);
    checkCorrectFieldForConstrainedValue(fieldNode, currentLocation);
  }

  private void validateNodeStructureAgainstFieldSchema(JsonNode fieldNode, JsonPointer currentLocation) throws
      CedarModelValidationException, IOException {
    if (isSingleDataTemplateField(fieldNode)) {
      validateNodeStructureAgainstSingleDataFieldSchema(fieldNode, currentLocation);
    } else if (isMultiDataTemplateField(fieldNode)) {
      validateNodeStructureAgainstMultiDataFieldSchema(fieldNode, currentLocation);
    } else if (isStaticTemplateField(fieldNode)) {
      validateNodeStructureAgainstStaticFieldSchema(fieldNode, currentLocation);
    } else {
      checkJsonLdTypeNodeExists(fieldNode, currentLocation);
      checkJsonSchemaTypeNodeExists(fieldNode, currentLocation);
    }
  }

  private void doInstanceValidation(JsonNode templateInstance, JsonNode instanceSchema, JsonPointer currentLocation)
      throws CedarModelValidationException, IOException {
    doValidation(instanceSchema, templateInstance, currentLocation);
  }

  private void checkUserSpecifiedPropertiesMemberFields(JsonNode artifactNode, JsonPointer currentLocation)
      throws CedarModelValidationException, IOException {
    JsonNode propertiesNode = artifactNode.get(JSON_SCHEMA_PROPERTIES);
    for (Iterator<String> iter = propertiesNode.fieldNames(); iter.hasNext(); ) {
      String propertiesMemberField = iter.next();
      if (isUserSpecifiedField(propertiesMemberField)) {
        JsonNode propertiesMemberNode = propertiesNode.get(propertiesMemberField);
        JsonPointer propertiesMemberPointer = createJsonPointer(currentLocation, getPropertiesMemberPath
            (propertiesMemberField));
        checkPropertiesMemberField(propertiesMemberNode, propertiesMemberPointer);
      }
    }
  }

  private void checkPropertiesMemberField(JsonNode propertiesMemberNode, JsonPointer propertiesMemberPointer)
      throws CedarModelValidationException, IOException {
    if (isObjectNode(propertiesMemberNode)) {
      checkAndValidateTemplateOrField(propertiesMemberNode, propertiesMemberPointer);
    } else if (isArrayNode(propertiesMemberNode)) {
      JsonNode arrayItem = propertiesMemberNode.get(JSON_SCHEMA_ITEMS);
      JsonPointer arrayItemPath = createJsonPointer(propertiesMemberPointer, "/items");
      checkAndValidateTemplateOrField(arrayItem, arrayItemPath);
    }
  }

  private void checkAndValidateTemplateOrField(JsonNode propertiesMemberNode, JsonPointer currentLocation)
      throws CedarModelValidationException, IOException {
    if (isTemplateElement(propertiesMemberNode)) {
      checkUserSpecifiedPropertiesMemberFields(propertiesMemberNode, currentLocation);
    } else {
      checkCorrectFieldForConstrainedValue(propertiesMemberNode, currentLocation);
    }
  }

  private static void checkJsonLdTypeNodeExists(JsonNode node, JsonPointer currentLocation)
      throws CedarModelValidationException {
    JsonNode typeNode = node.path(JSON_LD_TYPE);
    if (typeNode.isMissingNode()) {
      ProcessingMessage message = createErrorMessage("object has missing required properties (['@type'])");
      throw newCedarModelValidationException(message, currentLocation);
    }
  }

  private static void checkJsonSchemaTypeNodeExists(JsonNode node, JsonPointer currentLocation)
      throws CedarModelValidationException {
    JsonNode typeNode = node.path(JSON_SCHEMA_TYPE);
    if (typeNode.isMissingNode()) {
      ProcessingMessage message = createErrorMessage("object has missing required properties (['type'])");
      throw newCedarModelValidationException(message, currentLocation);
    }
  }

  private void checkCorrectFieldForConstrainedValue(JsonNode fieldNode, JsonPointer currentPath)
      throws CedarModelValidationException, IOException {
    if (isSingleDataTemplateField(fieldNode)) {
      JsonNode propertiesNode = fieldNode.get(JSON_SCHEMA_PROPERTIES);
      JsonPointer propertiesPointer = createJsonPointer(currentPath, "/properties");
      JsonNode valueConstraintsNode = fieldNode.get(CedarModelVocabulary.VALUE_CONSTRAINTS);
      if (hasConstraintSources(valueConstraintsNode)) {
        validateConstrainedField(propertiesNode, propertiesPointer);
      } else {
        validateNonConstrainedField(propertiesNode, propertiesPointer);
      }
    }
  }

  private void validateNodeStructureAgainstTemplateSchema(JsonNode templateNode, JsonPointer currentLocation)
      throws CedarModelValidationException, IOException {
    validateArtifact(SchemaResources.TEMPLATE_SCHEMA, templateNode, currentLocation);
  }

  private void validateNodeStructureAgainstElementSchema(JsonNode templateElementNode, JsonPointer currentLocation)
      throws CedarModelValidationException, IOException {
    validateArtifact(SchemaResources.ELEMENT_SCHEMA, templateElementNode, currentLocation);
  }

  private void validateNodeStructureAgainstSingleDataFieldSchema(JsonNode fieldNode, JsonPointer currentLocation)
      throws CedarModelValidationException, IOException {
    validateArtifact(SchemaResources.SINGLE_DATA_FIELD_SCHEMA, fieldNode, currentLocation);
  }

  private void validateNodeStructureAgainstMultiDataFieldSchema(JsonNode fieldNode, JsonPointer currentLocation)
      throws CedarModelValidationException, IOException {
    validateArtifact(SchemaResources.MULTI_DATA_FIELD_SCHEMA, fieldNode, currentLocation);
  }

  private void validateNodeStructureAgainstStaticFieldSchema(JsonNode fieldNode, JsonPointer currentLocation)
      throws CedarModelValidationException, IOException {
    validateArtifact(SchemaResources.STATIC_FIELD_SCHEMA, fieldNode, currentLocation);
  }

  private void validateArtifact(String schemaResourceLocation, JsonNode artifactNode, JsonPointer currentLocation)
      throws CedarModelValidationException, IOException {
    final JsonNode jsonSchemaNode = loadSchemaFromFile(schemaResourceLocation);
    doValidation(jsonSchemaNode, artifactNode, currentLocation);
  }

  private void validateConstrainedField(JsonNode propertiesNode, JsonPointer currentLocation)
      throws CedarModelValidationException, IOException {
    ListProcessingReport errorReport = new ListProcessingReport(LogLevel.DEBUG, LogLevel.NONE);
    try {
      if (hasBoth(JSON_LD_ID, JSON_LD_VALUE, propertiesNode)) {
        ProcessingMessage message = createErrorMessage("object has invalid properties (['@value'])");
        errorReport.error(message);
      }
      if (hasMissing(JSON_LD_ID, propertiesNode)) {
        ProcessingMessage message = createErrorMessage("object has missing required properties (['@id'])");
        errorReport.error(message);
      }
      if (!errorReport.isSuccess()) {
        throw newCedarModelValidationException(errorReport, currentLocation);
      }
    } catch (ProcessingException e) {
      throw new IOException(e.getMessage(), e);
    }
  }

  private void validateNonConstrainedField(JsonNode propertiesNode, JsonPointer currentLocation)
      throws CedarModelValidationException, IOException {
    ListProcessingReport errorReport = new ListProcessingReport(LogLevel.DEBUG, LogLevel.NONE);
    try {
      if (hasBoth(JSON_LD_VALUE, JSON_LD_ID, propertiesNode)) {
        ProcessingMessage message = createErrorMessage("object has invalid properties (['@id'])");
        errorReport.error(message);
      }
      if (hasMissing(JSON_LD_VALUE, propertiesNode)) {
        ProcessingMessage message = createErrorMessage("object has missing required properties (['@value'])");
        errorReport.error(message);
      }
      if (!errorReport.isSuccess()) {
        throw newCedarModelValidationException(errorReport, currentLocation);
      }
    } catch (ProcessingException e) {
      throw new IOException(e.getMessage(), e);
    }
  }

  private void collectErrorMessages(CedarModelValidationException exception, final CedarValidationReport report) {
    Multimap<JsonPointer, ProcessingMessage> errorDetails = exception.getDetails();
    for (JsonPointer errorLocation : errorDetails.keySet()) {
      Collection<ProcessingMessage> processingMessages = errorDetails.get(errorLocation);
      for (ProcessingMessage processingMessage : processingMessages) {
        final LogLevel messageLevel = processingMessage.getLogLevel();
        if (messageLevel == LogLevel.ERROR) {
          ParsedProcessingMessage parsedMessage = new ParsedProcessingMessage(processingMessage);
          for (ParsedProcessingMessage.ReportItem reportItem : parsedMessage.getReportItems()) {
            ErrorItem errorItem = createErrorItem(errorLocation,
                reportItem.getMessage(),
                reportItem.getLocation(),
                reportItem.getSchemaResource(),
                reportItem.getSchemaPointer());
            report.addError(errorItem);
          }
        }
      }
    }
  }

  /*
   * Private helper methods
   */

  private ErrorItem createErrorItem(JsonPointer baseLocation, String message,
                                    String location, String schemaResource, String
                                        schemaPointer) {
    ErrorItem errorItem = new ErrorItem(message, createLocation(baseLocation.toString(), location));
    errorItem.addAdditionalInfo("schemaPointer", schemaPointer);
    errorItem.addAdditionalInfo("schemaFile", schemaResource);
    return errorItem;
  }

  private static String createLocation(String baseLocation, String relativeLocation) {
    String absoluteLocation = relativeLocation;
    if (!baseLocation.equals("/")) {
      absoluteLocation = baseLocation.toString();
      if (!Strings.isNullOrEmpty(relativeLocation)) {
        absoluteLocation += relativeLocation;
      }
    }
    return absoluteLocation;
  }

  private static ProcessingMessage createErrorMessage(String message) {
    ProcessingMessage processingMessage = new ProcessingMessage();
    processingMessage.setLogLevel(LogLevel.ERROR);
    processingMessage.setMessage(message);
    return processingMessage;
  }

  private static boolean hasConstraintSources(JsonNode valueConstraintsNode) {
    return hasOntologySources(valueConstraintsNode) ||
        hasBranchSources(valueConstraintsNode) ||
        hasClassSources(valueConstraintsNode) ||
        hasValueSetSources(valueConstraintsNode);
  }

  private static boolean hasOntologySources(JsonNode valueConstraintsNode) {
    boolean hasSources = false;
    if (valueConstraintsNode.has(CedarModelVocabulary.ONTOLOGIES)) {
      JsonNode ontologiesNode = valueConstraintsNode.get(CedarModelVocabulary.ONTOLOGIES);
      if (hasElements(ontologiesNode)) {
        hasSources = true;
      }
    }
    return hasSources;
  }

  private static boolean hasBranchSources(JsonNode valueConstraintsNode) {
    boolean hasSources = false;
    if (valueConstraintsNode.has(CedarModelVocabulary.BRANCHES)) {
      JsonNode branchesNode = valueConstraintsNode.get(CedarModelVocabulary.BRANCHES);
      if (hasElements(branchesNode)) {
        hasSources = true;
      }
    }
    return hasSources;
  }

  private static boolean hasClassSources(JsonNode valueConstraintsNode) {
    boolean hasSources = false;
    if (valueConstraintsNode.has(CedarModelVocabulary.CLASSES)) {
      JsonNode classesNode = valueConstraintsNode.get(CedarModelVocabulary.CLASSES);
      if (hasElements(classesNode)) {
        hasSources = true;
      }
    }
    return hasSources;
  }

  private static boolean hasValueSetSources(JsonNode valueConstraintsNode) {
    boolean hasSources = false;
    if (valueConstraintsNode.has(CedarModelVocabulary.VALUE_SETS)) {
      JsonNode valueSetsNode = valueConstraintsNode.get(CedarModelVocabulary.VALUE_SETS);
      if (hasElements(valueSetsNode)) {
        hasSources = true;
      }
    }
    return hasSources;
  }

  private static boolean isUserSpecifiedField(String propertiesMemberField) {
    return !CedarModelVocabulary.CommonPropertiesInnerFields.contains(propertiesMemberField);
  }

  private static boolean hasBoth(String field1, String field2, JsonNode objectNode) {
    return objectNode.has(field1) && objectNode.has(field2);
  }

  private static boolean hasMissing(String field1, JsonNode objectNode) {
    return !objectNode.has(field1);
  }

  private static boolean hasElements(JsonNode node) {
    return node.size() > 0;
  }

  private static CedarModelValidationException newCedarModelValidationException(ProcessingReport report,
                                                                                JsonPointer currentLocation) {
    CedarModelValidationException exception = new CedarModelValidationException();
    exception.addProcessingReport(report, currentLocation);
    return exception;
  }

  private static CedarModelValidationException newCedarModelValidationException(ProcessingMessage message) {
    CedarModelValidationException exception = new CedarModelValidationException();
    exception.addProcessingMessage(message);
    return exception;
  }

  private static CedarModelValidationException newCedarModelValidationException(ProcessingMessage message,
                                                                                JsonPointer currentLocation) {
    CedarModelValidationException exception = new CedarModelValidationException();
    exception.addProcessingMessage(message, currentLocation);
    return exception;
  }

  private JsonNode loadSchemaFromFile(String resourceName) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource(resourceName);
    return MAPPER.readTree(resource);
  }

  private static boolean isTemplateElement(JsonNode memberNode) {
    return memberNode.path(JSON_LD_TYPE).asText().equals(CedarConstants.TEMPLATE_ELEMENT_TYPE_URI);
  }

  private static boolean isStaticTemplateField(JsonNode memberNode) {
    return memberNode.path(JSON_LD_TYPE).asText().equals(CedarConstants.STATIC_TEMPLATE_FIELD_TYPE_URI)
        && memberNode.path(JSON_SCHEMA_TYPE).asText().equals(JSON_SCHEMA_OBJECT);
  }

  private static boolean isSingleDataTemplateField(JsonNode memberNode) {
    return memberNode.path(JSON_LD_TYPE).asText().equals(CedarConstants.TEMPLATE_FIELD_TYPE_URI)
        && memberNode.path(JSON_SCHEMA_TYPE).asText().equals(JSON_SCHEMA_OBJECT);
  }

  private static boolean isMultiDataTemplateField(JsonNode memberNode) {
    return memberNode.path(JSON_SCHEMA_TYPE).asText().equals(JSON_SCHEMA_ARRAY);
  }

  private static String getPropertiesMemberPath(String fieldName) {
    return String.format("/%s/%s", JSON_SCHEMA_PROPERTIES, fieldName);
  }

  private static boolean isObjectNode(JsonNode node) {
    String type = node.path(JSON_SCHEMA_TYPE).asText();
    return type.equals("object");
  }

  private static boolean isArrayNode(JsonNode node) {
    String type = node.path(JSON_SCHEMA_TYPE).asText();
    return type.equals("array");
  }

  private static JsonPointer createJsonPointer(JsonPointer basePointer, String relativeDestination) {
    JsonPointer destinationPointer = JsonPointer.compile(relativeDestination);
    return basePointer.append(destinationPointer);
  }
}