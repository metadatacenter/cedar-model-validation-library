package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

public class JSONTreeTrimmer {

  private final ObjectNode rootNode;

  public JSONTreeTrimmer(@Nonnull JsonNode rootNode) {
    checkNotNull(rootNode);
    this.rootNode = rootNode.deepCopy();
  }

  /**
   * Creates a JSON object instance that is the outcome for applying the trimmer's methods.
   */
  public ObjectNode trim() {
    return rootNode;
  }

  /**
   * Removes all JSON nodes that have field names specified in the <code>targetFields</code>.
   *
   * @param targetFields an array of field names to prune
   * @return a reference to this object
   */
  public JSONTreeTrimmer prune(@Nonnull String... targetFields) {
    checkNotNull(targetFields);
    pruneNode(rootNode, Sets.newHashSet(targetFields));
    return this;
  }

  /**
   * Removes all JSON nodes that have field names specified in the <code>targetFields</code>.
   *
   * @param targetFields a set of field names to prune
   * @return a reference to this object
   */
  public JSONTreeTrimmer prune(@Nonnull Set<String> targetFields) {
    checkNotNull(targetFields);
    pruneNode(rootNode, targetFields);
    return this;
  }

  /**
   * Compacts all JSON object nodes into a single value or an array of values that have
   * field names specified in the <code>targetFields</code>.
   *
   * @param targetFields an array of field names to collapse
   * @return a reference to this object
   */
  public JSONTreeTrimmer collapse(@Nonnull String... targetFields) {
    checkNotNull(targetFields);
    scanObjectFields(rootNode, Sets.newHashSet(targetFields)); // ignore return
    return this;
  }

  /**
   * Compacts all JSON object nodes into a single value or an array of values that have
   * field names specified in the <code>targetFields</code>.
   *
   * @param targetFields an set of field names to collapse
   * @return a reference to this object
   */
  public JSONTreeTrimmer collapse(@Nonnull Set<String> targetFields) {
    checkNotNull(targetFields);
    scanObjectFields(rootNode, targetFields); // ignore return
    return this;
  }

  // Private prune operations

  private void pruneNode(JsonNode node, Set<String> targetFields) {
    if (node.isObject()) {
      doPruning((ObjectNode) node, targetFields);
      pruneObjectNode((ObjectNode) node, targetFields);
    } else if (node.isArray()) {
      pruneArrayNode((ArrayNode) node, targetFields);
    }
  }

  private void doPruning(ObjectNode node, Set<String> targetFields) {
    node.remove(targetFields);
  }

  private void pruneObjectNode(ObjectNode parentNode, Set<String> targetFields) {
    for (String fieldName : getFieldNames(parentNode)) {
      JsonNode childNode = parentNode.get(fieldName);
      pruneNode(childNode, targetFields);
    }
  }

  private void pruneArrayNode(ArrayNode arrayNode, Set<String> targetFields) {
    for (JsonNode elementNode : arrayNode) {
      pruneNode(elementNode, targetFields);
    }
  }

  // Private collapse operations

  private JsonNode collapseNode(JsonNode node, Set<String> targetFields) {
    JsonNode collapsedNode = null;
    if (node.isObject()) {
      collapsedNode = collapseObjectNode((ObjectNode) node, targetFields);
    } else if (node.isArray()) {
      collapsedNode = collapseArrayNode((ArrayNode) node, targetFields);
    } else if (node.isValueNode()) {
      collapsedNode = (ValueNode) node;
    }
    return collapsedNode;
  }

  private JsonNode collapseObjectNode(ObjectNode objectNode, Set<String> targetFields) {
    Set<String> fieldNames = getFieldNames(objectNode);
    if (contains(fieldNames, targetFields)) {
      return doCollapsing(objectNode, targetFields);
    } else {
      return scanObjectFields(objectNode, targetFields);
    }
  }

  private JsonNode collapseArrayNode(ArrayNode arrayNode, Set<String> targetFields) {
    ArrayNode collapsedNodes = JsonNodeFactory.instance.arrayNode();
    for (JsonNode element : arrayNode) {
      JsonNode collapsedNode = collapseNode(element, targetFields);
      collapsedNodes.add(collapsedNode);
    }
    return collapsedNodes;
  }

  private JsonNode doCollapsing(ObjectNode parentNode, Set<String> targetFields) {
    ArrayNode collapsedNodes = JsonNodeFactory.instance.arrayNode();
    for (String fieldName : matchFieldNames(parentNode, targetFields)) {
      JsonNode childNode = parentNode.get(fieldName);
      JsonNode collapsedNode = collapseNode(childNode, targetFields);
      collapsedNodes.add(collapsedNode);
    }
    return compact(collapsedNodes);
  }

  private JsonNode scanObjectFields(ObjectNode parentNode, Set<String> targetFields) {
    for (String fieldName : getFieldNames(parentNode)) {
      JsonNode childNode = parentNode.get(fieldName);
      parentNode.set(
          fieldName,
          collapseNode(childNode, targetFields));
    }
    return parentNode;
  }

  private Set<String> matchFieldNames(ObjectNode objectNode, Set<String> targetFields) {
    Set<String> fieldNames = getFieldNames(objectNode);
    return Sets.intersection(fieldNames, targetFields);
  }

  // Private utility methods

  private static Set<String> getFieldNames(ObjectNode objectNode) {
    return Sets.newHashSet(objectNode.fieldNames());
  }

  // Simple checking if there is at least one common element between two lists
  private static boolean contains(Set<String> set1, Set<String> set2) {
    return !Collections.disjoint(set1, set2);
  }

  private static JsonNode compact(ArrayNode arrayNode) {
    return hasSingleElement(arrayNode) ? arrayNode.get(0) : arrayNode;
  }

  private static boolean hasSingleElement(ArrayNode reducedNode) {
    return reducedNode.size() == 1;
  }
}
