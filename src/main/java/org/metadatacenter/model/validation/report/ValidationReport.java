package org.metadatacenter.model.validation.report;

import java.util.Collection;
import org.metadatacenter.model.validation.ValidationFinding;

public interface ValidationReport {

  String getValidationStatus();

  default boolean canSave() { return getErrors().isEmpty(); }

  default boolean isValid() { return getErrors().isEmpty() && getWarnings().isEmpty(); }

  default Collection<ValidationFinding> getFindings() { return java.util.List.of(); }

  Collection<WarningItem> getWarnings();

  Collection<ErrorItem> getErrors();
}
