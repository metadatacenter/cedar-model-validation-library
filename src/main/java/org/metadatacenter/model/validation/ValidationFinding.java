package org.metadatacenter.model.validation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

/** A validation fact. Save policy is supplied by the artifact's context, never by a host's code list. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ValidationFinding(String code, Category category, String path, String message,
                                VerificationStatus verificationStatus) {
  public enum Category {
    STRUCTURAL("structural"), CONSTRAINT("constraint"), COMPLETENESS("completeness"), VERIFICATION("verification");
    private final String value;
    Category(String value) { this.value = value; }
    @JsonValue public String value() { return value; }
  }
  public enum VerificationStatus {
    PASSED("passed"), FAILED("failed"), NOT_CHECKED("notChecked"), UNAVAILABLE("unavailable");
    private final String value;
    VerificationStatus(String value) { this.value = value; }
    @JsonValue public String value() { return value; }
  }
  public ValidationFinding(String code, Category category, String path, String message) {
    this(code, category, path, message, null);
  }
  public ValidationFinding {
    java.util.Objects.requireNonNull(code);
    java.util.Objects.requireNonNull(category);
    java.util.Objects.requireNonNull(message);
    path = path == null || path.isEmpty() ? "/" : path;
  }
}
