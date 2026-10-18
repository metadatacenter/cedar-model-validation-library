package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NumericPolicyConcordanceTest {
  @Test void normativeGrammarAndExactComparison() throws Exception {
    try (var input = getClass().getResourceAsStream("/concordance/numeric-policy.json")) {
      var corpus = new ObjectMapper().readTree(input);
      for (var row : corpus.get("cases")) {
        String value = row.get("value").asText();
        if (row.get("valid").asBoolean()) {
          assertDoesNotThrow(() -> NumericPolicy.parse(value), value);
          assertEquals(0, NumericPolicy.compare(value, NumericPolicy.number(value).toString()), value);
        } else assertThrows(NumberFormatException.class, () -> NumericPolicy.parse(value), value);
      }
      for (var row : corpus.get("comparisons"))
        assertEquals(row.get("order").asInt(), NumericPolicy.compare(row.get("left").asText(), row.get("right").asText()));
    }
  }
}
