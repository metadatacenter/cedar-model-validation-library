package org.metadatacenter.model.validation.exec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JacksonUtils;
import org.metadatacenter.model.validation.CEDARModelValidator;
import org.metadatacenter.model.validation.report.ErrorItem;
import org.metadatacenter.model.validation.report.ValidationReport;

import java.io.File;
import java.io.IOException;

public class ValidateTemplate {
  private static final ObjectMapper MAPPER = JacksonUtils.newMapper();

  public static void main(String[] args) {
    if (args.length != 1) {
      Usage();
    }

    try {
      File templateFile = new File(args[0]);
      JsonNode templateNode = MAPPER.readTree(templateFile);

      CEDARModelValidator cedarModelValidator = new CEDARModelValidator();
      ValidationReport validationReport = cedarModelValidator.validateTemplate(templateNode);
      for (ErrorItem errorItem : validationReport.getErrors()) {
        System.out.println("Message: " + errorItem.getMessage());
      }
    } catch (IOException e) {
      System.err.println("IO exception: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      System.err.println("Illegal argument exception: " + e.getMessage());
    }
  }

  private static void Usage() {
    System.err.println("Usage: " + ValidateTemplate.class.getName() + " <templateFileName>");
    System.exit(1);
  }
}
