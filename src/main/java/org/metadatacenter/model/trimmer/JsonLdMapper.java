package org.metadatacenter.model.trimmer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * The mapper the trimmer converts documents with. It handles {@code java.time} values through one
 * {@link JavaTimeModule}; Jackson ignores a second module with the same type id, so a customised copy
 * registered after a stock one would never take effect.
 */
public final class JsonLdMapper {

  private JsonLdMapper() {
  }

  public static final ObjectMapper MAPPER;

  static {
    MAPPER = new ObjectMapper();
    MAPPER.registerModule(new JavaTimeModule());
    MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }
}
