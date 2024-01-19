package org.metadatacenter.model.validation.exec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JacksonUtils;
import org.metadatacenter.model.validation.CedarValidator;
import org.metadatacenter.model.validation.ModelValidator;
import org.metadatacenter.model.validation.report.ErrorItem;
import org.metadatacenter.model.validation.report.ValidationReport;

import java.io.File;

public class ValidateTemplateField {
  private static final ObjectMapper MAPPER = JacksonUtils.newMapper();

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      Usage();
    }
    File templateFieldFile = new File(args[0]);
    JsonNode templateFieldNode = MAPPER.readTree(templateFieldFile);

    ModelValidator cedarModelValidator = new CedarValidator();
    ValidationReport validationReport = cedarModelValidator.validateTemplateField(templateFieldNode);

    if (validationReport.getValidationStatus().equals("true")) {
      System.out.println("Field is valid");
    } else {
      System.out.println("Field is invalid. Found " + validationReport.getErrors().size() + " error(s)");
      for (ErrorItem errorItem : validationReport.getErrors()) {
        System.out.println("[ERROR]: " + errorItem.getMessage() + ", location: " + errorItem.getLocation());
      }
    }
  }

  private static void Usage() {
    System.err.println("Usage: " + ValidateTemplateField.class.getName() + " <templateFieldFileName>");
    System.exit(1);
  }
}
