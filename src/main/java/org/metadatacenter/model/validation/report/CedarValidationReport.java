package org.metadatacenter.model.validation.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonPropertyOrder({"validates", "warnings", "errors"})
@Schema(name = "ValidationReport",
    description = "What validating an artifact against the CEDAR model found. Returned with 200 "
        + "whether or not the artifact is valid: an invalid artifact is an answer, not a failed "
        + "request. The artifact server produces it, and the resource server hands the same "
        + "document back unchanged.")
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
  @Schema(name = "validates", requiredMode = Schema.RequiredMode.REQUIRED,
      allowableValues = {IS_VALID, IS_INVALID},
      description = "Whether the artifact validates, as a string. One error makes it false; "
          + "warnings alone still validate.")
  public String getValidationStatus() {
    return errorDetails.isEmpty() ? IS_VALID : IS_INVALID;
  }

  public void addWarning(WarningItem warning) {
    checkNotNull(warning);
    warningDetails.add(warning);
  }

  @Override
  @JsonProperty("warnings")
  @Schema(name = "warnings", requiredMode = Schema.RequiredMode.REQUIRED,
      description = "Everything the artifact should change but need not, in the order found.")
  public Set<WarningItem> getWarnings() {
    return warningDetails;
  }

  public void addError(ErrorItem error) {
    checkNotNull(error);
    errorDetails.add(error);
  }

  @Override
  @JsonProperty("errors")
  @Schema(name = "errors", requiredMode = Schema.RequiredMode.REQUIRED,
      description = "Everything that makes the artifact invalid, in the order found.")
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
