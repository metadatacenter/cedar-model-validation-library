package org.metadatacenter.model.validation;

import com.google.common.collect.Sets;
import org.metadatacenter.model.validation.report.ErrorItem;
import org.metadatacenter.model.validation.report.ValidationReport;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class BaseValidationTest {

  protected static void assertValidationStatus(ValidationReport validationReport, String expectedValue) {
    String statusMessage = validationReport.getValidationStatus();
    assertThat(printReason(validationReport), statusMessage, is(expectedValue));
  }

  protected static void assertValidationMessage(ValidationReport validationReport, String expectedValue) {
    Set<String> errorMessages = Sets.newHashSet();
    for (ErrorItem errorItem : validationReport.getErrors()) {
      String errorMessage = errorItem.getMessage();
      errorMessages.add(errorMessage);
    }
    assertThat(printReason(validationReport), errorMessages.contains(expectedValue));
  }

  /** Asserts that one error carries both the message and the location, as an editor receives them. */
  protected static void assertValidationMessage(ValidationReport validationReport, String expectedMessage,
      String expectedLocation) {
    boolean found = validationReport.getErrors().stream()
        .anyMatch(error -> expectedMessage.equals(error.getMessage()) && expectedLocation.equals(error.getLocation()));
    assertThat(printReason(validationReport), found);
  }

  protected static String printReason(ValidationReport validationReport) {
    return "The model validator is returning a different validation report.\n(application/json): " + validationReport
        .toString();
  }
}
