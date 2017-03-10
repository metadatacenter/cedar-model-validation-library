package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jsonldjava.core.JsonLdError;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class JsonLdDocumentMain {

  private static final ObjectMapper mapper = new ObjectMapper();

  /*
   * Example use:
   *    $ java JsonLdDocumentMain -json example.jsonld
   *    $ java JsonLdDocumentMain -rdf example.jsonld
   */
  public static void main(String[] args) throws Exception {
    String option = args[0];
    String filename = args[1];

    InputStream in = JsonLdDocumentMain.class.getClassLoader().getResourceAsStream(filename);
    JsonNode rootNode = mapper.readTree(in);

    if ("-json".equals(option)) {
      printJson(rootNode);
    } else if ("-rdf".equals(option)) {
      printRdf(rootNode);
    }
  }

  private static void printRdf(JsonNode rootNode) throws JsonLdError, IOException {
    String rdfPrint = new JsonLdDocument(rootNode).asRdf();
    System.out.println(rdfPrint);
  }

  private static void printJson(JsonNode rootNode) throws IOException {
    JsonNode jsonNode = new JsonLdDocument(rootNode).asJson();
    String jsonPrint = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    System.out.println(jsonPrint);
  }
}
