package org.metadatacenter.model.validation;

import java.util.Set;

/** Templates must be consistent; instances may be incomplete or violate declared value constraints. */
public final class ValidationPolicy {
  private ValidationPolicy() {}
  public enum Context { TEMPLATE, INSTANCE }

  private static final Set<String> CONSTRAINTS = Set.of(
      "minLength", "maxLength", "regex", "pattern", "email", "link", "phoneNumber", "minValue", "maxValue",
      "decimalPlace", "temporalGranularity", "timezone", "choiceMembership", "maxItems",
      "controlledTermMembership", "authorityIdentifier", "authorityMembership");
  private static final Set<String> COMPLETENESS = Set.of("required", "minItems");
  private static final Set<String> VERIFICATION = Set.of(
      "regexPending", "regexEvaluation", "regexUnavailable", "semanticNotChecked", "semanticUnavailable",
      "controlledTermVerified", "authorityVerified");

  /** Stable categories shared with the TypeScript policy; unknown codes remain structural. */
  public static ValidationFinding.Category category(String code) {
    if (CONSTRAINTS.contains(code)) return ValidationFinding.Category.CONSTRAINT;
    if (COMPLETENESS.contains(code)) return ValidationFinding.Category.COMPLETENESS;
    if (VERIFICATION.contains(code)) return ValidationFinding.Category.VERIFICATION;
    return ValidationFinding.Category.STRUCTURAL;
  }

  /** The exact status/code pairs understood from each semantic service. Other responses are unavailable. */
  public static boolean acceptsSemanticResult(boolean controlled, String status, String code) {
    if ("passed".equals(status)) return (controlled ? "controlledTermVerified" : "authorityVerified").equals(code);
    if ("failed".equals(status)) return controlled ? "controlledTermMembership".equals(code)
        : "authorityIdentifier".equals(code) || "authorityMembership".equals(code);
    if ("notChecked".equals(status)) return "semanticNotChecked".equals(code);
    if ("unavailable".equals(status)) return "semanticUnavailable".equals(code);
    return false;
  }

  public static boolean blocksSave(ValidationFinding finding, Context context) {
    if (finding.verificationStatus() == ValidationFinding.VerificationStatus.PASSED) return false;
    if (finding.category() == ValidationFinding.Category.STRUCTURAL) return true;
    if (context == Context.INSTANCE) return false;
    if (finding.category() == ValidationFinding.Category.VERIFICATION)
      return finding.verificationStatus() == ValidationFinding.VerificationStatus.FAILED;
    return true;
  }

  public static boolean isQualityProblem(ValidationFinding finding) {
    return finding.verificationStatus() != ValidationFinding.VerificationStatus.PASSED;
  }
}
