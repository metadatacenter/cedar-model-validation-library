package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

public class JsonUtils {

  public static String removeFieldFromDocument(String jsonDocument, String pathToRemove) {
    checkNotNull(jsonDocument);
    checkNotNull(pathToRemove);
    ObjectNode jsonObject = getJsonObject(jsonDocument);
    try {
      int rootLevel = 1; // by default
      ObjectNode resultObject = visitChildrenAndRemove(jsonObject, pathToRemove, rootLevel);
      return resultObject.toString();
    } catch (IllegalArgumentException e) {
      throw new RuntimeException(String.format("%s: error: %s", pathToRemove, e));
    }
  }

  private static ObjectNode visitChildrenAndRemove(JsonNode parentNode, String path, int level) {
    String fieldName = getFieldNameAt(path, level);
    ObjectNode objectNode = checkObjectNode(parentNode);
    if (objectNode.has(fieldName)) {
      if (isEndReference(fieldName, path)) {
        objectNode.remove(fieldName);
      } else {
        JsonNode childNode = parentNode.get(fieldName);
        ObjectNode modifiedNode = visitChildrenAndRemove(childNode, path, ++level);
        objectNode.replace(fieldName, modifiedNode);
      }
      return objectNode;
    }
    throw new IllegalArgumentException("Unable to find field name: " + fieldName);
  }

  private static String getFieldNameAt(String path, int level) {
    return path.split("/")[level];
  }

  private static ObjectNode checkObjectNode(JsonNode node) {
    if (!node.isObject()) {
      throw new IllegalArgumentException("Only support paths leading to an object node");
    }
    return (ObjectNode) node;
  }

  private static ObjectNode getJsonObject(String jsonDocument) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return (ObjectNode) mapper.readTree(jsonDocument);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean isEndReference(String fieldName, String path) {
    String endReference = path.substring(path.lastIndexOf("/") + 1);
    return fieldName.equals(endReference);
  }
}
