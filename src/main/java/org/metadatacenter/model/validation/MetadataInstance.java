package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nonnull;

import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public class MetadataInstance {

  private final JsonNode metadataInstance;

  public MetadataInstance(@Nonnull JsonNode metadataInstance) {
    this.metadataInstance = checkNotNull(metadataInstance);
  }

  public JsonNode asOriginal() {
    return metadataInstance;
  }

  public JsonNode asJson() {
    return new JSONTreeTrimmer(metadataInstance)
        .prune(PRUNING_SET)
        .collapse(JSONLDToken.VALUE)
        .trim();
  }

  private final static Set<String> PRUNING_SET = JSONLDToken.AllTokensSpec10.stream()
      .filter(token -> !token.equals(JSONLDToken.VALUE))
      .collect(Collectors.toSet());
}
