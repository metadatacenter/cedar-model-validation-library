package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import org.metadatacenter.model.validation.report.ValidationReport;

public interface ModelValidator {

  ValidationReport validateTemplate(JsonNode templateNode) throws Exception;

  ValidationReport validateTemplateElement(JsonNode elementNode) throws Exception;

  ValidationReport validateTemplateField(JsonNode fieldNode) throws Exception;

  ValidationReport validateTemplateInstance(JsonNode instanceNode, JsonNode instanceSchema) throws Exception;
}
