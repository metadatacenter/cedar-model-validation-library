package org.metadatacenter.model.trimmer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class MatchingPattern {

  private final ObjectNode patternNode;

  public MatchingPattern(ObjectNode patternNode) {
    this.patternNode = patternNode;
  }

  public static MatchingPattern whenFound(@Nonnull ObjectNode matchingPattern) {
    checkNotNull(matchingPattern);
    return new MatchingPattern(matchingPattern);
  }

  public boolean matches(@Nonnull ObjectNode targetNode) {
    checkNotNull(targetNode);
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

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (!(o instanceof MatchingPattern)) {
      return false;
    }
    MatchingPattern other = (MatchingPattern) o;
    return Objects.equal(patternNode, other.patternNode);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(patternNode);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .addValue(patternNode)
        .toString();
  }
}