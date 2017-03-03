package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class MetadataInstanceTest {

  public static void main(String[] args) throws Exception {
//    String filename = args[0];

    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = mapper.readTree(new File("/Users/jhardi/Documents/Projects/CEDAR/cedar-model-validation-library/target/test-classes/metadata-example.txt"));

    JsonNode output = new MetadataInstance(root).asJson();
    String prettyPrint = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(output);

    System.out.println(prettyPrint);
  }
}
