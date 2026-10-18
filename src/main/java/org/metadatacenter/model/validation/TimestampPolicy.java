package org.metadatacenter.model.validation;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.regex.Pattern;

/** Provenance timestamps, separate from the partial dates allowed in temporal fields. */
public final class TimestampPolicy {
  private TimestampPolicy() {}

  private static final Pattern SHAPE = Pattern.compile(
      "(?:[0-9]{4}|-[0-9]{4,9}|\\+[0-9]{5,9})-[0-9]{2}-[0-9]{2}[Tt]"
      + "[0-9]{2}:[0-9]{2}(?::[0-9]{2}(?:\\.[0-9]{1,9})?)?"
      + "(?:[Zz]|[+-][0-9]{2}(?::[0-9]{2}(?::[0-9]{2})?)?)");
  private static final DateTimeFormatter OUTPUT = new DateTimeFormatterBuilder()
      .appendPattern("uuuu-MM-dd'T'HH:mm:ss")
      .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
      .appendOffsetId().toFormatter(Locale.ROOT);

  /** Accept the historical colonless offset spelling, but never infer a missing offset. */
  public static OffsetDateTime parse(String value) {
    String normalized = value.replaceFirst("([+-][0-9]{2})([0-9]{2})$", "$1:$2");
    if (!SHAPE.matcher(normalized).matches())
      throw new DateTimeParseException("Expected an ISO timestamp with an explicit offset", value, 0);
    return OffsetDateTime.parse(normalized);
  }

  /** Keep the local date, offset and every significant fractional second. */
  public static String format(OffsetDateTime value) {
    return OUTPUT.format(value);
  }
}
