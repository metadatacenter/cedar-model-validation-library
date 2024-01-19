package org.metadatacenter.model.validation.exec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JacksonUtils;
import org.metadatacenter.model.validation.CedarValidator;
import org.metadatacenter.model.validation.ModelValidator;
import org.metadatacenter.model.validation.report.ErrorItem;
import org.metadatacenter.model.validation.report.ValidationReport;

import java.io.File;

public class ValidateTemplateElement {
  private static final ObjectMapper MAPPER = JacksonUtils.newMapper();

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      Usage();
    }
    File templateElementFile = new File(args[0]);
    JsonNode templateElementNode = MAPPER.readTree(templateElementFile);

    ModelValidator cedarModelValidator = new CedarValidator();
    ValidationReport validationReport = cedarModelValidator.validateTemplateElement(templateElementNode);

    if (validationReport.getValidationStatus().equals("true")) {
      System.out.println("Element is valid");
    } else {
      System.out.println("Element is invalid. Found " + validationReport.getErrors().size() + " error(s)");
      for (ErrorItem errorItem : validationReport.getErrors()) {
        System.out.println("[ERROR]: " + errorItem.getMessage() + ", location: " + errorItem.getLocation());
      }
    }
  }

  private static void Usage() {
    System.err.println("Usage: " + ValidateTemplateElement.class.getName() + " <templateElementFileName>");
    System.exit(1);
  }
}
