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

public class Validate
{
  private static final ObjectMapper MAPPER = JacksonUtils.newMapper();

  public static void main(String[] args) throws ProcessingException, IOException, URISyntaxException
  {
    if (args.length != 2)
      Usage();

    CEDARValidator cedarValidator = new CEDARValidator();

    File schemaFile = new File(args[0]);
    File instanceFile = new File(args[1]);
    JsonNode schema = MAPPER.readTree(schemaFile);
    JsonNode instance = MAPPER.readTree(instanceFile);

    ProcessingReport processingReport = cedarValidator.validate(schema, instance);

    for (ProcessingMessage processingMessage : processingReport) {
      processingMessage.setLogLevel(LogLevel.DEBUG);
      System.out.println("Message: " + processingMessage.getMessage());
    }
  }

  private static void Usage()
  {
    System.err.println("Usage: " + Validate.class.getName() + " <schemaFileName> <instanceFileName>");
    System.exit(1);
  }
}
