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
import org.metadatacenter.model.validation.internal.ParsedProcessingMessage;
import org.metadatacenter.model.validation.internal.SchemaResources;
import org.metadatacenter.model.validation.report.CedarValidationReport;
import org.metadatacenter.model.validation.report.ErrorItem;
import org.metadatacenter.model.validation.report.ValidationReport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

public class CedarValidator implements ModelValidator {

  private static final ObjectMapper MAPPER = JacksonUtils.newMapper();

  private static final String JSON_SCHEMA_PROPERTIES_FIELD_NAME = "properties";
  private static final String VALUE_CONSTRAINTS_FIELD_NAME = "_valueConstraints";
  private static final String ONTOLOGIES_CONSTRAINT_FIELD_NAME = "ontologies";
  private static final String BRANCHES_CONSTRAINT_FIELD_NAME = "branches";
  private static final String CLASSES_CONSTRAINT_FIELD_NAME = "classes";
  private static final String VALUE_SETS_CONSTRAINT_FIELD_NAME = "branches";

  private static final String JSON_LD_TYPE_FIELD_NAME = "@type";
  private static final String JSON_LD_VALUE_FIELD_NAME = "@value";
  private static final String JSON_LD_ID_FIELD_NAME = "@id";

  private static final String CEDAR_TEMPLATE_ELEMENT_TYPE_URI = "https://schema.metadatacenter.org/core/TemplateElement";
  private static final String CEDAR_TEMPLATE_FIELD_TYPE_URI = "https://schema.metadatacenter.org/core/TemplateField";
  private static final String CEDAR_STATIC_TEMPLATE_FIELD_TYPE_URI = "https://schema.metadatacenter.org/core/StaticTemplateField";

  private final JsonPointer startingLocation;

  public CedarValidator() {
    this("/");
  }

  public CedarValidator(@Nonnull String startingLocation) {
    checkNotNull(startingLocation);
    this.startingLocation = JsonPointer.compile(startingLocation);
  }

  @Override
  public ValidationReport validateTemplate(JsonNode templateNode) throws IOException {
    CedarValidationReport report = CedarValidationReport.newEmptyReport();
    try {
      doTemplateValidation(templateNode);
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

  private void doTemplateValidation(JsonNode templateNode) throws CedarModelValidationException, IOException {
    validateUserSpecifiedPropertiesMemberFields(templateNode, startingLocation);
    validateNodeStructureAgainstTemplateSchema(templateNode, startingLocation);
  }

  private void doElementValidation(JsonNode elementNode, JsonPointer currentLocation)
      throws CedarModelValidationException, IOException {
    validateUserSpecifiedPropertiesMemberFields(elementNode, currentLocation);
    validateNodeStructureAgainstElementSchema(elementNode, currentLocation);
  }

  private void doFieldValidation(JsonNode fieldNode, JsonPointer currentLocation)
      throws CedarModelValidationException, IOException {
    validateValueConstrainedPropertiesMemberFields(fieldNode, currentLocation);
    validateNodeStructureAgainstFieldSchema(fieldNode, currentLocation);
  }

  private void doInstanceValidation(JsonNode templateInstance, JsonNode instanceSchema, JsonPointer currentLocation)
      throws CedarModelValidationException, IOException {
    doValidation(instanceSchema, templateInstance, currentLocation);
  }

  private void validateUserSpecifiedPropertiesMemberFields(JsonNode artifactNode, JsonPointer currentLocation)
      throws CedarModelValidationException, IOException {
    JsonNode propertiesNode = artifactNode.path(JSON_SCHEMA_PROPERTIES_FIELD_NAME);
    if (!propertiesNode.isMissingNode()) {
      for (Iterator<String> iter = propertiesNode.fieldNames(); iter.hasNext(); ) {
        String propertiesMemberField = iter.next();
        JsonNode propertiesMemberNode = propertiesNode.get(propertiesMemberField);
        JsonPointer propertiesMemberPointer = createJsonPointer(currentLocation, getPropertiesMemberPath(propertiesMemberField));
        validatePropertiesMemberField(propertiesMemberNode, propertiesMemberPointer);
      }
    }
  }

  private void validatePropertiesMemberField(JsonNode propertiesMemberNode, JsonPointer propertiesMemberPointer)
      throws CedarModelValidationException, IOException {
    if (isObjectNode(propertiesMemberNode)) {
      checkAndValidateTemplateOrField(propertiesMemberNode, propertiesMemberPointer);
    } else if (isArrayNode(propertiesMemberNode)) {
      for (int index = 0; index < propertiesMemberNode.size(); index++) {
        JsonNode arrayItemNode = propertiesMemberNode.get(index);
        JsonPointer arrayItemPath = createJsonPointer(propertiesMemberPointer, getArrayItemPath(index));
        if (isObjectNode(arrayItemNode)) {
          checkAndValidateTemplateOrField(propertiesMemberNode, arrayItemPath);
        }
      }
    }
  }

  private void checkAndValidateTemplateOrField(JsonNode propertiesMemberNode, JsonPointer currentLocation)
      throws CedarModelValidationException, IOException {
    if (isTemplateElement(propertiesMemberNode)) {
      doElementValidation(propertiesMemberNode, currentLocation);
    } else if (isStaticTemplateElement(propertiesMemberNode)) {
      doElementValidation(propertiesMemberNode, currentLocation);
    } else if (isTemplateField(propertiesMemberNode)) {
      doFieldValidation(propertiesMemberNode, currentLocation);
    }
  }

  private void validateValueConstrainedPropertiesMemberFields(JsonNode fieldNode, JsonPointer currentPath)
      throws CedarModelValidationException, IOException {
    JsonNode propertiesNode = fieldNode.path(JSON_SCHEMA_PROPERTIES_FIELD_NAME);
    if (!propertiesNode.isMissingNode()) {
      JsonPointer propertiesPointer = createJsonPointer(currentPath, "/properties");
      JsonNode valueConstraintsNode = fieldNode.path(VALUE_CONSTRAINTS_FIELD_NAME);
      if (!valueConstraintsNode.isMissingNode()) {
        if (hasConstraintSources(valueConstraintsNode)) {
          validateConstrainedField(propertiesNode, propertiesPointer);
        } else {
          validateNonConstrainedField(propertiesNode, propertiesPointer);
        }
      }
    }
  }

  private void validateNodeStructureAgainstTemplateSchema(JsonNode templateNode, JsonPointer currentLocation)
      throws CedarModelValidationException, IOException {
    for (String resourceName : SchemaResources.fullTemplateSchemaResources) {
      validateArtifact(resourceName, templateNode, currentLocation);
    }
  }

  private void validateNodeStructureAgainstElementSchema(JsonNode templateElementNode, JsonPointer
      currentLocation) throws CedarModelValidationException, IOException {
    for (String resourceName : SchemaResources.fullElementSchemaResources) {
      validateArtifact(resourceName, templateElementNode, currentLocation);
    }
  }

  private void validateNodeStructureAgainstFieldSchema(JsonNode fieldNode, JsonPointer currentLocation)
      throws CedarModelValidationException, IOException {
    for (String resourceName : SchemaResources.fullFieldSchemaResources) {
      validateArtifact(resourceName, fieldNode, currentLocation);
    }
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
      if (hasBoth(JSON_LD_ID_FIELD_NAME, JSON_LD_VALUE_FIELD_NAME, propertiesNode)) {
        ProcessingMessage message = createErrorMessage("object has invalid properties ([\"@value\"])");
        errorReport.error(message);
      }
      if (hasMissing(JSON_LD_ID_FIELD_NAME, propertiesNode)) {
        ProcessingMessage message = createErrorMessage("object has missing required properties ([\"@id\"])");
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
      if (hasBoth(JSON_LD_VALUE_FIELD_NAME, JSON_LD_ID_FIELD_NAME, propertiesNode)) {
        ProcessingMessage message = createErrorMessage("object has invalid properties ([\"@id\"])");
        errorReport.error(message);
      }
      if (hasMissing(JSON_LD_VALUE_FIELD_NAME, propertiesNode)) {
        ProcessingMessage message = createErrorMessage("object has missing required properties ([\"@value\"])");
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

  private ErrorItem createErrorItem(@Nonnull JsonPointer baseLocation, @Nullable String message,
                                    @Nullable String location, @Nullable String schemaResource, @Nullable String schemaPointer) {
    ErrorItem errorItem = new ErrorItem(message, createLocation(baseLocation.toString(), location));
    errorItem.addAdditionalInfo("schemaPointer", schemaPointer);
    errorItem.addAdditionalInfo("schemaFile", schemaResource);
    return errorItem;
  }

  private static String createLocation(@Nonnull String baseLocation, @Nullable String relativeLocation) {
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

  private boolean hasConstraintSources(JsonNode valueConstraintsNode) {
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

  private JsonNode loadSchemaFromFile(String resourceName) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource(resourceName);
    return MAPPER.readTree(resource);
  }

  private static boolean isTemplateElement(JsonNode memberNode) {
    return memberNode.path(JSON_LD_TYPE_FIELD_NAME).asText().equals(CEDAR_TEMPLATE_ELEMENT_TYPE_URI);
  }

  private static boolean isStaticTemplateElement(JsonNode memberNode) {
    return memberNode.path(JSON_LD_TYPE_FIELD_NAME).asText().equals(CEDAR_STATIC_TEMPLATE_FIELD_TYPE_URI);
  }

  private static boolean isTemplateField(JsonNode memberNode) {
    return memberNode.path(JSON_LD_TYPE_FIELD_NAME).asText().equals(CEDAR_TEMPLATE_FIELD_TYPE_URI);
  }

  private static String getPropertiesMemberPath(String fieldName) {
    return String.format("/%s/%s", JSON_SCHEMA_PROPERTIES_FIELD_NAME, fieldName);
  }

  private static String getArrayItemPath(int index) {
    return String.format("[%d]", index);
  }

  private static boolean isObjectNode(JsonNode node) {
    return node.isObject();
  }

  private static boolean isArrayNode(JsonNode node) {
    return node.isArray();
  }

  private static JsonPointer createJsonPointer(JsonPointer basePointer, String relativeDestination) {
    JsonPointer destinationPointer = JsonPointer.compile(relativeDestination);
    return basePointer.append(destinationPointer);
  }
}