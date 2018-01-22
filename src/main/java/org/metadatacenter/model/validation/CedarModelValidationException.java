package org.metadatacenter.model.validation;

import com.fasterxml.jackson.core.JsonPointer;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class CedarModelValidationException extends Exception {

  private final Multimap<JsonPointer, ProcessingMessage> validationMessages =
      Multimaps.newSetMultimap(Maps.newHashMap(), HashSet::new);

  public CedarModelValidationException() {
    super();
  }

  public void addProcessingReport(ProcessingReport report) {
    checkNotNull(report);
    for (ProcessingMessage message : report) {
      addProcessingMessage(message);
    }
  }

  public void addProcessingReport(ProcessingReport report, JsonPointer baseLocation) {
    checkNotNull(report);
    checkNotNull(baseLocation);
    for (ProcessingMessage message : report) {
      addProcessingMessage(message, baseLocation);
    }
  }

  public void addProcessingMessage(ProcessingMessage message) {
    checkNotNull(message);
    validationMessages.put(JsonPointer.compile("/"), message);
  }

  public void addProcessingMessage(ProcessingMessage message, JsonPointer baseLocation) {
    checkNotNull(message);
    checkNotNull(baseLocation);
    validationMessages.put(baseLocation, message);
  }

  public Collection<ProcessingMessage> getProcessingMessages() {
    return Collections.unmodifiableCollection(validationMessages.values());
  }

  public Collection<ProcessingMessage> getErrorMessages() {
    Set<ProcessingMessage> errorMessages = Sets.newHashSet();
    for (ProcessingMessage message : getProcessingMessages()) {
      if (message.getLogLevel() == LogLevel.ERROR) {
        errorMessages.add(message);
      }
    }
    return errorMessages;
  }

  public Collection<ProcessingMessage> getWarningMessages() {
    Set<ProcessingMessage> warningMessages = Sets.newHashSet();
    for (ProcessingMessage message : getProcessingMessages()) {
      if (message.getLogLevel() == LogLevel.WARNING) {
        warningMessages.add(message);
      }
    }
    return warningMessages;
  }

  public Multimap<JsonPointer, ProcessingMessage> getDetails() {
    return validationMessages;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    boolean needSeparator = false;
    for (ProcessingMessage message : getProcessingMessages()) {
      if (needSeparator) {
        sb.append("\n");
      }
      sb.append(message.toString());
      needSeparator = true;
    }
    return sb.toString();
  }
}
