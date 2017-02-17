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

public class CEDARModelValidator
{
  private static final ObjectMapper MAPPER = JacksonUtils.newMapper();
  private final JsonValidator jsonSchemaValidator;

  private static final String TEMPLATE_SCHEMA_NAME = "template";

  public CEDARModelValidator()
  {
    JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
    this.jsonSchemaValidator = factory.getValidator();
  }

  /**
   * Take a JSON Schema node and validate a JSON node against it.
   *
   * @param schemaNode   A node containing a JSON Schema document
   * @param instanceNode A node containing a JSON document
   * @return A processing report
   * @throws ProcessingException If a processing exception occurs during processing
   * @throws IOException         If an IO exception occurs during processing
   * @throws URISyntaxException  If a URI syntax exception occurs during processing
   */
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
