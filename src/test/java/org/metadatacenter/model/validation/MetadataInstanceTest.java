package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class MetadataInstanceTest {

  public static void main(String[] args) throws Exception {
    String filename = args[0];

    InputStream in = MetadataInstanceTest.class.getClassLoader().getResourceAsStream(filename);

    ObjectMapper mapper = new ObjectMapper();
    JsonNode input = mapper.readTree(in);

    JsonNode output = new MetadataInstance(input).asJson();
    String prettyPrint = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(output);

    System.out.println(prettyPrint);
  }
}
