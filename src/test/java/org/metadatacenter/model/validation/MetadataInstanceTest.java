package org.metadatacenter.model.validation;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

public class MetadataInstanceTest {

  public static void main(String[] args) throws Exception {
    String filename = args[0];

    InputStream in = MetadataInstanceTest.class.getClassLoader().getResourceAsStream(filename);
    String input = IOUtils.toString(in, "UTF-8");

    String jsonPrint = new TemplateInstance(input).asJson();
    System.out.println(jsonPrint);

    String rdfPrint = new TemplateInstance(input).asRdf();
    System.out.println(rdfPrint);
  }
}
