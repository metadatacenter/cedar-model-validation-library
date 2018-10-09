package org.metadatacenter.model.validation.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonPropertyOrder({"validates", "warnings", "errors"})
public class CedarValidationReport implements ValidationReport {

  public static final String IS_INVALID = "false";
  public static final String IS_VALID = "true";

  private final Set<WarningItem> warningDetails = Sets.newLinkedHashSet();
  private final Set<ErrorItem> errorDetails = Sets.newLinkedHashSet();

  // Prevent external initialization
  private CedarValidationReport() {
    // NO-OP
  }

  public static CedarValidationReport newEmptyReport() {
    return new CedarValidationReport();
  }

  @Override
  @JsonProperty("validates")
  public String getValidationStatus() {
    return errorDetails.isEmpty() ? IS_VALID : IS_INVALID;
  }

  public void addWarning(WarningItem warning) {
    checkNotNull(warning);
    warningDetails.add(warning);
  }

  @Override
  @JsonProperty("warnings")
  public Set<WarningItem> getWarnings() {
    return warningDetails;
  }

  public void addError(ErrorItem error) {
    checkNotNull(error);
    errorDetails.add(error);
  }

  @Override
  @JsonProperty("errors")
  public Set<ErrorItem> getErrors() {
    return errorDetails;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (!(o instanceof CedarValidationReport)) {
      return false;
    }
    CedarValidationReport other = (CedarValidationReport) o;
    return Objects.equal(getValidationStatus(), other.getValidationStatus())
        && Objects.equal(warningDetails, other.warningDetails)
        && Objects.equal(errorDetails, other.errorDetails);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getValidationStatus(), warningDetails, errorDetails);
  }

  @Override
  public String toString() {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Programming error", e);
    }
  }
}
