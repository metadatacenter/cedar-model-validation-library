package org.metadatacenter.model.validation.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import org.metadatacenter.model.validation.ValidationFinding;
import org.metadatacenter.model.validation.ValidationPolicy;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonPropertyOrder({"validates", "canSave", "isValid", "findings", "warnings", "errors"})
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

  private final Set<ValidationFinding> findings = Sets.newLinkedHashSet();
  private final ValidationPolicy.Context context;

  private CedarValidationReport(ValidationPolicy.Context context) {
    this.context = context;
  }

  public static CedarValidationReport newEmptyReport() {
    return new CedarValidationReport(ValidationPolicy.Context.TEMPLATE);
  }

  public static CedarValidationReport newInstanceReport() {
    return new CedarValidationReport(ValidationPolicy.Context.INSTANCE);
  }

  @Override @JsonProperty("canSave")
  @Schema(name = "canSave", requiredMode = Schema.RequiredMode.REQUIRED,
      description = "Whether the context-specific save policy permits this artifact. Structural defects always block.")
  public boolean canSave() { return errorDetails.isEmpty(); }

  @Override @JsonProperty("isValid")
  @Schema(name = "isValid", requiredMode = Schema.RequiredMode.REQUIRED,
      description = "Whether every requested check passed, with no quality findings or unverified checks.")
  public boolean isValid() {
    return errorDetails.isEmpty() && warningDetails.isEmpty()
        && findings.stream().noneMatch(ValidationPolicy::isQualityProblem);
  }

  @Override @JsonProperty("findings")
  @Schema(name = "findings", requiredMode = Schema.RequiredMode.REQUIRED,
      description = "Stable rule codes, categories, JSON Pointer paths and explicit semantic verification outcomes.")
  public Set<ValidationFinding> getFindings() { return java.util.Collections.unmodifiableSet(findings); }

  public void addFinding(ValidationFinding finding) {
    checkNotNull(finding);
    if (!findings.add(finding) || !ValidationPolicy.isQualityProblem(finding)) return;
    if (ValidationPolicy.blocksSave(finding, context))
      errorDetails.add(new ErrorItem(finding.message(), finding.path()));
    else warningDetails.add(new WarningItem(finding.message(), finding.path()));
  }

  /** Preserve schema diagnostics while supplying the shared category and code. */
  public void addFinding(ValidationFinding finding, ErrorItem diagnostic) {
    addFinding(finding);
    if (!ValidationPolicy.isQualityProblem(finding)) return;
    if (ValidationPolicy.blocksSave(finding, context)) {
      errorDetails.remove(new ErrorItem(finding.message(), finding.path()));
      errorDetails.add(diagnostic);
    } else {
      warningDetails.remove(new WarningItem(finding.message(), finding.path()));
      WarningItem warning = new WarningItem(diagnostic.getMessage(), diagnostic.getLocation());
      diagnostic.getAdditionalInfo().forEach(warning::addAdditionalInfo);
      warningDetails.add(warning);
    }
  }

  /** Replace the previous semantic outcome while preserving unrelated findings at the same path. */
  public void resolveVerification(ValidationFinding finding) {
    var previous = findings.stream().filter(f -> f.verificationStatus() != null
        && f.path().equals(finding.path())
        && (f.code().startsWith("semantic") || f.code().startsWith("controlledTerm")
            || f.code().startsWith("authority"))).toList();
    previous.forEach(f -> {
      findings.remove(f);
      warningDetails.remove(new WarningItem(f.message(), f.path()));
      errorDetails.remove(new ErrorItem(f.message(), f.path()));
    });
    addFinding(finding);
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
    findings.add(new ValidationFinding("valueShape", ValidationFinding.Category.STRUCTURAL,
        error.getLocation(), error.getMessage()));
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
        && Objects.equal(errorDetails, other.errorDetails)
        && Objects.equal(findings, other.findings);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getValidationStatus(), warningDetails, errorDetails, findings);
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
