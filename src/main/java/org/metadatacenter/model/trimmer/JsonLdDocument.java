package org.metadatacenter.model.trimmer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.impl.NQuadTripleCallback;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.metadatacenter.model.trimmer.MatchingPattern.whenFound;
import static org.metadatacenter.model.trimmer.TargetFields.at;

public class JsonLdDocument {

  private static final ObjectNode IDENTIFIER_PATTERN = createTypeIdNode();

  private final JsonNode rootNode;

  public JsonLdDocument(JsonNode rootNode) {
    this.rootNode = checkNotNull(rootNode);
  }

  public JsonNode asJsonLd() {
    return rootNode;
  }

  public JsonNode asJson() {
    JsonNode jsonDocument = new JsonTrimmer(rootNode)
        .collapse(at(JsonLdToken.JSON_LD_ID), whenFound(IDENTIFIER_PATTERN))
        .collapse(at(JsonLdToken.JSON_LD_VALUE))
        .prune(at(JsonLdToken.AllTokensSpec10))
        .trim();
    return jsonDocument;
  }

  public String asRdf() throws JsonLdError {
    Map<String, Object> jsonMap = createJsonMap(rootNode);
    return JsonLdProcessor.toRDF(jsonMap, new NQuadTripleCallback()).toString();
  }

  private Map createJsonMap(JsonNode jsonNode) {
    return JsonLdMapper.MAPPER.convertValue(jsonNode, Map.class);
  }

  private static ObjectNode createTypeIdNode() {
    final ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
    objectNode.set("@type", JsonNodeFactory.instance.textNode("@id"));
    return objectNode;
  }
}
