package org.metadatacenter.model.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.impl.NQuadTripleCallback;
import org.metadatacenter.model.trimmer.JSONTreeTrimmer;

import javax.annotation.Nonnull;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.metadatacenter.model.trimmer.JSONTreeTrimmer.at;
import static org.metadatacenter.model.trimmer.JSONTreeTrimmer.whenFound;

public class MetadataInstance {

  private final JsonNode rootNode;

  public MetadataInstance(@Nonnull JsonNode rootNode) {
    this.rootNode = checkNotNull(rootNode);
  }

  public String asOriginal() throws JsonProcessingException {
    return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
  }

  public String asJson() throws JsonProcessingException {
    ObjectNode identifierPattern = createTypeIdNode();
    JsonNode jsonDocument = new JSONTreeTrimmer(rootNode)
        .collapse(at(JSONLDToken.ID), whenFound(identifierPattern))
        .collapse(at(JSONLDToken.VALUE))
        .prune(at(JSONLDToken.AllTokensSpec10))
        .trim();
    return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jsonDocument);
  }

  public String asRdf() throws JsonLdError {
    Map<String, Object> result = new ObjectMapper().convertValue(rootNode, Map.class);
    return JsonLdProcessor.toRDF(result, new NQuadTripleCallback()).toString();
  }

  private ObjectNode createTypeIdNode() {
    final ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
    objectNode.set("@type", JsonNodeFactory.instance.textNode("@id"));
    return objectNode;
  }
}
