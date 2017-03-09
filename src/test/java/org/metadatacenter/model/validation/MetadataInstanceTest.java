package org.metadatacenter.model.validation;

import com.github.jsonldjava.core.JsonLdError;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class MetadataInstanceTest {

  public static void main(String[] args) throws Exception {
    String option = args[0];
    String filename = args[1];

    InputStream in = MetadataInstanceTest.class.getClassLoader().getResourceAsStream(filename);
    String input = IOUtils.toString(in, "UTF-8");

    if ("-json".equals(option)) {
      printJson(input);
    } else if ("-rdf".equals(option)) {
      printRdf(input);
    }
  }

  private static void printRdf(String input) throws JsonLdError, IOException {
    String rdfPrint = new TemplateInstance(input).asRdf();
    System.out.println(rdfPrint);
  }

  private static void printJson(String input) throws IOException {
    String jsonPrint = new TemplateInstance(input).asJson();
    System.out.println(jsonPrint);
  }
}
