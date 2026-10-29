package org.metadatacenter.model.validation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ConstraintLinksValidationTest {
  private final ObjectMapper json = new ObjectMapper();
  @Test void onlyTheTwoOptionalUrlKeysAreAccepted() throws Exception {
    for (String valid : new String[]{"{\"landingPage\":\"https://example.org/record\"}",
        "{\"icon\":\"https://example.org/icon.svg\"}",
        "{\"landingPage\":\"https://example.org/record\",\"icon\":\"https://example.org/icon.svg\"}"})
      assertDoesNotThrow(() -> ConstraintLinksValidation.validate(json.readTree(valid)));
    for (String invalid : new String[]{"{}", "null", "[]", "{\"landingPage\":null}",
        "{\"landingPage\":\"javascript:alert(1)\"}", "{\"icon\":\"bioportal\"}",
        "{\"landingPage\":\"https://user:secret@example.org/\"}",
        "{\"landingPage\":\"https://example.org\",\"versionScope\":\"current\"}",
        "{\"landingPage\":\"https://example.org\",\"provider\":{}}"})
      assertThrows(IllegalArgumentException.class, () -> ConstraintLinksValidation.validate(json.readTree(invalid)), invalid);
  }
}
