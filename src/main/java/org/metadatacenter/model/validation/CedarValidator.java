package org.metadatacenter.model.validation;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
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
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class CedarValidator implements ModelValidator {

  private static final ObjectMapper MAPPER = JacksonUtils.newMapper();

  private static final String JSON_SCHEMA_PROPERTIES = "properties";
  private static final String JSON_SCHEMA_TYPE = "type";
  private static final String JSON_SCHEMA_ITEMS = "items";

  private static final String JSON_SCHEMA_OBJECT = "object";
  private static final String JSON_SCHEMA_ARRAY = "array";

  private static final String JSON_LD_TYPE = "@type";

  private static final String INPUT_TYPE_CONTROLLED_TERM = "controlled-term";
  private static final String INPUT_TYPE_LINK = "link";
  private static final String INPUT_TYPE_EXT_ROR = "ext-ror";
  private static final String INPUT_TYPE_EXT_ORCID = "ext-orcid";
  private static final String INPUT_TYPE_EXT_PFAS = "ext-pfas";
  private static final String INPUT_TYPE_EXT_RRID = "ext-rrid";
  private static final String INPUT_TYPE_EXT_PUBMED = "ext-pubmed";
  private static final String INPUT_TYPE_EXT_NIH_GRANT = "ext-nih-grant";
  private static final String INPUT_TYPE_EXT_DOI = "ext-doi";

  private static Set<String> IRI_INPUT_TYPES = Set.of(INPUT_TYPE_CONTROLLED_TERM, INPUT_TYPE_LINK, INPUT_TYPE_EXT_ROR,
      INPUT_TYPE_EXT_ORCID, INPUT_TYPE_EXT_PFAS, INPUT_TYPE_EXT_RRID, INPUT_TYPE_EXT_PUBMED,
      INPUT_TYPE_EXT_NIH_GRANT, INPUT_TYPE_EXT_DOI);

  private static final String INPUT_TYPE_ATTRIBUTE_VALUE = "attribute-value";
  private static final String INPUT_TYPE_CHECK_BOX = "checkbox";

  private final JsonPointer startingLocation;

  public CedarValidator() {
    this("/");
  }

  public CedarValidator(String startingLocation) {
    checkNotNull(startingLocation);
    this.startingLocation = JsonPointer.compile(startingLocation);
  }

  public ValidationReport validateTemplate(JsonNode templateNode) throws IOException {
    CedarValidationReport report = CedarValidationReport.newEmptyReport();
    try {
      doTemplateValidation(templateNode, startingLocation);
    } catch (CedarModelValidationException thrownException) {
      collectErrorMessages(thrownException, report);
    }
    return report;
  }

  public ValidationReport validateTemplateElement(JsonNode elementNode) throws IOException {
    CedarValidationReport report = CedarValidationReport.newEmptyReport();
    try {
      doElementValidation(elementNode, startingLocation);
    } catch (CedarModelValidationException thrownException) {
      collectErrorMessages(thrownException, report);
    }
    return report;
  }

  public ValidationReport validateTemplateField(JsonNode fieldNode) throws IOException {
    CedarValidationReport report = CedarValidationReport.newEmptyReport();
    try {
      doFieldValidation(fieldNode, startingLocation);
    } catch (CedarModelValidationException thrownException) {
      collectErrorMessages(thrownException, report);
    }
    return report;
  }

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

  private static void doValidation(JsonNode schemaNode, JsonNode instanceNode, JsonPointer location) throws
      CedarModelValidationException {
    try {
      JsonSchema schema = JsonSchemaFactory.byDefault().getJsonSchema(schemaNode);
      ProcessingReport processingReport = schema.validate(instanceNode, false);
      if (!processingReport.isSuccess()) {
        throw newCedarModelValidationException(processingReport, location);
      }
    } catch (ProcessingException e) {
      throw newCedarModelValidationException(e.getProcessingMessage());
    }
  }

  private void doTemplateValidation(JsonNode templateNode, JsonPointer location)
      throws CedarModelValidationException, IOException {
    validateTemplate(templateNode, location);
    checkUserSpecifiedFieldsRecursively(templateNode, location);
  }

  private void doElementValidation(JsonNode elementNode, JsonPointer location)
      throws CedarModelValidationException, IOException {
    validateTemplateElement(elementNode, location);
    checkUserSpecifiedFieldsRecursively(elementNode, location);
  }

  private void doFieldValidation(JsonNode fieldNode, JsonPointer location)
      throws CedarModelValidationException, IOException {
    validateNodeStructureAgainstFieldSchema(fieldNode, location);
  }

  private void doInstanceValidation(JsonNode instanceDocument, JsonNode instanceSchema, JsonPointer location)
      throws CedarModelValidationException, IOException {
    doValidation(instanceSchema, instanceDocument, location);
  }

  private void checkUserSpecifiedFieldsRecursively(JsonNode resourceNode, JsonPointer location)
      throws CedarModelValidationException, IOException {
    JsonNode propertiesNode = resourceNode.get(JSON_SCHEMA_PROPERTIES);
    for (Iterator<String> iter = propertiesNode.fieldNames(); iter.hasNext(); ) {
      String fieldItem = iter.next();
      if (isUserSpecifiedField(fieldItem)) {
        JsonNode fieldNode = propertiesNode.get(fieldItem);
        JsonPointer fieldLocation = createJsonPointer(location, getFieldPath(fieldItem));
        validateUserSpecifiedField(fieldNode, fieldLocation);
      }
    }
  }

  private void validateUserSpecifiedField(JsonNode fieldNode, JsonPointer location)
      throws CedarModelValidationException, IOException {
    if (isMultiInstanceTemplateField(fieldNode)) {
      validateMultiInstanceTemplateField(fieldNode, location);
    } else {
      if (isUsingMultipleOption(fieldNode)) {
        validateFieldWithMultipleOption(fieldNode, location);
      } else {
        validateFieldWithoutMultipleOption(fieldNode, location);
      }
    }
  }

  private void validateFieldWithMultipleOption(JsonNode fieldNode, JsonPointer location)
      throws CedarModelValidationException, IOException {
    JsonNode resourceNode = fieldNode.get(JSON_SCHEMA_ITEMS);
    JsonPointer newLocation = createJsonPointer(location, "/items");
    if (isTemplateElement(resourceNode)) {
      checkUserSpecifiedFieldsRecursively(resourceNode, newLocation);
    } else {
      validateTemplateField(resourceNode, newLocation);
    }
  }

  private void validateFieldWithoutMultipleOption(JsonNode resourceNode, JsonPointer location)
      throws CedarModelValidationException, IOException {
    if (isTemplateElement(resourceNode)) {
      checkUserSpecifiedFieldsRecursively(resourceNode, location);
    } else {
      validateTemplateField(resourceNode, location);
    }
  }

  private void validateTemplateField(JsonNode fieldNode, JsonPointer location)
      throws CedarModelValidationException, IOException {
    if (isStaticTemplateField(fieldNode)) {
      validateStaticTemplateField(fieldNode, location);
    } else {
      validateIRIorLiteralTemplateField(fieldNode, location);
    }
  }

  private void validateTemplate(JsonNode templateNode, JsonPointer location)
      throws CedarModelValidationException, IOException {
    validateResource(SchemaResources.TEMPLATE_META_SCHEMA, templateNode, location);
  }

  private void validateTemplateElement(JsonNode templateElementNode, JsonPointer location)
      throws CedarModelValidationException, IOException {
    validateResource(SchemaResources.ELEMENT_META_SCHEMA, templateElementNode, location);
  }

  private void validateNodeStructureAgainstFieldSchema(JsonNode fieldNode, JsonPointer location)
      throws CedarModelValidationException, IOException {
    if (isStaticTemplateField(fieldNode)) {
      validateStaticTemplateField(fieldNode, location);
    } else if (isMultiInstanceTemplateField(fieldNode)) {
      validateMultiInstanceTemplateField(fieldNode, location);
    } else {
      validateIRIorLiteralTemplateField(fieldNode, location);
    }
  }

  private void validateIRIorLiteralTemplateField(JsonNode fieldNode, JsonPointer location)
      throws CedarModelValidationException, IOException {
    if (isIRIField(fieldNode)) {
      validateResource(SchemaResources.IRI_FIELD_META_SCHEMA, fieldNode, location);
    } else {
      validateResource(SchemaResources.LITERAL_FIELD_META_SCHEMA, fieldNode, location);
    }
  }

  private void validateMultiInstanceTemplateField(JsonNode fieldNode, JsonPointer location)
      throws CedarModelValidationException, IOException {
    validateResource(SchemaResources.MULTI_INSTANCE_FIELD_META_SCHEMA, fieldNode, location);
  }

  private void validateStaticTemplateField(JsonNode fieldNode, JsonPointer location)
      throws CedarModelValidationException, IOException {
    validateResource(SchemaResources.STATIC_FIELD_SCHEMA, fieldNode, location);
  }

  private void validateResource(String schemaFile, JsonNode resourceNode, JsonPointer location)
      throws CedarModelValidationException, IOException {
    final JsonNode schemaNode = loadSchemaFromFile(schemaFile);
    doValidation(schemaNode, resourceNode, location);
  }

  private void collectErrorMessages(CedarModelValidationException exception, final CedarValidationReport report) {
    Multimap<JsonPointer, ProcessingMessage> errorDetails = exception.getDetails();
    for (JsonPointer errorLocation : errorDetails.keySet()) {
      Collection<ProcessingMessage> processingMessages = errorDetails.get(errorLocation);
      for (ProcessingMessage processingMessage : processingMessages) {
        final LogLevel messageLevel = processingMessage.getLogLevel();
        if (messageLevel == LogLevel.ERROR || messageLevel == LogLevel.FATAL) {
          ParsedProcessingMessage parsedMessage = new ParsedProcessingMessage(processingMessage);
          for (ParsedProcessingMessage.ReportItem reportItem : parsedMessage.getReportItems()) {
            String errorAbsoluteLocation = createLocation(errorLocation.toString(), reportItem.getLocation());
            ErrorItem errorItem = createErrorItem(
                reportItem.getMessage(),
                errorAbsoluteLocation,
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

  private ErrorItem createErrorItem(String message, String errorLocation, String schemaName, String schemaLocation) {
    ErrorItem errorItem = new ErrorItem(message, errorLocation);
    errorItem.addAdditionalInfo("schemaPointer", schemaLocation);
    errorItem.addAdditionalInfo("schemaFile", schemaName);
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

  private static boolean isUserSpecifiedField(String fieldName) {
    return !CedarModelVocabulary.CommonPropertiesInnerFields.contains(fieldName);
  }

  private static CedarModelValidationException newCedarModelValidationException(
      ProcessingReport report, JsonPointer location) {
    CedarModelValidationException exception = new CedarModelValidationException();
    exception.addProcessingReport(report, location);
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

  private static boolean isTemplateElement(JsonNode resourceNode) {
    return resourceNode.path(JSON_LD_TYPE).asText().equals(CedarConstants.TEMPLATE_ELEMENT_TYPE_URI);
  }

  private static boolean isTemplateField(JsonNode resourceNode) {
    return resourceNode.path(JSON_LD_TYPE).asText().equals(CedarConstants.TEMPLATE_FIELD_TYPE_URI);
  }

  private static boolean isStaticTemplateField(JsonNode resourceNode) {
    return resourceNode.path(JSON_LD_TYPE).asText().equals(CedarConstants.STATIC_TEMPLATE_FIELD_TYPE_URI);
  }

  private static boolean isMultiInstanceTemplateField(JsonNode resourceNode) {
    if (isTypedArray(resourceNode)) {
      JsonNode fieldNode = resourceNode.get(JSON_SCHEMA_ITEMS);
      if (fieldNode.has(CedarModelVocabulary.UI)) {
        JsonNode uiNode = fieldNode.get(CedarModelVocabulary.UI);
        String inputType = uiNode.path(CedarModelVocabulary.INPUT_TYPE).asText();
        return inputType.equals(INPUT_TYPE_CHECK_BOX) || inputType.equals(INPUT_TYPE_ATTRIBUTE_VALUE);
      }
    }
    return false;
  }

  private static boolean isUsingMultipleOption(JsonNode resourceNode) {
    return isTypedArray(resourceNode);
  }

  private static boolean isTypedArray(JsonNode node) {
    return node.path(JSON_SCHEMA_TYPE).asText().equals(JSON_SCHEMA_ARRAY);
  }

  private static String getFieldPath(String fieldName) {
    return String.format("/%s/%s", JSON_SCHEMA_PROPERTIES, fieldName);
  }

  private static boolean isIRIField(JsonNode node) {
    if (node.has(CedarModelVocabulary.UI)) {
      JsonNode uiNode = node.get(CedarModelVocabulary.UI);
      String inputType = uiNode.path(CedarModelVocabulary.INPUT_TYPE).asText();
      return IRI_INPUT_TYPES.contains(inputType);
    }
    return false;
  }

  private static JsonPointer createJsonPointer(JsonPointer basePointer, String relativeDestination) {
    JsonPointer destinationPointer = JsonPointer.compile(relativeDestination);
    return basePointer.append(destinationPointer);
  }
}
