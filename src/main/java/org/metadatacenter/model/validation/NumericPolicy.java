package org.metadatacenter.model.validation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Pattern;

/** Exact decimal values. No locale, Java numeric suffixes, hex floats or implicit trimming. */
public final class NumericPolicy {
  private NumericPolicy() {}
  private static final Pattern DECIMAL = Pattern.compile(
      "[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?");

  public static BigDecimal parse(String text) {
    if (!DECIMAL.matcher(text).matches()) throw new NumberFormatException("Expected a decimal number");
    return new BigDecimal(text);
  }

  /** Retain the established integral Number shape without rounding larger integers or decimals. */
  public static Number number(String text) {
    BigDecimal exact = parse(text);
    if (text.indexOf('.') < 0 && text.indexOf('e') < 0 && text.indexOf('E') < 0) {
      BigInteger integer = exact.toBigIntegerExact();
      try { return integer.longValueExact(); }
      catch (ArithmeticException large) { return integer; }
    }
    return exact;
  }

  public static int compare(String left, String right) {
    return parse(left).compareTo(parse(right));
  }
}
