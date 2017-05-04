package org.metadatacenter.model.validation.exec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import org.metadatacenter.model.validation.CEDARModelValidator;
import org.metadatacenter.model.validation.report.ErrorItem;
import org.metadatacenter.model.validation.report.ValidationReport;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class ValidateTemplateField {
  private static final ObjectMapper MAPPER = JacksonUtils.newMapper();

  public static void main(String[] args) throws ProcessingException, IOException, URISyntaxException {
    if (args.length != 1) {
      Usage();
    }

    try {
      File templateFieldFile = new File(args[0]);
      JsonNode templateFieldNode = MAPPER.readTree(templateFieldFile);

      CEDARModelValidator cedarModelValidator = new CEDARModelValidator();
      ValidationReport validationReport = cedarModelValidator.validateTemplateField(templateFieldNode);
      for (ErrorItem errorItem : validationReport.getErrors()) {
        System.out.println("Message: " + errorItem.getMessage());
      }
    } catch (ProcessingException e) {
      System.err.println("Processing exception: " + e.getMessage());
    } catch (IOException e) {
      System.err.println("IO exception: " + e.getMessage());
    } catch (URISyntaxException e) {
      System.err.println("URI syntax exception: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      System.err.println("Illegal argument exception: " + e.getMessage());
    }
  }

  private static void Usage() {
    System.err.println("Usage: " + ValidateTemplateField.class.getName() + " <templateFieldFileName>");
    System.exit(1);
  }
}
