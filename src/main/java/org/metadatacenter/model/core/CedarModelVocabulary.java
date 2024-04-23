package org.metadatacenter.model.core;

import org.metadatacenter.model.trimmer.JsonLdToken;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CedarModelVocabulary {

  public static final String SCHEMA = "$schema";

  public static final String UI = "_ui";
  public static final String VALUE_LABEL = "_valueLabel";
  public static final String VALUE_CONSTRAINTS = "_valueConstraints";

  public static final String TITLE = "title";
  public static final String DESCRIPTION = "description";
  public static final String ORDER = "order";
  public static final String INPUT_TYPE = "inputType";
  public static final String PROPERTY_LABELS = "propertyLabels";
  public static final String DATE_TYPE = "dateType";
  public static final String VALUE_RECOMMENDATION_ENABLED = "valueRecommendationEnabled";

  public static final String REQUIRED_VALUE = "requiredValue";
  public static final String ONTOLOGIES = "ontologies";
  public static final String VALUE_SETS = "valueSets";
  public static final String CLASSES = "classes";
  public static final String BRANCHES = "branches";

  public static final String SOURCE = "source";
  public static final String ACRONYM = "acronym";
  public static final String URI = "uri";
  public static final String NAME = "name";
  public static final String MAX_DEPTH = "maxDepth";

  public static final String ADDITIONAL_PROPERTIES = "additionalProperties";

  public static final String ANNOTATIONS = "_annotations";

  public static final String PAV_CREATED_ON = "pav:createdOn";
  public static final String PAV_CREATED_BY = "pav:createdBy";
  public static final String PAV_LAST_UPDATED_ON = "pav:lastUpdatedOn";
  public static final String PAV_DERIVED_FROM = "pav:derivedFrom";
  public static final String OSLC_MODIFIED_BY = "oslc:modifiedBy";

  public static final String SCHEMA_IS_BASED_ON = "schema:isBasedOn";
  public static final String SCHEMA_NAME = "schema:name";
  public static final String SCHEMA_DESCRIPTION = "schema:description";

  public static final Set<String> CommonPropertiesInnerFields = new HashSet<>(
      Arrays.asList(JsonLdToken.JSON_LD_CONTEXT, JsonLdToken.JSON_LD_TYPE, JsonLdToken.JSON_LD_ID, JsonLdToken.JSON_LD_VALUE,
          PAV_CREATED_ON, PAV_CREATED_BY, PAV_LAST_UPDATED_ON, PAV_DERIVED_FROM, OSLC_MODIFIED_BY, VALUE_LABEL,
          SCHEMA_IS_BASED_ON, SCHEMA_NAME, SCHEMA_DESCRIPTION, ANNOTATIONS));

  public static final Set<String> CommonUIInnerFields = new HashSet<>(
      Arrays.asList(TITLE, DESCRIPTION, ORDER, INPUT_TYPE, PROPERTY_LABELS, DATE_TYPE,
          VALUE_RECOMMENDATION_ENABLED));
}
