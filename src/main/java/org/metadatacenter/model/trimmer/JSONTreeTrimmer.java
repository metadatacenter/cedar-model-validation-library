package org.metadatacenter.model.trimmer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

public class JSONTreeTrimmer {

  private final ObjectNode rootNode;

  private List<Operation> operationQueue = new ArrayList<>();

  public JSONTreeTrimmer(@Nonnull JsonNode rootNode) {
    checkNotNull(rootNode);
    this.rootNode = rootNode.deepCopy();
  }

  public static TargetFields at(@Nonnull String... fieldNames) {
    checkNotNull(fieldNames);
    return new TargetFields(Sets.newHashSet(fieldNames));
  }

  public static TargetFields at(@Nonnull Set<String> fieldNames) {
    checkNotNull(fieldNames);
    return new TargetFields(fieldNames);
  }

  /**
   * Removes all JSON nodes that have field names specified in the
   * <code>targetFields</code>.
   *
   * @param targetFields a set of field names to prune
   * @return a reference to this object
   */
  public JSONTreeTrimmer prune(@Nonnull TargetFields targetFields) {
    checkNotNull(targetFields);
    operationQueue.add(new PruneOperation(rootNode, targetFields));
    return this;
  }

  /**
   * Compacts all JSON object nodes into a single value or an array of values that
   * have field names specified in the <code>targetFields</code>.
   *
   * @param targetFields a set of field names to collapse
   * @return a reference to this object
   */
  public JSONTreeTrimmer collapse(@Nonnull TargetFields targetFields) {
    checkNotNull(targetFields);
    operationQueue.add(new CollapseOperation(rootNode, targetFields));
    return this;
  }

  /**
   * Returns the final JSON tree node from executing all the trimming operations
   */
  public ObjectNode trim() {
    for (Operation operation : operationQueue) {
      operation.evaluate();
    }
    return rootNode;
  }
}
