package org.metadatacenter.model.trimmer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;

import java.util.Set;

public class MatchingPattern {

  private final ObjectNode patternNode;

  public MatchingPattern(ObjectNode patternNode) {
    this.patternNode = patternNode;
  }

  public boolean foundMatch(ObjectNode targetNode) {
    Set<String> patternNodeFields = getFieldNames(patternNode);
    Set<String> targetNodeFields = getFieldNames(targetNode);
    if (!targetNodeFields.containsAll(patternNodeFields)) {
      return false;
    }
    for (String fieldName : patternNodeFields) {
      JsonNode valueNodeInPatternNode = patternNode.get(fieldName);
      JsonNode valueNodeInTargetNode = targetNode.get(fieldName);
      if (!valueNodeInPatternNode.equals(valueNodeInTargetNode)) {
        return false;
      }
    }
    return true;
  }

  private static Set<String> getFieldNames(ObjectNode objectNode) {
    return Sets.newHashSet(objectNode.fieldNames());
  }
}