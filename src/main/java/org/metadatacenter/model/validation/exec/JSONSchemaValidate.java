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

public class JSONSchemaValidate {
  private static final ObjectMapper MAPPER = JacksonUtils.newMapper();

  public static void main(String[] args) throws ProcessingException, IOException, URISyntaxException {
    if (args.length != 2) {
      Usage();
    }

    CEDARModelValidator cedarModelValidator = new CEDARModelValidator();

    File schemaFile = new File(args[0]);
    File instanceFile = new File(args[1]);
    JsonNode schema = MAPPER.readTree(schemaFile);
    JsonNode instance = MAPPER.readTree(instanceFile);

    ValidationReport validationReport = cedarModelValidator.validateTemplateInstance(instance, schema);

    for (ErrorItem errorItem : validationReport.getErrors()) {
      System.out.println("Message: " + errorItem.getMessage());
    }
  }

  private static void Usage() {
    System.err.println("Usage: " + JSONSchemaValidate.class.getName() + " <schemaFileName> <instanceFileName>");
    System.exit(1);
  }
}
