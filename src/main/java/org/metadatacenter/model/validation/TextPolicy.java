package org.metadatacenter.model.validation;

/** Stable CEDAR text rules shared with the TypeScript policy entry point. Never normalizes input. */
public final class TextPolicy {
  private TextPolicy() {}

  /** The Java 17 blankness contract, explicitly pinned so a Unicode/runtime upgrade cannot change it. */
  public static boolean isBlank(String value) {
    return value.codePoints().allMatch(TextPolicy::isBlankCodePoint);
  }

  public static boolean isBlankCodePoint(int cp) {
    return cp >= 0x09 && cp <= 0x0d || cp >= 0x1c && cp <= 0x20 || cp == 0x1680
        || cp >= 0x2000 && cp <= 0x2006 || cp >= 0x2008 && cp <= 0x200a
        || cp == 0x2028 || cp == 0x2029 || cp == 0x205f || cp == 0x3000;
  }

  /** JSON Schema string length counts code points, not UTF-16 units or displayed graphemes. */
  public static int codePointLength(String value) {
    return value.codePointCount(0, value.length());
  }
}
