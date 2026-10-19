package org.metadatacenter.model.validation;

import java.net.URI;
import java.net.URISyntaxException;

/** Identifier positions use Java URI spelling; field values use IriReference and retain RDF spelling. */
public final class IdentifierPolicy {
  private IdentifierPolicy() {}

  public static URI reference(String value) throws URISyntaxException {
    IriReference.toUri(value);
    return new URI(value);
  }

  public static URI absolute(String value) throws URISyntaxException {
    URI uri = reference(value);
    if (!uri.isAbsolute()) throw new URISyntaxException(value, "An identifier must be an absolute IRI");
    return uri;
  }

  public static boolean isValid(String value) {
    if (value == null) return false;
    try { absolute(value); return true; }
    catch (URISyntaxException e) { return false; }
  }
}
