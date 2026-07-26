package org.metadatacenter.model.validation;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.networknt.schema.ValidationMessage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Carries the outcome of a single failed schema validation. Each instance corresponds to one
 * {@code doValidation} call, so it holds the messages from validating one node against one schema,
 * keyed by the base location of that node within the enclosing artifact. The optional schema
 * resource name identifies the meta-schema file the node was validated against, when one applies.
 */
public class CedarModelValidationException extends Exception {

  private final Multimap<JsonPointer, ValidationMessage> validationMessages =
      Multimaps.newSetMultimap(Maps.newHashMap(), HashSet::new);

  private String schemaResourceName;

  public CedarModelValidationException() {
    super();
  }

  public void addValidationMessages(Set<ValidationMessage> messages, JsonPointer baseLocation) {
    checkNotNull(messages);
    checkNotNull(baseLocation);
    for (ValidationMessage message : messages) {
      validationMessages.put(baseLocation, message);
    }
  }

  public void setSchemaResourceName(String schemaResourceName) {
    this.schemaResourceName = schemaResourceName;
  }

  public String getSchemaResourceName() {
    return schemaResourceName;
  }

  public Collection<ValidationMessage> getValidationMessages() {
    return validationMessages.values();
  }

  public Multimap<JsonPointer, ValidationMessage> getDetails() {
    return validationMessages;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    boolean needSeparator = false;
    for (ValidationMessage message : getValidationMessages()) {
      if (needSeparator) {
        sb.append("\n");
      }
      sb.append(message.getMessage());
      needSeparator = true;
    }
    return sb.toString();
  }
}
