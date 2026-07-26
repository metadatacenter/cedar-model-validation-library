package org.metadatacenter.model.validation.internal;

import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.format.AbstractFormat;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * Draft-04 {@code uri} and {@code date-time} format checkers that reproduce the acceptance set of
 * the former FGE engine, so that swapping in networknt does not change which artifacts the API
 * accepts or rejects.
 *
 * <p>FGE's formats are more permissive than networknt's strict RFC 3986 / RFC 3339 built-ins:
 * <ul>
 *   <li>{@code uri} accepted any value that {@link java.net.URI} could parse, including relative
 *       references such as {@code tmp-1542047882174-1714355} that CEDAR uses for unsaved
 *       artifacts. networknt's built-in requires an absolute URI and would reject them.</li>
 *   <li>{@code date-time} was parsed with the Joda patterns {@code yyyy-MM-dd'T'HH:mm:ssZ} and
 *       {@code yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,12}Z}, whose {@code Z} token accepts a numeric offset
 *       written with or without a colon (for example {@code -0800} as well as {@code -08:00}) and
 *       the literal {@code Z}. networknt's built-in requires the RFC 3339 colon form and would
 *       reject the offsets CEDAR timestamps carry.</li>
 * </ul>
 */
public final class FgeCompatFormats {

  private FgeCompatFormats() {
  }

  /**
   * A networknt {@link JsonSchemaFactory} for JSON Schema Draft-04 whose {@code uri} and
   * {@code date-time} format checkers reproduce FGE's acceptance set.
   */
  public static final JsonSchemaFactory FACTORY = buildFactory();

  private static JsonSchemaFactory buildFactory() {
    JsonMetaSchema v4 = JsonMetaSchema.builder(JsonMetaSchema.getV4())
        .addFormat(new UriFormat())
        .addFormat(new DateTimeFormat())
        .build();
    return JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4, builder -> builder.metaSchema(v4));
  }

  private static final class UriFormat extends AbstractFormat {

    private UriFormat() {
      super("uri", "must be a valid URI");
    }

    @Override
    public boolean matches(String value) {
      if (value == null) {
        return true;
      }
      try {
        new URI(value);
        return true;
      } catch (URISyntaxException e) {
        return false;
      }
    }
  }

  private static final class DateTimeFormat extends AbstractFormat {

    // The base civil time, matching Joda's yyyy-MM-dd'T'HH:mm:ss with an optional fractional part.
    // The trailing offset is required, as in FGE's Joda patterns; the two accepted spellings — with
    // or without a colon, or the literal Z — are covered by trying each formatter in turn.
    private static final DateTimeFormatter[] FORMATS = {
        buildFormatter("+HH:MM:ss"),
        buildFormatter("+HHMM")
    };

    private DateTimeFormat() {
      super("date-time", "must be a valid RFC 3339 date-time");
    }

    private static DateTimeFormatter buildFormatter(String offsetPattern) {
      return new DateTimeFormatterBuilder()
          .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
          .optionalStart()
          .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
          .optionalEnd()
          .appendOffset(offsetPattern, "Z")
          .toFormatter();
    }

    @Override
    public boolean matches(String value) {
      if (value == null) {
        return true;
      }
      for (DateTimeFormatter formatter : FORMATS) {
        try {
          formatter.parse(value);
          return true;
        } catch (RuntimeException e) {
          // Try the next accepted spelling.
        }
      }
      return false;
    }
  }
}
