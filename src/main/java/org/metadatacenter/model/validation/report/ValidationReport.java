package org.metadatacenter.model.validation.report;

import java.util.Collection;
import java.util.List;

public interface ValidationReport {

  String getValidationStatus();

  Collection<WarningItem> getWarnings();

  Collection<ErrorItem> getErrors();
}
