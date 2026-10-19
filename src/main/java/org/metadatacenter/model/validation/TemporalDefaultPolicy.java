package org.metadatacenter.model.validation;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Set;
import java.util.regex.Pattern;

/** Defaults may express partial components, while instance literals use their complete padded form. */
public final class TemporalDefaultPolicy {
  private TemporalDefaultPolicy() {}
  private static final String ZONE = "(?:Z|[+-](?:(?:0[0-9]|1[0-3]):[0-5][0-9]|14:00))?";
  private static final String HOUR = "(?:[01][0-9]|2[0-3])";
  private static final String MINUTE = "[0-5][0-9]";
  private static final String FULL_DATE = "[0-9]{4}-(?:0[1-9]|1[0-2])-(?:0[1-9]|[12][0-9]|3[01])";

  public static boolean isValid(String value, String type, String granularity) {
    if (value == null || value.isEmpty()) return true;
    if (granularity == null) granularity = "";
    boolean compatible = granularity.isEmpty() || switch (type) {
      case "xsd:date" -> Set.of("year", "month", "day").contains(granularity);
      case "xsd:time" -> Set.of("hour", "minute", "second", "decimalSecond").contains(granularity);
      case "xsd:dateTime" -> Set.of("day", "hour", "minute", "second", "decimalSecond").contains(granularity);
      default -> false;
    };
    if (!compatible) return false;
    String pattern = switch (type) {
      case "xsd:date" -> switch (granularity) {
        case "year" -> "[0-9]{4}";
        case "month" -> "[0-9]{4}-(?:0[1-9]|1[0-2])";
        case "day" -> FULL_DATE;
        default -> "[0-9]{4}(?:-(?:0[1-9]|1[0-2])(?:-(?:0[1-9]|[12][0-9]|3[01]))?)?";
      };
      case "xsd:time" -> time(granularity);
      case "xsd:dateTime" -> granularity.equals("day") ? FULL_DATE
          : granularity.isEmpty() ? FULL_DATE + "(?:T" + time("") + ")?" : FULL_DATE + "T" + time(granularity);
      default -> "a^";
    };
    if (!Pattern.matches(pattern, value)) return false;
    var date = Pattern.compile("^([0-9]{4})-([0-9]{2})-([0-9]{2})").matcher(value);
    if (date.find()) {
      try { LocalDate.of(Integer.parseInt(date.group(1)), Integer.parseInt(date.group(2)), Integer.parseInt(date.group(3))); }
      catch (DateTimeException invalid) { return false; }
    }
    return true;
  }
  private static String time(String granularity) {
    return switch (granularity) {
      case "hour" -> HOUR + ZONE;
      case "minute" -> HOUR + ":" + MINUTE + ZONE;
      case "second" -> HOUR + ":" + MINUTE + ":" + MINUTE + ZONE;
      case "decimalSecond" -> HOUR + ":" + MINUTE + ":" + MINUTE + "\\.[0-9]+" + ZONE;
      default -> HOUR + "(?::" + MINUTE + "(?::" + MINUTE + "(?:\\.[0-9]+)?)?)?" + ZONE;
    };
  }
}
