package org.metadatacenter.model.trimmer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class TargetFields implements Iterable<String> {

  private final Set<String> fieldSet;

  public TargetFields(@Nonnull Set<String> fieldSet) {
    this.fieldSet = checkNotNull(fieldSet);
  }

  public static TargetFields at(@Nonnull String... fieldNames) {
    checkNotNull(fieldNames);
    return new TargetFields(Sets.newHashSet(fieldNames));
  }

  public static TargetFields at(@Nonnull Set<String> fieldNames) {
    checkNotNull(fieldNames);
    return new TargetFields(fieldNames);
  }

  public boolean within(@Nonnull ObjectNode objectNode) {
    checkNotNull(objectNode);
    Set<String> fieldsAtObjectNode = getFieldNames(objectNode);
    for (String fieldName : fieldSet) {
      if (fieldsAtObjectNode.contains(fieldName)) {
        return true;
      }
    }
    return false;
  }

  public Set<String> getMatchingFields(@Nonnull ObjectNode objectNode) {
    checkNotNull(objectNode);
    Set<String> fieldsAtObjectNode = getFieldNames(objectNode);
    final Set<String> matchingFields = new HashSet<>();
    for (String fieldName : fieldSet) {
      if (fieldsAtObjectNode.contains(fieldName)) {
        matchingFields.add(fieldName);
      }
    }
    return matchingFields;
  }

  private static Set<String> getFieldNames(ObjectNode objectNode) {
    return Sets.newHashSet(objectNode.fieldNames());
  }

  @Override
  public Iterator<String> iterator() {
    return fieldSet.iterator();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (!(o instanceof TargetFields)) {
      return false;
    }
    TargetFields other = (TargetFields) o;
    return Objects.equal(fieldSet, other.fieldSet);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(fieldSet);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .addValue(fieldSet)
        .toString();
  }
}
