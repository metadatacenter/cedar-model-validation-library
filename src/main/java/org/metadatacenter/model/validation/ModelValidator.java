package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import org.metadatacenter.model.validation.report.ValidationReport;

public interface ModelValidator {

  ValidationReport validateTemplate(JsonNode templateNode) throws Exception;

  ValidationReport validateTemplateElement(JsonNode elementNode) throws Exception;

  ValidationReport validateTemplateField(JsonNode fieldNode) throws Exception;

  ValidationReport validateTemplateInstance(JsonNode instanceNode, JsonNode instanceSchema) throws Exception;

  /**
   * Validates an element instance against its element schema. A CEDAR element artifact is
   * itself the JSON Schema its instances validate against in situ, so this is the same
   * schema-against-document validation as {@link #validateTemplateInstance} — named so the
   * interface states that element instances are a supported validation target. The instance
   * is expected in its nested shape (as it would sit inside a parent instance).
   */
  ValidationReport validateElementInstance(JsonNode elementInstanceNode, JsonNode elementSchema) throws Exception;
}
