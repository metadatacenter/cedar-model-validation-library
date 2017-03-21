package org.metadatacenter.model.trimmer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class CollapseOperation implements Operation {

  private final ObjectNode objectNode;
  private final TargetFields targetFields;

  private Optional<MatchingPattern> condition = Optional.empty();

  public CollapseOperation(@Nonnull ObjectNode objectNode, @Nonnull TargetFields targetFields) {
    this.objectNode = checkNotNull(objectNode);
    this.targetFields = checkNotNull(targetFields);
  }

  public void setCondition(MatchingPattern condition) {
    this.condition = Optional.of(condition);
  }

  @Override
  public void evaluate() {
    scanObjectFields(objectNode, targetFields);
  }

  private JsonNode collapseNode(JsonNode node, TargetFields targetFields) {
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

  private JsonNode collapseObjectNode(ObjectNode objectNode, TargetFields targetFields) {
    if (condition.isPresent()) {
      return collapseObjectNodeWithCondition(objectNode, targetFields, condition.get());
    } else {
      return collapseObjectNodeWithoutCondition(objectNode, targetFields);
    }
  }

  private JsonNode collapseObjectNodeWithCondition(ObjectNode objectNode, TargetFields targetFields,
                                                   MatchingPattern matchingPattern) {
    if (targetFields.within(objectNode)) {
      if (matchingPattern.matches(objectNode)) {
        return doCollapsing(objectNode, targetFields);
      }
    }
    return scanObjectFields(objectNode, targetFields);
  }

  private JsonNode collapseObjectNodeWithoutCondition(ObjectNode objectNode, TargetFields targetFields) {
    if (targetFields.within(objectNode)) {
      return doCollapsing(objectNode, targetFields);
    } else {
      return scanObjectFields(objectNode, targetFields);
    }
  }

  private JsonNode collapseArrayNode(ArrayNode arrayNode, TargetFields targetFields) {
    ArrayNode collapsedNodes = JsonNodeFactory.instance.arrayNode();
    for (JsonNode element : arrayNode) {
      JsonNode collapsedNode = collapseNode(element, targetFields);
      collapsedNodes.add(collapsedNode);
    }
    return collapsedNodes;
  }


  private JsonNode doCollapsing(ObjectNode parentNode, TargetFields targetFields) {
    ArrayNode collapsedNodes = JsonNodeFactory.instance.arrayNode();
    for (String fieldName : matchingFields(parentNode, targetFields)) {
      JsonNode childNode = parentNode.get(fieldName);
      JsonNode collapsedNode = collapseNode(childNode, targetFields);
      collapsedNodes.add(collapsedNode);
    }
    return compact(collapsedNodes);
  }

  private JsonNode scanObjectFields(ObjectNode parentNode, TargetFields targetFields) {
    for (String fieldName : getFieldNames(parentNode)) {
      JsonNode childNode = parentNode.get(fieldName);
      parentNode.set(
          fieldName,
          collapseNode(childNode, targetFields));
    }
    return parentNode;
  }

  private static Set<String> getFieldNames(ObjectNode objectNode) {
    return Sets.newHashSet(objectNode.fieldNames());
  }

  private static Set<String> matchingFields(ObjectNode objectNode, TargetFields targetFields) {
    return targetFields.getMatchingFields(objectNode);
  }

  private static JsonNode compact(ArrayNode arrayNode) {
    return hasSingleElement(arrayNode) ? arrayNode.get(0) : arrayNode;
  }

  private static boolean hasSingleElement(ArrayNode reducedNode) {
    return reducedNode.size() == 1;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (!(o instanceof CollapseOperation)) {
      return false;
    }
    CollapseOperation other = (CollapseOperation) o;
    return Objects.equal(objectNode, other.objectNode)
        && Objects.equal(targetFields, other.targetFields);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(objectNode, targetFields);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .addValue(objectNode)
        .addValue(targetFields)
        .toString();
  }
}
