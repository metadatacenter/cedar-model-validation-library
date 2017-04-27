package org.metadatacenter.model.validation.internal;

import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SchemaResources {

  public static final String CORE_JSON_SCHEMA_FIELDS_SCHEMA_RESOURCE_NAME =
      "coreJSONSchemaFields.json";
  public static final String JSON_LD_ID_FIELD_SCHEMA_RESOURCE_NAME =
      "jsonLDIDField.json";
  public static final String JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME =
      "jsonLDTypeField.json";
  public static final String PROVENANCE_FIELDS_SCHEMA_RESOURCE_NAME =
      "provenanceFields.json";

  public static final String CORE_JSON_SCHEMA_TEMPLATE_FIELDS_SCHEMA_RESOURCE_NAME =
      "coreJSONSchemaTemplateFields.json";
  public static final String TEMPLATE_JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME =
      "templateJSONLDTypeField.json";
  public static final String TEMPLATE_JSON_LD_CONTEXT_FIELD_SCHEMA_RESOURCE_NAME =
      "templateJSONLDContextField.json";
  public static final String TEMPLATE_PROPERTIES_FIELD_SCHEMA_RESOURCE_NAME =
      "templatePropertiesField.json";
  public static final String TEMPLATE_UI_FIELD_SCHEMA_RESOURCE_NAME =
      "templateUIField.json";
  public static final String TEMPLATE_REQUIRED_FIELD_SCHEMA_RESOURCE_NAME =
      "templateRequiredField.json";

  public static final String CORE_JSON_SCHEMA_TEMPLATE_ELEMENT_FIELDS_SCHEMA_RESOURCE_NAME =
      "coreJSONSchemaTemplateElementFields.json";
  public static final String TEMPLATE_ELEMENT_JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME =
      "templateElementJSONLDTypeField.json";
  public static final String TEMPLATE_ELEMENT_JSON_LD_CONTEXT_FIELD_SCHEMA_RESOURCE_NAME =
      "templateElementJSONLDContextField.json";
  public static final String TEMPLATE_ELEMENT_PROPERTIES_FIELD_SCHEMA_RESOURCE_NAME =
      "templateElementPropertiesField.json";
  public static final String TEMPLATE_ELEMENT_UI_FIELD_SCHEMA_RESOURCE_NAME =
      "templateElementUIField.json";

  public static final String CORE_JSON_SCHEMA_TEMPLATE_FIELD_FIELDS_SCHEMA_RESOURCE_NAME =
      "coreJSONSchemaTemplateFieldFields.json";
  public static final String TEMPLATE_FIELD_JSON_LD_ID_FIELD_SCHEMA_RESOURCE_NAME =
      "templateFieldJSONLDIDField.json";
  public static final String TEMPLATE_FIELD_JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME =
      "templateFieldJSONLDTypeField.json";
  public static final String TEMPLATE_FIELD_JSON_LD_CONTEXT_FIELD_SCHEMA_RESOURCE_NAME =
      "templateFieldJSONLDContextField.json";
  public static final String VALUE_CONSTRAINTS_FIELD_SCHEMA_RESOURCE_NAME =
      "valueConstraintsField.json";
  public static final String TEMPLATE_FIELD_SINGLE_VALUE_CONTENT_FIELD_SCHEMA_RESOURCE_NAME =
      "templateFieldSingleValueContent.json";
  public static final String TEMPLATE_FIELD_UI_FIELD_SCHEMA_RESOURCE_NAME =
      "templateFieldUIField.json";

  public static final Set<String> coreSchemaResources = new HashSet<>(
      Arrays.asList(CORE_JSON_SCHEMA_FIELDS_SCHEMA_RESOURCE_NAME,
          JSON_LD_ID_FIELD_SCHEMA_RESOURCE_NAME,
          JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME,
          PROVENANCE_FIELDS_SCHEMA_RESOURCE_NAME));

  public static final Set<String> coreTemplateSchemaResources = Sets.newHashSet(coreSchemaResources);

  public static final Set<String> templateSchemaResources = new HashSet<>(
      Arrays.asList(CORE_JSON_SCHEMA_TEMPLATE_FIELDS_SCHEMA_RESOURCE_NAME,
          TEMPLATE_JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME,
          TEMPLATE_JSON_LD_CONTEXT_FIELD_SCHEMA_RESOURCE_NAME,
          TEMPLATE_PROPERTIES_FIELD_SCHEMA_RESOURCE_NAME,
          TEMPLATE_UI_FIELD_SCHEMA_RESOURCE_NAME,
          TEMPLATE_REQUIRED_FIELD_SCHEMA_RESOURCE_NAME));

  public static final Set<String> coreElementSchemaResources = Sets.newHashSet(coreSchemaResources);

  public static final Set<String> elementSchemaResources = new HashSet<>(
      Arrays.asList(CORE_JSON_SCHEMA_TEMPLATE_ELEMENT_FIELDS_SCHEMA_RESOURCE_NAME,
          TEMPLATE_ELEMENT_JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME,
          TEMPLATE_ELEMENT_JSON_LD_CONTEXT_FIELD_SCHEMA_RESOURCE_NAME,
          TEMPLATE_ELEMENT_PROPERTIES_FIELD_SCHEMA_RESOURCE_NAME,
          TEMPLATE_ELEMENT_UI_FIELD_SCHEMA_RESOURCE_NAME));

  public static final Set<String> coreFieldSchemaResources = Sets.newHashSet(coreSchemaResources);

  public static final Set<String> fieldSchemaResources = new HashSet<>(
      Arrays.asList(CORE_JSON_SCHEMA_TEMPLATE_FIELD_FIELDS_SCHEMA_RESOURCE_NAME,
          TEMPLATE_FIELD_JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME,
          TEMPLATE_FIELD_JSON_LD_CONTEXT_FIELD_SCHEMA_RESOURCE_NAME,
          VALUE_CONSTRAINTS_FIELD_SCHEMA_RESOURCE_NAME,
          TEMPLATE_FIELD_SINGLE_VALUE_CONTENT_FIELD_SCHEMA_RESOURCE_NAME,
          TEMPLATE_FIELD_UI_FIELD_SCHEMA_RESOURCE_NAME));

  public static final Set<String> coreStaticFieldSchemaResources = Sets.newHashSet(coreSchemaResources);

  public static final Set<String> staticFieldSchemaResources = new HashSet<>(
      Arrays.asList(CORE_JSON_SCHEMA_TEMPLATE_FIELD_FIELDS_SCHEMA_RESOURCE_NAME,
          TEMPLATE_FIELD_JSON_LD_TYPE_FIELD_SCHEMA_RESOURCE_NAME,
          TEMPLATE_FIELD_SINGLE_VALUE_CONTENT_FIELD_SCHEMA_RESOURCE_NAME,
          TEMPLATE_FIELD_UI_FIELD_SCHEMA_RESOURCE_NAME));

  public static final Set<String> fullTemplateSchemaResources = Sets.newHashSet();
  static {
    fullTemplateSchemaResources.addAll(coreTemplateSchemaResources);
    fullTemplateSchemaResources.addAll(templateSchemaResources);
  }

  public static final Set<String> fullElementSchemaResources = Sets.newHashSet();
  static {
    fullElementSchemaResources.addAll(coreElementSchemaResources);
    fullElementSchemaResources.addAll(elementSchemaResources);
  }

  public static final Set<String> fullFieldSchemaResources = Sets.newHashSet();
  static {
    fullFieldSchemaResources.addAll(coreFieldSchemaResources);
    fullFieldSchemaResources.addAll(fieldSchemaResources);
  }

  public static final Set<String> fullStaticFieldSchemaResources = Sets.newHashSet();
  static {
    fullStaticFieldSchemaResources.addAll(coreStaticFieldSchemaResources);
    fullStaticFieldSchemaResources.addAll(staticFieldSchemaResources);
  }
}
