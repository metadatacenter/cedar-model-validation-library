package org.metadatacenter.model.trimmer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class PruneOperation implements Operation {

  private final ObjectNode objectNode;
  private final TargetFields targetFields;

  public PruneOperation(ObjectNode objectNode, TargetFields targetFields) {
    this.objectNode = checkNotNull(objectNode);
    this.targetFields = checkNotNull(targetFields);
  }

  @Override
  public void evaluate() {
    pruneNode(objectNode, targetFields);
  }

  private void pruneNode(JsonNode node, TargetFields targetFields) {
    if (node.isObject()) {
      doPruning((ObjectNode) node, targetFields);
      pruneObjectNode((ObjectNode) node, targetFields);
    } else if (node.isArray()) {
      pruneArrayNode((ArrayNode) node, targetFields);
    }
  }

  private void doPruning(ObjectNode objectNode, TargetFields targetFields) {
    for (String targetField : targetFields) {
      objectNode.remove(targetField);
    }
  }

  private void pruneObjectNode(ObjectNode parentNode, TargetFields targetFields) {
    for (String fieldName : getFieldNames(parentNode)) {
      JsonNode childNode = parentNode.get(fieldName);
      pruneNode(childNode, targetFields);
    }
  }

  private void pruneArrayNode(ArrayNode arrayNode, TargetFields targetFields) {
    for (JsonNode elementNode : arrayNode) {
      pruneNode(elementNode, targetFields);
    }
  }

  private static Set<String> getFieldNames(ObjectNode objectNode) {
    return Sets.newHashSet(objectNode.fieldNames());
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (!(o instanceof PruneOperation)) {
      return false;
    }
    PruneOperation other = (PruneOperation) o;
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
