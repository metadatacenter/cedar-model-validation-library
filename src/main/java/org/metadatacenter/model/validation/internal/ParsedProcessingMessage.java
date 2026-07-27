package org.metadatacenter.model.validation.internal;

import com.networknt.schema.ValidationMessage;

import java.util.Collection;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adapts a single networknt {@link ValidationMessage} into the {@link ReportItem} shape the
 * validator reports. It also reproduces, for the two keywords whose wording the public API has
 * historically exposed, the exact English strings the former FGE engine emitted:
 *
 * <ul>
 *   <li>{@code required} &rarr; {@code object has missing required properties (['<field>'])}</li>
 *   <li>{@code additionalProperties} &rarr;
 *       {@code object instance has properties which are not allowed by the schema: ['<field>']}</li>
 * </ul>
 *
 * networknt reports one message per missing or disallowed property, so each adapted item names a
 * single property. For every other keyword the underlying networknt message is passed through, with
 * its doubled single-quote escaping normalised to single quotes.
 */
public class ParsedProcessingMessage {

  private static final String KEYWORD_REQUIRED = "required";
  private static final String KEYWORD_ADDITIONAL_PROPERTIES = "additionalProperties";

  private final ValidationMessage message;
  private final String schemaResourceName;

  public ParsedProcessingMessage(ValidationMessage message, String schemaResourceName) {
    checkNotNull(message);
    this.message = message;
    this.schemaResourceName = schemaResourceName;
  }

  public Collection<ReportItem> getReportItems() {
    ReportItem reportItem = new ReportItem(
        getMessage(),
        getLocation(),
        schemaResourceName,
        getSchemaPointer());
    return Collections.singletonList(reportItem);
  }

  private String getMessage() {
    String keyword = message.getType();
    if (KEYWORD_REQUIRED.equals(keyword)) {
      return String.format("object has missing required properties (['%s'])", getSubjectProperty());
    }
    if (KEYWORD_ADDITIONAL_PROPERTIES.equals(keyword)) {
      return String.format("object instance has properties which are not allowed by the schema: ['%s']",
          getSubjectProperty());
    }
    return prettyText(message.getMessage());
  }

  /**
   * The property that a {@code required} or {@code additionalProperties} violation is about. Prefer
   * the explicit property carried on the message; fall back to the first message argument.
   */
  private String getSubjectProperty() {
    String property = message.getProperty();
    if (property != null && !property.isEmpty()) {
      return property;
    }
    Object[] arguments = message.getArguments();
    if (arguments != null && arguments.length > 0 && arguments[0] != null) {
      return String.valueOf(arguments[0]);
    }
    return property;
  }

  private static String prettyText(String s) {
    return (s == null) ? null : s.replace("\"", "'");
  }

  private String getLocation() {
    String location = message.getInstanceLocation().toString();
    return location.isEmpty() ? "/" : location;
  }

  private String getSchemaPointer() {
    return (message.getSchemaLocation() != null) ? message.getSchemaLocation().toString() : null;
  }

  public static class ReportItem {

    private final String message;
    private final String location;
    private final String schemaResource;
    private final String schemaPointer;

    public ReportItem(String message, String location, String schemaResource, String schemaPointer) {
      this.message = message;
      this.location = location;
      this.schemaResource = schemaResource;
      this.schemaPointer = schemaPointer;
    }

    public String getMessage() {
      return message;
    }

    public String getLocation() {
      return location;
    }

    public String getSchemaResource() {
      return schemaResource;
    }

    public String getSchemaPointer() {
      return schemaPointer;
    }
  }
}
