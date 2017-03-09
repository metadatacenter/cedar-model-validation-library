package org.metadatacenter.model.validation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.impl.NQuadTripleCallback;
import org.metadatacenter.model.trimmer.JSONTreeTrimmer;

import javax.annotation.Nonnull;

import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.metadatacenter.model.trimmer.MatchingPattern.whenFound;
import static org.metadatacenter.model.trimmer.TargetFields.at;

public class JsonLdDocument {

  private static final ObjectNode IDENTIFIER_PATTERN = createTypeIdNode();

  private final String jsonldString;

  public JsonLdDocument(@Nonnull String jsonldString) {
    this.jsonldString = checkNotNull(jsonldString);
  }

  public String asOriginal() {
    return jsonldString;
  }

  public String asJson() throws IOException {
    final JsonNode rootNode = createJsonNode(jsonldString);
    JsonNode jsonDocument = new JSONTreeTrimmer(rootNode)
        .collapse(at(JSONLDToken.ID), whenFound(IDENTIFIER_PATTERN))
        .collapse(at(JSONLDToken.VALUE))
        .prune(at(JSONLDToken.AllTokensSpec10))
        .trim();
    return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jsonDocument);
  }

  public String asRdf() throws JsonLdError, IOException {
    Map<String, Object> result = new ObjectMapper().readValue(
        jsonldString, new TypeReference<Map<String, Object>>() {});
    return JsonLdProcessor.toRDF(result, new NQuadTripleCallback()).toString();
  }

  private JsonNode createJsonNode(String inputString) throws IOException {
    return new ObjectMapper().readTree(inputString);
  }

  private static ObjectNode createTypeIdNode() {
    final ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
    objectNode.set("@type", JsonNodeFactory.instance.textNode("@id"));
    return objectNode;
  }
}
