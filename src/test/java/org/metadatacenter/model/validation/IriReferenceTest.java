package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.SchemaValidatorsConfig;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.metadatacenter.model.validation.internal.FgeCompatFormats;
import static org.junit.jupiter.api.Assertions.*;

class IriReferenceTest {
  @ParameterizedTest
  @ValueSource(strings = {"https://example.org/Niger\u00a0NER", "https://example.org/Niger%C2%A0NER",
      "https://example.org/café", "https://example.org/\u2003term", "https://example.org/\uD83D\uDE00",
      "https://example.org/?q=\ue000", "urn:example:Niger\u00a0NER", "tmp-123", ""})
  void legacyUriFormatAcceptsIriCharactersWithoutRewriting(String value) throws Exception {
    assertNotNull(IriReference.toUri(value));
    var config = SchemaValidatorsConfig.builder().formatAssertionsEnabled(true).build();
    var schema = FgeCompatFormats.FACTORY.getSchema("{\"type\":\"string\",\"format\":\"uri\"}", config);
    var node = new ObjectMapper().getNodeFactory().textNode(value);
    assertTrue(schema.validate(node).isEmpty());
    assertEquals(value, node.asText());
  }

  @ParameterizedTest
  @ValueSource(strings = {"https://example.org/a b", "https://example.org/a\tb", "https://example.org/\u0085",
      "https://example.org/%xx", "https://example.org/a#b#c", "://example.org/a", "https://example.org/\ud800",
      "https://example.org/\uffff", "https://example.org/\ue000", "https://example.org/#\ue000"})
  void rejectsMalformedIri(String value) throws Exception {
    assertThrows(java.net.URISyntaxException.class, () -> IriReference.toUri(value));
    var config = SchemaValidatorsConfig.builder().formatAssertionsEnabled(true).build();
    var schema = FgeCompatFormats.FACTORY.getSchema("{\"type\":\"string\",\"format\":\"uri\"}", config);
    assertFalse(schema.validate(new ObjectMapper().getNodeFactory().textNode(value)).isEmpty());
  }
}
