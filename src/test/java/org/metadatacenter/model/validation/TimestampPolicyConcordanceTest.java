package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.time.format.DateTimeParseException;
import static org.junit.jupiter.api.Assertions.*;

class TimestampPolicyConcordanceTest {
  @Test void normativeCorpusAndPreservation() throws Exception {
    try (var input = getClass().getResourceAsStream("/concordance/timestamp-policy.json")) {
      for (var row : new ObjectMapper().readTree(input).get("cases")) {
        String value = row.get("value").asText(), id = row.get("id").asText();
        if (row.get("canonical").isNull()) {
          assertThrows(DateTimeParseException.class, () -> TimestampPolicy.parse(value), id);
        } else {
          var parsed = TimestampPolicy.parse(value);
          String rendered = TimestampPolicy.format(parsed);
          assertEquals(row.get("canonical").asText(), rendered, id);
          assertEquals(parsed, TimestampPolicy.parse(rendered), id);
        }
      }
    }
  }
}
