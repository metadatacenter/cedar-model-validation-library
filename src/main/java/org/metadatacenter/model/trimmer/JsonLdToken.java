package org.metadatacenter.model.trimmer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JsonLdToken {

  /**
   * Based on JSON 1.0 specification document:
   * https://www.w3.org/TR/2014/REC-json-ld-20140116/#syntax-tokens-and-keywords
   */
  public static final String JSON_LD_CONTEXT = "@context";
  public static final String JSON_LD_ID = "@id";
  public static final String JSON_LD_VALUE = "@value";
  public static final String JSON_LD_LANGUAGE = "@language";
  public static final String JSON_LD_TYPE = "@type";
  public static final String JSON_LD_CONTAINER = "@container";
  public static final String JSON_LD_LIST = "@list";
  public static final String JSON_LD_SET = "@set";
  public static final String JSON_LD_REVERSE = "@reverse";
  public static final String JSON_LD_INDEX = "@index";
  public static final String JSON_LD_BASE = "@base";
  public static final String JSON_LD_VOCAB = "@vocab";
  public static final String JSON_LD_GRAPH = "@graph";

  /**
   * Additions based on JSON 1.1 specification document:
   * https://www.w3.org/TR/2014/REC-json-ld-20140116/#syntax-tokens-and-keywords
   */
  public static final String NEST = "@nest";

  public static final Set<String> AllTokensSpec10 = new HashSet<>(
      Arrays.asList(JSON_LD_CONTEXT, JSON_LD_ID, JSON_LD_VALUE, JSON_LD_LANGUAGE, JSON_LD_TYPE, JSON_LD_CONTAINER,
        JSON_LD_LIST, JSON_LD_SET, JSON_LD_REVERSE, JSON_LD_INDEX, JSON_LD_BASE, JSON_LD_VOCAB, JSON_LD_GRAPH));

  public static final Set<String> AllTokensSpec11 = new HashSet<>(
      Arrays.asList(JSON_LD_CONTEXT, JSON_LD_ID, JSON_LD_VALUE, JSON_LD_LANGUAGE, JSON_LD_TYPE, JSON_LD_CONTAINER,
        JSON_LD_LIST, JSON_LD_SET, JSON_LD_REVERSE, JSON_LD_INDEX, JSON_LD_BASE, JSON_LD_VOCAB, JSON_LD_GRAPH, NEST));
}
