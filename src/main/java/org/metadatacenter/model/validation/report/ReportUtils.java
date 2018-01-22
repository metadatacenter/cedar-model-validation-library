package org.metadatacenter.model.validation.report;

import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

public class ReportUtils {

  public static void outputLogger(Logger logger, ValidationReport report, boolean printWarning) {
    checkNotNull(logger);
    checkNotNull(report);
    StringBuilder logBuilder = new StringBuilder();
    if (report.getValidationStatus().equals("false")) {
      for (ErrorItem errorItem : report.getErrors()) {
        String errorMessage = String.format("%s: error: %s", errorItem.getLocation(), errorItem.getMessage());
        logBuilder.append(errorMessage);
        logBuilder.append("\n");
      }
    }
    if (printWarning) {
      for (WarningItem warningItem : report.getWarnings()) {
        String warningMessage = String.format("%s: warning: %s", warningItem.getLocation(), warningItem.getMessage());
        logBuilder.append(warningMessage);
        logBuilder.append("\n");
      }
    }
    String logMessage = logBuilder.toString();
    logger.info(logMessage);
  }
}
