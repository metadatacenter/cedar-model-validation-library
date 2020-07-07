package org.metadatacenter.model.trimmer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class JsonLdMapper {

  public static final String xsdDateTimeFormatterString = "uuuu-MM-dd'T'HH:mm:ssZZZZZ";
  public static final DateTimeFormatter xsdDateTimeFormatter =
      DateTimeFormatter.ofPattern(xsdDateTimeFormatterString).withZone(ZoneId.systemDefault());

  private JsonLdMapper() {
  }

  public static final ObjectMapper MAPPER;

  static {
    MAPPER = new ObjectMapper();
    MAPPER.registerModule(new JavaTimeModule());
    JavaTimeModule javaTimeModule = new JavaTimeModule();
    javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(xsdDateTimeFormatter));
    javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(xsdDateTimeFormatter));
    MAPPER.registerModule(javaTimeModule);
    MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }
}
