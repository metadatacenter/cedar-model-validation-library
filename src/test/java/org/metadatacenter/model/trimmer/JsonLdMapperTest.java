package org.metadatacenter.model.trimmer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

public class JsonLdMapperTest {

  @Test
  public void javaTimeValuesAreWrittenAsIsoStrings() throws Exception {
    assertThat(JsonLdMapper.MAPPER.writeValueAsString(LocalDateTime.of(2026, 9, 2, 10, 30, 15)),
        is("\"2026-09-02T10:30:15\""));
    assertThat(JsonLdMapper.MAPPER.writeValueAsString(OffsetDateTime.of(2026, 9, 2, 10, 30, 15, 0, ZoneOffset.ofHours(-7))),
        is("\"2026-09-02T10:30:15-07:00\""));
  }

  @Test
  public void javaTimeValuesReadBack() throws Exception {
    assertThat(JsonLdMapper.MAPPER.readValue("\"2026-09-02T10:30:15\"", LocalDateTime.class),
        is(LocalDateTime.of(2026, 9, 2, 10, 30, 15)));
  }

  @Test
  public void aDocumentConvertsToAMap() throws Exception {
    JsonNode node = JsonLdMapper.MAPPER.readTree("{\"@id\":\"x\",\"pav:createdOn\":\"2026-09-02T10:30:15-07:00\"}");
    Map<?, ?> map = JsonLdMapper.MAPPER.convertValue(node, Map.class);
    assertThat(map.get("pav:createdOn"), is("2026-09-02T10:30:15-07:00"));
  }
}
