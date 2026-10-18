package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TextPolicyConcordanceTest {
  @Test void normativeCorpus() throws Exception {
    try (var input = getClass().getResourceAsStream("/concordance/text-policy.json")) {
      for (var row : new ObjectMapper().readTree(input).get("cases")) {
        String value = row.get("value").asText(), id = row.get("id").asText();
        assertEquals(row.get("blank").asBoolean(), TextPolicy.isBlank(value), id);
        assertEquals(row.get("codePointLength").asInt(), TextPolicy.codePointLength(value), id);
        assertEquals(row.get("identifier").asBoolean(), IdentifierPolicy.isValid(value), id);
        boolean field = !value.isEmpty();
        try { IriReference.toUri(value); } catch (java.net.URISyntaxException e) { field = false; }
        assertEquals(row.get("fieldIri").asBoolean(), field, id);
      }
    }
  }

  @Test void pinsEveryJava17CodePoint() {
    for (int cp = 0; cp <= Character.MAX_CODE_POINT; cp++) {
      if (Character.isWhitespace(cp) != TextPolicy.isBlankCodePoint(cp))
        fail("Runtime whitespace changed at U+" + Integer.toHexString(cp));
    }
  }
}
