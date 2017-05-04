package org.metadatacenter.model.trimmer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JsonLdToken {

  /**
   * Based on JSON 1.0 specification document:
   * https://www.w3.org/TR/2014/REC-json-ld-20140116/#syntax-tokens-and-keywords
   */
  public static final String CONTEXT = "@context";
  public static final String ID = "@id";
  public static final String VALUE = "@value";
  public static final String LANGUAGE = "@language";
  public static final String TYPE = "@type";
  public static final String CONTAINER = "@container";
  public static final String LIST = "@list";
  public static final String SET = "@set";
  public static final String REVERSE = "@reverse";
  public static final String INDEX = "@index";
  public static final String BASE = "@base";
  public static final String VOCAB = "@vocab";
  public static final String GRAPH = "@graph";

  /**
   * Additions based on JSON 1.1 specification document:
   * https://www.w3.org/TR/2014/REC-json-ld-20140116/#syntax-tokens-and-keywords
   */
  public static final String NEST = "@nest";

  public static final Set<String> AllTokensSpec10 = new HashSet<>(
      Arrays.asList(CONTEXT, ID, VALUE, LANGUAGE, TYPE,
          CONTAINER, LIST, SET, REVERSE, INDEX, BASE,
          VOCAB, GRAPH));

  public static final Set<String> AllTokensSpec11 = new HashSet<>(
      Arrays.asList(CONTEXT, ID, VALUE, LANGUAGE, TYPE,
          CONTAINER, LIST, SET, REVERSE, INDEX, BASE,
          VOCAB, GRAPH, NEST));
}
