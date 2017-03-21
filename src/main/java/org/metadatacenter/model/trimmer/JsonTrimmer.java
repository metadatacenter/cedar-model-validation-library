package org.metadatacenter.model.trimmer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.annotation.Nonnull;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

public class JsonTrimmer {

  private final ObjectNode rootNode;

  private List<Operation> operationQueue = new ArrayList<>();

  public JsonTrimmer(@Nonnull JsonNode rootNode) {
    checkNotNull(rootNode);
    this.rootNode = rootNode.deepCopy();
  }

  /**
   * Removes all JSON nodes that have field names specified in the
   * <code>targetFields</code>.
   *
   * @param targetFields a set of field names to prune
   * @return a reference to this object
   */
  public JsonTrimmer prune(@Nonnull TargetFields targetFields) {
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
  public JsonTrimmer collapse(@Nonnull TargetFields targetFields) {
    checkNotNull(targetFields);
    operationQueue.add(new CollapseOperation(rootNode, targetFields));
    return this;
  }

  /**
   * Compacts all JSON object nodes into a single value or an array of values that have
   * field names specified in the <code>targetFields</code> and the object structure
   * contains the node specified in the <code>matchingPattern</code>.
   *
   * @param targetFields      a set of field names to collapse
   * @param matchingPattern   a matching pattern for the target fields to collapse
   * @return a reference to this object
   */
  public JsonTrimmer collapse(@Nonnull TargetFields targetFields,
                              @Nonnull MatchingPattern matchingPattern) {
    checkNotNull(targetFields);
    checkNotNull(matchingPattern);
    CollapseOperation collapseOperation = new CollapseOperation(rootNode, targetFields);
    collapseOperation.setCondition(matchingPattern);
    operationQueue.add(collapseOperation);
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
