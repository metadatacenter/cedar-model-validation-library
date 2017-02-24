package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class ValidateTemplateElement
{
  private static final ObjectMapper MAPPER = JacksonUtils.newMapper();

  public static void main(String[] args)
  {
    if (args.length != 1)
      Usage();

    try {
      File templateElementFile = new File(args[0]);
      JsonNode templateElementNode = MAPPER.readTree(templateElementFile);

      CEDARModelValidator cedarModelValidator = new CEDARModelValidator();

      Optional<ProcessingReport> processingReport = cedarModelValidator
        .validateTemplateElementNode(templateElementNode);

      if (processingReport.isPresent()) {
        for (ProcessingMessage processingMessage : processingReport.get()) {
          processingMessage.setLogLevel(LogLevel.DEBUG);
          System.out.println("Message: " + processingMessage.getMessage());
        }
      } else
        System.out.println("Template element is valid");
    } catch (IOException e) {
      System.err.println("IO exception: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      System.err.println("Illegal argument exception: " + e.getMessage());
    }
  }

  private static void Usage()
  {
    System.err.println("Usage: " + ValidateTemplateElement.class.getName() + " <templateElementFileName>");
    System.exit(1);
  }
}
