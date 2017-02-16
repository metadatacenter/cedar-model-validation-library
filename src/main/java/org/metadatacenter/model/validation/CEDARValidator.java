package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class CEDARValidator
{
  private static final ObjectMapper MAPPER = JacksonUtils.newMapper();
  private final JsonValidator jsonSchemaValidator;

  private static final String TEMPLATE_SCHEMA_NAME = "template";

  public CEDARValidator()
  {
    JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
    this.jsonSchemaValidator = factory.getValidator();
  }

  public ProcessingReport validate(JsonNode schemaNode, JsonNode instanceNode)
    throws ProcessingException, IOException, URISyntaxException
  {
    JsonSchema schema = JsonSchemaFactory.byDefault().getJsonSchema(schemaNode);
    ProcessingReport processingReport = schema.validate(instanceNode, false);

    return processingReport;
  }

  private JsonSchema loadSchema(String schemaName) throws IOException, URISyntaxException, ProcessingException
  {
    ClassLoader classLoader = getClass().getClassLoader();
    String url = classLoader.getResource(schemaName + ".json").toString();
    URI schemaURI = new URI(url);

    return JsonSchemaFactory.byDefault().getJsonSchema(schemaURI.toString());
  }
}
