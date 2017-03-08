package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.metadatacenter.model.trimmer.JSONTreeTrimmer;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.metadatacenter.model.trimmer.JSONTreeTrimmer.at;
import static org.metadatacenter.model.trimmer.JSONTreeTrimmer.whenFound;

public class MetadataInstance {

  private final JsonNode metadataInstance;

  public MetadataInstance(@Nonnull JsonNode metadataInstance) { // TemplateInstance
    this.metadataInstance = checkNotNull(metadataInstance);
  }

  public JsonNode asOriginal() {
    return metadataInstance;
  }

  public JsonNode asJson() {
    ObjectNode identifierPattern = createTypeIdNode();
    return new JSONTreeTrimmer(metadataInstance)
        .collapse(at(JSONLDToken.ID), whenFound(identifierPattern))
        .collapse(at(JSONLDToken.VALUE))
        .prune(at(JSONLDToken.AllTokensSpec10))
        .trim();
  }

  private ObjectNode createTypeIdNode() {
    final ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
    objectNode.set("@type", JsonNodeFactory.instance.textNode("@id"));
    return objectNode;
  }
}
