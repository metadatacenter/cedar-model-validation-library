package org.metadatacenter.model.validation;

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

  private final JsonNode rootNode;

  public JsonLdDocument(@Nonnull JsonNode rootNode) {
    this.rootNode = checkNotNull(rootNode);
  }

  public JsonNode asJsonLd() {
    return rootNode;
  }

  public JsonNode asJson() throws IOException {
    JsonNode jsonDocument = new JSONTreeTrimmer(rootNode)
        .collapse(at(JsonLdToken.ID), whenFound(IDENTIFIER_PATTERN))
        .collapse(at(JsonLdToken.VALUE))
        .prune(at(JsonLdToken.AllTokensSpec10))
        .trim();
    return jsonDocument;
  }

  public String asRdf() throws JsonLdError, IOException {
    Map<String, Object> jsonMap = createJsonMap(rootNode);
    return JsonLdProcessor.toRDF(jsonMap, new NQuadTripleCallback()).toString();
  }

  private Map createJsonMap(JsonNode jsonNode) {
    return new ObjectMapper().convertValue(jsonNode, Map.class);
  }

  private static ObjectNode createTypeIdNode() {
    final ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
    objectNode.set("@type", JsonNodeFactory.instance.textNode("@id"));
    return objectNode;
  }
}
