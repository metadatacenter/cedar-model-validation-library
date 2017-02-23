package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

public class ValidateTemplateField
{
  private static final ObjectMapper MAPPER = JacksonUtils.newMapper();

  public static void main(String[] args) throws ProcessingException, IOException, URISyntaxException
  {
    if (args.length != 1)
      Usage();

    File templateFieldFile = new File(args[0]);
    JsonNode templateFieldNode = MAPPER.readTree(templateFieldFile);

    CEDARModelValidator cedarModelValidator = new CEDARModelValidator();
    //TODO: add real value for path argument. Maybe args[0]?
    Optional<ProcessingReport> processingReport = cedarModelValidator.validateTemplateFieldNode(templateFieldNode, null);

    if (processingReport.isPresent()) {
      for (ProcessingMessage processingMessage : processingReport.get()) {
        processingMessage.setLogLevel(LogLevel.DEBUG);
        System.out.println("Message: " + processingMessage.getMessage());
      }
    } else
      System.out.println("Template field is valid");
  }

  private static void Usage()
  {
    System.err.println("Usage: " + ValidateTemplateField.class.getName() + " <templateFieldFileName>");
    System.exit(1);
  }
}
