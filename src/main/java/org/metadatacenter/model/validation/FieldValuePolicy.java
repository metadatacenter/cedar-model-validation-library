package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.metadatacenter.model.validation.ValidationFinding.Category.*;

/** Pure field-value rules shared by model validation and artifact builders. Never changes a value. */
public final class FieldValuePolicy {
  private FieldValuePolicy() {}
  private static final Map<String, String[]> INTEGRAL = Map.of(
      "xsd:byte", new String[]{"-128", "127"}, "xsd:short", new String[]{"-32768", "32767"},
      "xsd:int", new String[]{"-2147483648", "2147483647"},
      "xsd:long", new String[]{"-9223372036854775808", "9223372036854775807"});
  private static final Set<String> FRACTIONAL = Set.of("xsd:decimal", "xsd:float", "xsd:double");
  private static final Pattern EMAIL = Pattern.compile(
      "[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?"
          + "(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*");
  private static final Pattern INTEGER = Pattern.compile("[+-]?(?:0|[1-9][0-9]*)");
  private static final Pattern DECIMAL = Pattern.compile("[+-]?(?:(?:0|[1-9][0-9]*)(?:\\.[0-9]*)?|\\.[0-9]+)");
  private static final Pattern DATE = Pattern.compile("([0-9]{4})-([0-9]{2})-([0-9]{2})");
  private static final Pattern TIME = Pattern.compile("([0-9]{2}):([0-9]{2})(?::([0-9]{2}))?(\\.[0-9]+)?");
  private static final Pattern OFFSET = Pattern.compile("(Z|[+-][0-9]{2}:[0-9]{2})$");

  public static List<ValidationFinding> validate(String value, JsonNode field, String path, boolean defaultValue) {
    List<ValidationFinding> out = new ArrayList<>();
    if (value == null || value.isEmpty()) return out;
    JsonNode c = field.path("_valueConstraints");
    String input = field.path("_ui").path("inputType").asText();
    if (input.equals("numeric")) numeric(value, c, path, defaultValue, out);
    else if (input.equals("temporal")) {
      if (defaultValue) {
        if (OFFSET.matcher(value).find() && !field.path("_ui").path("timezoneEnabled").asBoolean())
          add(out, "timezone", CONSTRAINT, path, "Carries a timezone offset but none is enabled.");
        if (!TemporalDefaultPolicy.isValid(value, c.path("temporalType").asText(), field.path("_ui").path("temporalGranularity").asText()))
          add(out, "temporalType", STRUCTURAL, path, "Not a valid " + c.path("temporalType").asText() + " default.");
      } else temporal(value, field, path, out);
    }
    else {
      int length = TextPolicy.codePointLength(value);
      if (c.path("minLength").isIntegralNumber() && length < c.get("minLength").intValue())
        add(out, "minLength", CONSTRAINT, path, "Shorter than the minimum " + c.get("minLength") + ".");
      if (c.path("maxLength").isIntegralNumber() && length > c.get("maxLength").intValue())
        add(out, "maxLength", CONSTRAINT, path, "Longer than the maximum " + c.get("maxLength") + ".");
      if (c.path("literals").isArray() && !c.get("literals").isEmpty()) {
        boolean found = false;
        for (JsonNode literal : c.get("literals")) if (value.equals(literal.path("label").asText())) found = true;
        if (!found) add(out, "choiceMembership", CONSTRAINT, path, "Not one of the permitted choices.");
      }
      if (input.equals("email") && !EMAIL.matcher(value).matches())
        add(out, "email", CONSTRAINT, path, "Not a valid e-mail address.");
      if (input.equals("phone-number") && !value.matches("[-+0-9 	()]+"))
        add(out, "phoneNumber", CONSTRAINT, path, "Not a valid phone number.");
    }
    FieldRegexValidation.validateValue(value, field, path, out);
    if (isIriField(field)) {
      try {
        var uri = IriReference.toUri(value);
        if (!uri.isAbsolute()) throw new IllegalArgumentException();
        if ((uri.getScheme().equalsIgnoreCase("http") || uri.getScheme().equalsIgnoreCase("https"))
            && (uri.getRawAuthority() == null || uri.getRawAuthority().isEmpty()))
          throw new IllegalArgumentException();
      } catch (java.net.URISyntaxException | IllegalArgumentException e) {
        add(out, "iriMalformed", STRUCTURAL, path, "Not a valid absolute IRI.");
      }
    }
    return out;
  }

  public static List<ValidationFinding> validateConfiguration(JsonNode field, String path) {
    List<ValidationFinding> out = new ArrayList<>();
    JsonNode c = field.path("_valueConstraints");
    for (String key : List.of("minLength", "maxLength", "decimalPlace")) {
      if (c.has(key) && (!c.get(key).isIntegralNumber() || !c.get(key).canConvertToInt() || c.get(key).intValue() < 0))
        add(out, "templateConstraint", STRUCTURAL, path + "/_valueConstraints/" + key,
            key + " must be a nonnegative integer.");
    }
    for (String key : List.of("minValue", "maxValue")) {
      if (c.has(key) && !c.get(key).isNumber())
        add(out, "templateConstraint", STRUCTURAL, path + "/_valueConstraints/" + key,
            "Numeric bounds must be finite decimal values.");
    }
    for (String[] pair : List.of(new String[]{"minValue", "maxValue"}, new String[]{"minLength", "maxLength"})) {
      if (c.path(pair[0]).isNumber() && c.path(pair[1]).isNumber()
          && c.get(pair[0]).decimalValue().compareTo(c.get(pair[1]).decimalValue()) > 0)
        add(out, "templateConstraint", STRUCTURAL, path + "/_valueConstraints/" + pair[1],
            "The minimum exceeds the maximum.");
    }
    if (field.path("_ui").path("inputType").asText().equals("numeric")) {
      String type = c.path("numberType").asText();
      if (!INTEGRAL.containsKey(type) && !FRACTIONAL.contains(type))
        add(out, "templateConstraint", STRUCTURAL, path + "/_valueConstraints/numberType",
            "The numeric datatype is missing or unsupported.");
      else {
        JsonNode precision = c.path("decimalPlace");
        boolean validPrecision = precision.isIntegralNumber() && precision.canConvertToInt() && precision.intValue() >= 0;
        if (INTEGRAL.containsKey(type) && validPrecision && precision.intValue() != 0)
          add(out, "templateConstraint", STRUCTURAL, path + "/_valueConstraints/decimalPlace",
              "An integer datatype cannot have decimal places.");
        for (String key : List.of("minValue", "maxValue")) {
          if (!c.path(key).isNumber()) continue;
          BigDecimal value = c.get(key).decimalValue();
          if (!fitsDatatype(value, type))
            add(out, "templateConstraint", STRUCTURAL, path + "/_valueConstraints/" + key,
                "The bound is outside the range or precision of " + type + ".");
          else if (validPrecision && Math.max(0, value.stripTrailingZeros().scale()) > precision.intValue())
            add(out, "templateConstraint", STRUCTURAL, path + "/_valueConstraints/" + key,
                "The bound exceeds the declared number of decimal places.");
        }
      }
    }
    FieldRegexValidation.validateConfiguration(field, path, out);
    return out;
  }

  public static boolean isIriField(JsonNode field) {
    String input = field.path("_ui").path("inputType").asText();
    return field.path("properties").has("@id") || input.equals("controlled-term") || input.equals("controlled")
        || input.equals("link") || input.startsWith("ext-");
  }

  public static boolean isControlled(JsonNode field) {
    String input = field.path("_ui").path("inputType").asText();
    if (input.equals("controlled-term") || input.equals("controlled")) return true;
    JsonNode c = field.path("_valueConstraints");
    return isIriField(field) && (c.has("ontologies") || c.has("branches") || c.has("classes") || c.has("valueSets"));
  }

  private static void numeric(String value, JsonNode c, String path, boolean defaultValue,
                              List<ValidationFinding> out) {
    String type = c.path("numberType").asText("xsd:decimal");
    String[] bounds = INTEGRAL.get(type);
    try {
      BigDecimal number = NumericPolicy.parse(value);
      if (!defaultValue && !(bounds != null ? INTEGER : DECIMAL).matcher(value).matches())
        throw new NumberFormatException();
      if ((bounds != null || defaultValue) && !fitsDatatype(number, type))
        throw new NumberFormatException();
      if (bounds == null && !FRACTIONAL.contains(type)) throw new NumberFormatException();
      if (c.path("decimalPlace").isIntegralNumber()) {
        int places = defaultValue ? Math.max(0, number.stripTrailingZeros().scale())
            : value.contains(".") ? value.length() - value.indexOf('.') - 1 : 0;
        if (places > c.get("decimalPlace").intValue())
          add(out, "decimalPlace", CONSTRAINT, path, "More than " + c.get("decimalPlace") + " decimal places.");
      }
      if (c.path("minValue").isNumber() && number.compareTo(c.get("minValue").decimalValue()) < 0)
        add(out, "minValue", CONSTRAINT, path, "Below the minimum " + c.get("minValue") + ".");
      if (c.path("maxValue").isNumber() && number.compareTo(c.get("maxValue").decimalValue()) > 0)
        add(out, "maxValue", CONSTRAINT, path, "Above the maximum " + c.get("maxValue") + ".");
    } catch (NumberFormatException e) {
      add(out, "numberType", STRUCTURAL, path, "Not a valid " + type + ".");
    }
  }

  private static boolean fitsDatatype(BigDecimal number, String type) {
    String[] bounds = INTEGRAL.get(type);
    if (bounds != null) return number.stripTrailingZeros().scale() <= 0
        && number.compareTo(new BigDecimal(bounds[0])) >= 0 && number.compareTo(new BigDecimal(bounds[1])) <= 0;
    if (type.equals("xsd:float") || type.equals("xsd:double")) {
      BigDecimal magnitude = number.abs();
      BigDecimal largest = new BigDecimal(type.equals("xsd:float") ? "3.4028234663852886e38" : "1.7976931348623157e308");
      BigDecimal smallest = new BigDecimal(type.equals("xsd:float") ? "1.401298464324817e-45" : "4.9e-324");
      return magnitude.compareTo(largest) <= 0 && (magnitude.signum() == 0 || magnitude.compareTo(smallest) >= 0);
    }
    return type.equals("xsd:decimal");
  }

  private static void temporal(String value, JsonNode field, String path, List<ValidationFinding> out) {
    String type = field.path("_valueConstraints").path("temporalType").asText();
    String granularity = field.path("_ui").path("temporalGranularity").asText();
    String rest = value;
    var offset = OFFSET.matcher(rest);
    if (offset.find()) {
      String zone = offset.group();
      rest = rest.substring(0, offset.start());
      if (!zone.equals("Z")) {
        int h = Integer.parseInt(zone.substring(1, 3)), m = Integer.parseInt(zone.substring(4, 6));
        if (h > 14 || m > 59 || h == 14 && m != 0)
          add(out, "timezoneOffset", STRUCTURAL, path, "Not a valid xsd timezone offset.");
      }
      if (!field.path("_ui").path("timezoneEnabled").asBoolean())
        add(out, "timezone", CONSTRAINT, path, "Carries a timezone offset but none is enabled.");
    }
    String[] parts = rest.split("T", -1);
    String date = type.equals("xsd:time") ? null : parts[0];
    String time = type.equals("xsd:time") ? rest : parts.length == 2 ? parts[1] : null;
    if (parts.length > 2 || !(type.equals("xsd:date") && time == null && DATE.matcher(rest).matches()
        || type.equals("xsd:time") && TIME.matcher(rest).matches()
        || type.equals("xsd:dateTime") && time != null && DATE.matcher(date).matches() && TIME.matcher(time).matches())) {
      add(out, "temporalType", STRUCTURAL, path, "Not a valid " + type + ".");
      return;
    }
    if (date != null) {
      var d = DATE.matcher(date); d.matches();
      try { LocalDate.of(Integer.parseInt(d.group(1)), Integer.parseInt(d.group(2)), Integer.parseInt(d.group(3))); }
      catch (DateTimeException e) { add(out, "temporalCalendar", STRUCTURAL, path, "Not a real calendar date."); }
      if (granularity.equals("year") && (!d.group(2).equals("01") || !d.group(3).equals("01"))
          || granularity.equals("month") && !d.group(3).equals("01"))
        add(out, "temporalGranularity", CONSTRAINT, path, "The value exceeds the declared temporal granularity.");
    }
    if (time != null) {
      var t = TIME.matcher(time); t.matches();
      int hour = Integer.parseInt(t.group(1)), minute = Integer.parseInt(t.group(2));
      int second = t.group(3) == null ? 0 : Integer.parseInt(t.group(3));
      if (hour > 23 || minute > 59 || second > 59)
        add(out, "temporalCalendar", STRUCTURAL, path, "Not a real time of day.");
      boolean fraction = t.group(4) != null;
      if (Set.of("year", "month", "day").contains(granularity) && (hour != 0 || minute != 0 || second != 0 || fraction)
          || granularity.equals("hour") && (minute != 0 || second != 0 || fraction)
          || granularity.equals("minute") && (second != 0 || fraction)
          || granularity.equals("second") && (t.group(3) == null || fraction)
          || granularity.equals("decimalSecond") && (t.group(3) == null || !fraction))
        add(out, "temporalGranularity", CONSTRAINT, path, "The value does not match the declared temporal granularity.");
    }
  }

  private static void add(List<ValidationFinding> out, String code, ValidationFinding.Category category,
                          String path, String message) {
    out.add(new ValidationFinding(code, category, path, message));
  }
}
