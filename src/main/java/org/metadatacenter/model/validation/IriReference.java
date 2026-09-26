package org.metadatacenter.model.validation;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

/** RFC 3987 character validation without changing the identifier's RDF spelling. */
public final class IriReference {
  private IriReference() {}

  /**
   * Return a URI usable for transport. Keep the original string separately for RDF identity:
   * percent-encoding is not an RDF equality normalization. Relative references remain supported
   * for CEDAR's existing unsaved identifiers; callers requiring an absolute IRI check isAbsolute().
   */
  public static URI toUri(String value) throws URISyntaxException {
    StringBuilder transport = new StringBuilder();
    boolean query = false;
    boolean fragment = false;
    for (int offset = 0; offset < value.length();) {
      int cp = value.codePointAt(offset);
      if (cp == '#') { fragment = true; query = false; }
      else if (cp == '?' && !fragment) query = true;
      if (cp < 0x80) {
        transport.append((char) cp); // java.net.URI checks ASCII syntax, escapes and delimiters.
      } else {
        if (!isUcsChar(cp) && !(query && isPrivate(cp)))
          throw new URISyntaxException(value, "Character is not allowed in an IRI", offset);
        for (byte octet : new String(Character.toChars(cp)).getBytes(StandardCharsets.UTF_8)) {
          int unsigned = octet & 0xff;
          transport.append('%').append("0123456789ABCDEF".charAt(unsigned >>> 4))
              .append("0123456789ABCDEF".charAt(unsigned & 15));
        }
      }
      offset += Character.charCount(cp);
    }
    URI encoded = new URI(transport.toString());
    // Retain the previous URI API spelling whenever java.net.URI can represent it, e.g. café.
    try { return new URI(value); }
    catch (URISyntaxException unsupportedUnicode) { return encoded; }
  }

  private static boolean isUcsChar(int cp) {
    return cp >= 0xa0 && cp <= 0xd7ff || cp >= 0xf900 && cp <= 0xfdcf
        || cp >= 0xfdf0 && cp <= 0xffef
        || cp >= 0x10000 && cp <= 0xdfffd && (cp & 0xffff) <= 0xfffd
        || cp >= 0xe1000 && cp <= 0xefffd;
  }

  private static boolean isPrivate(int cp) {
    return cp >= 0xe000 && cp <= 0xf8ff || cp >= 0xf0000 && cp <= 0xffffd
        || cp >= 0x100000 && cp <= 0x10fffd;
  }
}
