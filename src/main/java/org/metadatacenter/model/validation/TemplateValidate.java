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

public class TemplateValidate
{
  private static final ObjectMapper MAPPER = JacksonUtils.newMapper();

  public static void main(String[] args) throws ProcessingException, IOException, URISyntaxException
  {
    if (args.length != 1)
      Usage();

    File templateFile = new File(args[0]);
    JsonNode templateNode = MAPPER.readTree(templateFile);

    CEDARModelValidator cedarModelValidator = new CEDARModelValidator();
    Optional<ProcessingReport> processingReport = cedarModelValidator.validateTemplateNode(templateNode);

    if (processingReport.isPresent()) {
      for (ProcessingMessage processingMessage : processingReport.get()) {
        processingMessage.setLogLevel(LogLevel.DEBUG);
        System.out.println("Message: " + processingMessage.getMessage());
      }
    } else
      System.out.println("Template is valid");
  }

  private static void Usage()
  {
    System.err.println("Usage: " + TemplateValidate.class.getName() + " <templateFileName>");
    System.exit(1);
  }
}
