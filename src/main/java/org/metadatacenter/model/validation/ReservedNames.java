package org.metadatacenter.model.validation;

import java.util.Set;

/**
 * The names a child of a template or element, or an attribute a form-filler invents, may not take.
 *
 * <p>Every child's key, and every attribute name of an attribute-value field, becomes a property of
 * an instance's JSON object beside the properties CEDAR writes there itself. Such a name therefore
 * may not be a JSON-LD keyword, a CEDAR instance property or an object internal that JavaScript
 * consumers of the same document cannot hold as an ordinary key.
 *
 * <p>An attribute-value field's own key has one more constraint. The YAML form writes it beside its
 * parent's metadata rather than under {@code children}, so it may not be one of the metadata keys
 * that parent's YAML mapping carries. An element reserves the keys of its standalone mapping, which
 * include those of its nested one, so the same element instance remains writable in either form.
 *
 * <p>The validator and the artifact library's readers and builders all ask this class. The
 * TypeScript model library's {@code ReservedNames} answers the same questions with the same sets.
 */
public final class ReservedNames {
  /** The artifact an attribute-value field is a child of, which decides the YAML keys beside it. */
  public enum Parent { TEMPLATE, ELEMENT }

  /** The properties CEDAR writes into a template, element or field instance. */
  public static final Set<String> INSTANCE_PROPERTIES = Set.of("schema:name", "schema:description",
      "schema:identifier", "schema:isBasedOn", "pav:createdOn", "pav:createdBy", "pav:lastUpdatedOn",
      "pav:derivedFrom", "oslc:modifiedBy", "_annotations", "rdfs:label", "skos:notation", "skos:prefLabel",
      "skos:altLabel");

  /** The keys a JavaScript object treats as its own machinery rather than as data. */
  public static final Set<String> OBJECT_INTERNALS = Set.of("__proto__", "constructor", "prototype");

  /** The metadata keys of a template instance's YAML mapping. */
  public static final Set<String> TEMPLATE_INSTANCE_YAML_KEYS = Set.of("type", "name", "description", "id",
      "isBasedOn", "derivedFrom", "createdBy", "modifiedBy", "createdOn", "modifiedOn", "children", "annotations");

  /** The metadata keys of an element instance's YAML mapping when it is written inside its parent. */
  public static final Set<String> NESTED_ELEMENT_INSTANCE_YAML_KEYS = Set.of("type", "id", "children");

  /** The metadata keys of an element instance's YAML mapping when it is written on its own. */
  public static final Set<String> STANDALONE_ELEMENT_INSTANCE_YAML_KEYS = Set.of("type", "name", "description",
      "id", "createdBy", "modifiedBy", "createdOn", "modifiedOn", "children");

  private ReservedNames() {}

  /** Whether no child, and no attribute a form-filler invents, may take this name. */
  public static boolean isReservedName(String name) {
    return name.startsWith("@") || INSTANCE_PROPERTIES.contains(name) || OBJECT_INTERNALS.contains(name);
  }

  /** Whether an attribute-value field that is a child of {@code parent} may not take this name. */
  public static boolean isReservedAttributeValueFieldName(String name, Parent parent) {
    return isReservedName(name) || yamlKeys(parent).contains(name);
  }

  /** The metadata keys the YAML form writes beside an attribute-value field of {@code parent}. */
  public static Set<String> yamlKeys(Parent parent) {
    return parent == Parent.TEMPLATE ? TEMPLATE_INSTANCE_YAML_KEYS : STANDALONE_ELEMENT_INSTANCE_YAML_KEYS;
  }
}
