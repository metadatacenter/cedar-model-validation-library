package org.metadatacenter.model.validation;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

public class JsonUtils {

  /**
   * Removes the member the JSON Pointer {@code pathToRemove} identifies. The pointer is resolved in full,
   * so a path that repeats its last segment, such as {@code /properties/Study Name/properties}, removes the
   * nested member and never the root's.
   */
  public static String removeFieldFromDocument(String jsonDocument, String pathToRemove) {
    checkNotNull(jsonDocument);
    checkNotNull(pathToRemove);
    ObjectNode jsonObject = getJsonObject(jsonDocument);
    JsonPointer pointer = JsonPointer.compile(pathToRemove);
    JsonNode parent = jsonObject.at(pointer.head());
    String fieldName = pointer.last().getMatchingProperty();
    if (!parent.isObject() || !parent.has(fieldName)) {
      throw new RuntimeException(String.format("%s: error: Unable to find field name: %s", pathToRemove, fieldName));
    }
    ((ObjectNode) parent).remove(fieldName);
    return jsonObject.toString();
  }

  private static ObjectNode getJsonObject(String jsonDocument) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return (ObjectNode) mapper.readTree(jsonDocument);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
