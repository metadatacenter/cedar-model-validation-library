package org.metadatacenter.model.validation;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.PathType;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.ValidationMessage;
import org.metadatacenter.model.core.CedarConstants;
import org.metadatacenter.model.validation.internal.FgeCompatFormats;
import org.metadatacenter.model.core.CedarModelVocabulary;
import org.metadatacenter.model.validation.internal.ParsedProcessingMessage;
import org.metadatacenter.model.validation.internal.SchemaResources;
import org.metadatacenter.model.validation.report.CedarValidationReport;
import org.metadatacenter.model.validation.report.ErrorItem;
import org.metadatacenter.model.validation.report.ValidationReport;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class CedarValidator implements ModelValidator {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  // CEDAR meta-schemas and template-derived instance schemas are JSON Schema Draft-04. Format
  // assertion is enabled explicitly so that the `uri` and `date-time` formats the meta-schemas
  // declare are enforced; the factory supplies FGE-compatible checkers for those two formats so
  // that assertion accepts exactly what the former engine accepted.
  private static final JsonSchemaFactory SCHEMA_FACTORY = FgeCompatFormats.FACTORY;

  private static final SchemaValidatorsConfig VALIDATOR_CONFIG = SchemaValidatorsConfig.builder()
      .pathType(PathType.JSON_POINTER)
      .formatAssertionsEnabled(true)
      .build();

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
  private static final String INPUT_TYPE_EXT_NIH_GRANT_ID = "ext-nih-grant-id";
  private static final String INPUT_TYPE_EXT_DOI = "ext-doi";

  private static Set<String> IRI_INPUT_TYPES = Set.of(INPUT_TYPE_CONTROLLED_TERM, INPUT_TYPE_LINK, INPUT_TYPE_EXT_ROR,
      INPUT_TYPE_EXT_ORCID, INPUT_TYPE_EXT_PFAS, INPUT_TYPE_EXT_RRID, INPUT_TYPE_EXT_PUBMED,
      INPUT_TYPE_EXT_NIH_GRANT_ID, INPUT_TYPE_EXT_DOI);

  private static final String INPUT_TYPE_ATTRIBUTE_VALUE = "attribute-value";
  private static final String INPUT_TYPE_CHECK_BOX = "checkbox";

  /**
   * Attribute-value names are promoted to keys of the object that contains the
   * field. They therefore share a namespace with JSON-LD metadata and normal
   * template children; JSON Schema alone cannot express those cross-property
   * constraints.
   */
  private static final Set<String> RESERVED_ATTRIBUTE_VALUE_NAMES = Set.of(
      "@context", "@id", "@type", "@value", "@language",
      "schema:isBasedOn", "schema:name", "schema:description",
      "pav:derivedFrom", "pav:createdOn", "pav:createdBy", "pav:lastUpdatedOn",
      "oslc:modifiedBy", "rdfs:label", "skos:prefLabel", "skos:altLabel",
      "skos:notation", "_annotations");

  private static final Set<String> NON_SERIALIZING_INPUT_TYPES = Set.of(
      "page-break", "section-break", "richtext", "image", "youtube", "attribute-value");

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
    collectSchemaPropertyIriErrors(templateNode, "", report);
    return report;
  }

  public ValidationReport validateTemplateElement(JsonNode elementNode) throws IOException {
    CedarValidationReport report = CedarValidationReport.newEmptyReport();
    try {
      doElementValidation(elementNode, startingLocation);
    } catch (CedarModelValidationException thrownException) {
      collectErrorMessages(thrownException, report);
    }
    collectSchemaPropertyIriErrors(elementNode, "", report);
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
    collectAttributeValueNameErrors(templateInstance, instanceSchema, "", report);
    return report;
  }

  public ValidationReport validateElementInstance(JsonNode elementInstance, JsonNode elementSchema)
      throws IOException {
    CedarValidationReport report = CedarValidationReport.newEmptyReport();
    try {
      doInstanceValidation(elementInstance, elementSchema, startingLocation);
    } catch (CedarModelValidationException thrownException) {
      collectErrorMessages(thrownException, report);
    }
    return report;
  }

  private static void doValidation(JsonNode schemaNode, JsonNode instanceNode, JsonPointer location)
      throws CedarModelValidationException {
    doValidation(schemaNode, instanceNode, location, null);
  }

  private static void doValidation(JsonNode schemaNode, JsonNode instanceNode, JsonPointer location,
                                   String schemaResourceName) throws CedarModelValidationException {
    JsonSchema schema = SCHEMA_FACTORY.getSchema(schemaNode, VALIDATOR_CONFIG);
    Set<ValidationMessage> messages = schema.validate(instanceNode);
    if (!messages.isEmpty()) {
      throw newCedarModelValidationException(messages, location, schemaResourceName);
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
    doValidation(schemaNode, resourceNode, location, schemaFile);
  }

  private void collectErrorMessages(CedarModelValidationException exception, final CedarValidationReport report) {
    Multimap<JsonPointer, ValidationMessage> errorDetails = exception.getDetails();
    String schemaResourceName = exception.getSchemaResourceName();
    for (JsonPointer errorLocation : errorDetails.keySet()) {
      Collection<ValidationMessage> validationMessages = errorDetails.get(errorLocation);
      for (ValidationMessage validationMessage : validationMessages) {
        ParsedProcessingMessage parsedMessage = new ParsedProcessingMessage(validationMessage, schemaResourceName);
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

  /**
   * Enforces the namespace rules for attribute-value fields at every instance
   * container. The template identifies the string arrays that are really
   * attribute-value fields, avoiding the false positive of treating every
   * textual array as one.
   */
  private void collectAttributeValueNameErrors(JsonNode instanceNode, JsonNode schemaNode, String path,
                                               CedarValidationReport report) {
    if (instanceNode == null || !instanceNode.isObject() || schemaNode == null || !schemaNode.isObject()) {
      return;
    }
    JsonNode schemaProperties = schemaNode.get(JSON_SCHEMA_PROPERTIES);
    if (schemaProperties == null || !schemaProperties.isObject()) {
      return;
    }

    Map<String, JsonNode> declaredChildren = new LinkedHashMap<>();
    Set<String> attributeValueGroups = new LinkedHashSet<>();
    Set<String> serializingChildren = new LinkedHashSet<>();
    schemaProperties.fields().forEachRemaining(entry -> {
      JsonNode child = childDefinition(entry.getValue());
      if (child == null || !child.path(CedarModelVocabulary.UI).isObject()) {
        return;
      }
      declaredChildren.put(entry.getKey(), child);
      String inputType = child.path(CedarModelVocabulary.UI).path(CedarModelVocabulary.INPUT_TYPE).asText();
      if (INPUT_TYPE_ATTRIBUTE_VALUE.equals(inputType)) {
        attributeValueGroups.add(entry.getKey());
      } else if (!NON_SERIALIZING_INPUT_TYPES.contains(inputType)) {
        serializingChildren.add(entry.getKey());
      }
    });

    Map<String, String> firstGroupForName = new HashMap<>();
    for (String groupName : attributeValueGroups) {
      JsonNode names = instanceNode.get(groupName);
      if (names == null || !names.isArray()) {
        continue;
      }
      Set<String> namesInGroup = new HashSet<>();
      for (JsonNode nameNode : names) {
        if (!nameNode.isTextual()) {
          continue; // The JSON Schema report owns the type error.
        }
        String name = nameNode.asText();
        String location = path + "/" + escapePointer(groupName) + "/" + escapePointer(name);
        if (name.isBlank()) {
          report.addError(new ErrorItem("Attribute-value names must not be blank", location));
          continue;
        }
        if (name.startsWith("@") || RESERVED_ATTRIBUTE_VALUE_NAMES.contains(name)) {
          report.addError(new ErrorItem("Attribute-value name '" + name + "' is reserved for instance metadata",
              location));
        } else if (attributeValueGroups.contains(name) || serializingChildren.contains(name)) {
          report.addError(new ErrorItem("Attribute-value name '" + name
              + "' collides with a template child in the same object", location));
        }
        if (!namesInGroup.add(name)) {
          report.addError(new ErrorItem("Attribute-value name '" + name
              + "' occurs more than once in field '" + groupName + "'", location));
        }
        String firstGroup = firstGroupForName.putIfAbsent(name, groupName);
        if (firstGroup != null && !firstGroup.equals(groupName)) {
          report.addError(new ErrorItem("Attribute-value name '" + name + "' is also used by field '"
              + firstGroup + "'", location));
        }
      }
    }

    for (Map.Entry<String, JsonNode> child : declaredChildren.entrySet()) {
      if (attributeValueGroups.contains(child.getKey())) {
        continue;
      }
      JsonNode childInstance = instanceNode.get(child.getKey());
      if (childInstance == null) {
        continue;
      }
      String childPath = path + "/" + escapePointer(child.getKey());
      if (isTemplateElement(child.getValue())) {
        collectElementOccurrenceIdErrors(childInstance, childPath, report);
      }
      if (childInstance.isArray()) {
        for (int index = 0; index < childInstance.size(); index++) {
          collectAttributeValueNameErrors(childInstance.get(index), child.getValue(), childPath + "/" + index,
              report);
        }
      } else {
        collectAttributeValueNameErrors(childInstance, child.getValue(), childPath, report);
      }
    }
  }

  private void collectElementOccurrenceIdErrors(JsonNode occurrence, String path, CedarValidationReport report) {
    if (occurrence == null) {
      return;
    }
    if (occurrence.isArray()) {
      for (int index = 0; index < occurrence.size(); index++) {
        collectElementOccurrenceIdErrors(occurrence.get(index), path + "/" + index, report);
      }
      return;
    }
    if (!occurrence.isObject()) {
      return;
    }
    JsonNode id = occurrence.get("@id");
    // Null is the intentional draft spelling. The artifact server replaces it
    // before calling this validator on a write; a stated non-null value must be
    // an actual absolute IRI, not the empty relative URI accepted by format:uri.
    if (id != null && !id.isNull() && id.isTextual() && !isAbsoluteIri(id.asText())) {
      report.addError(new ErrorItem("Element occurrence @id must be an absolute IRI or null", path + "/@id"));
    }
  }

  private void collectSchemaPropertyIriErrors(JsonNode container, String path, CedarValidationReport report) {
    if (container == null || !container.isObject()) {
      return;
    }
    JsonNode properties = container.get(JSON_SCHEMA_PROPERTIES);
    if (properties == null || !properties.isObject()) {
      return;
    }
    JsonNode contextProperties = properties.path("@context").path(JSON_SCHEMA_PROPERTIES);
    properties.fields().forEachRemaining(entry -> {
      JsonNode child = childDefinition(entry.getValue());
      if (child == null || !child.path(CedarModelVocabulary.UI).isObject()) {
        return;
      }
      String childPath = path + "/properties/" + escapePointer(entry.getKey());
      JsonNode mapping = contextProperties.path(entry.getKey()).path("enum");
      if (mapping.isArray()) {
        for (JsonNode iri : mapping) {
          if (iri.isTextual() && !isAbsoluteIri(iri.asText())) {
            report.addError(new ErrorItem("Property IRI for child '" + entry.getKey()
                + "' must be an absolute IRI", path + "/properties/@context/properties/"
                + escapePointer(entry.getKey()) + "/enum"));
          }
        }
      }
      collectSchemaPropertyIriErrors(child, childPath, report);
    });
  }

  private static JsonNode childDefinition(JsonNode declared) {
    if (declared == null || !declared.isObject()) {
      return null;
    }
    JsonNode items = declared.get(JSON_SCHEMA_ITEMS);
    return items != null && items.isObject() ? items : declared;
  }

  private static String escapePointer(String component) {
    return component.replace("~", "~0").replace("/", "~1");
  }

  private static boolean isAbsoluteIri(String value) {
    if (value == null || value.isBlank()) {
      return false;
    }
    try {
      return new URI(value).isAbsolute();
    } catch (URISyntaxException e) {
      return false;
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
      Set<ValidationMessage> messages, JsonPointer location, String schemaResourceName) {
    CedarModelValidationException exception = new CedarModelValidationException();
    exception.setSchemaResourceName(schemaResourceName);
    exception.addValidationMessages(messages, location);
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
