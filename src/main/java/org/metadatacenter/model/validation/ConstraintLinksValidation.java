package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;

/** Uses the same links definition as saved artifact validation. */
public final class ConstraintLinksValidation {
  private static final JsonSchema SCHEMA = load();
  private ConstraintLinksValidation() {}
  private static JsonSchema load() {
    try (var in = ConstraintLinksValidation.class.getResourceAsStream("/iri-field-meta-schema.json")) {
      ObjectNode document = (ObjectNode) new ObjectMapper().readTree(in);
      document.put("$ref", "#/definitions/valueConstraintsLinksFieldContent");
      return JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).getSchema(document);
    } catch (java.io.IOException e) { throw new ExceptionInInitializerError(e); }
  }
  public static void validate(JsonNode links) {
    if (links == null || !SCHEMA.validate(links).isEmpty())
      throw new IllegalArgumentException("links must contain only optional landingPage and icon HTTP(S) URLs");
  }
}
