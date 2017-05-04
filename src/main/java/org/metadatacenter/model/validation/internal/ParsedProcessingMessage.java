package org.metadatacenter.model.validation.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class ParsedProcessingMessage {

  private final JsonNode parsedMessage;

  public ParsedProcessingMessage(@Nonnull ProcessingMessage processingMessage) {
    checkNotNull(processingMessage);
    this.parsedMessage = processingMessage.asJson();
  }

  public Collection<ReportItem> getReportItems() {
    JsonNode rootNode = parsedMessage;
    JsonNode reportNode = rootNode.path("reports");
    Set<ReportItem> reportItems = Sets.newHashSet();
    if (!reportNode.isMissingNode()) {
      iterateReport(reportItems, reportNode);
    } else {
      collectReportItem(reportItems, rootNode);
    }
    return reportItems;
  }

  private void iterateReport(Set<ReportItem> reportItems, JsonNode reportNode) {
    for (JsonNode reportItemNode : reportNode) {
      JsonNode rootNode = reportItemNode.path(0);
      JsonNode subReportNode = rootNode.path("reports");
      if (!subReportNode.isMissingNode()) {
        iterateSubReport(reportItems, subReportNode);
      } else {
        collectReportItem(reportItems, rootNode);
      }
    }
  }

  private void iterateSubReport(Set<ReportItem> reportItems, JsonNode subReportNode) {
    for (JsonNode subReportItemNode : subReportNode) {
      JsonNode subReportDetails = subReportItemNode.path(0);
      if (!subReportDetails.isMissingNode()) {
        collectReportItem(reportItems, subReportDetails);
      }
    }
  }

  private void collectReportItem(Set<ReportItem> reportItems, JsonNode reportDetails) {
    ReportItem reportItem = new ReportItem(
        getMessage(reportDetails),
        getLocation(reportDetails),
        getSchemaResource(reportDetails),
        getSchemaPointer(reportDetails));
    reportItems.add(reportItem);
  }

  @Nullable
  public static String getMessage(JsonNode node) {
    JsonNode messageNode = node.path("message");
    return (!messageNode.isMissingNode()) ? prettyText(messageNode.asText()) : null;
  }

  private static String prettyText(String s) {
    return s.replace("\"", "'");
  }

  @Nullable
  public static String getLocation(JsonNode node) {
    JsonNode locationNode = node.path("instance").path("pointer");
    if (!locationNode.isMissingNode()) {
      String location = locationNode.asText();
      if (location.isEmpty()) {
        location = "/";
      }
      return location;
    }
    return null;
  }

  @Nullable
  public static String getSchemaResource(JsonNode node) {
    JsonNode schemaResourceNode = node.path("schema").path("loadingURI");
    return (!schemaResourceNode.isMissingNode()) ? schemaResourceNode.asText() : null;

  }

  @Nullable
  public static String getSchemaPointer(JsonNode node) {
    JsonNode schemaPointerNode = node.path("schema").path("pointer");
    return (!schemaPointerNode.isMissingNode()) ? schemaPointerNode.asText() : null;
  }

  public class ReportItem {

    private final String message;
    private final String location;
    private final String schemaResource;
    private final String schemaPointer;

    public ReportItem(@Nullable String message, @Nullable String location, @Nullable String schemaResource, @Nullable
        String schemaPointer) {
      this.message = message;
      this.location = location;
      this.schemaResource = schemaResource;
      this.schemaPointer = schemaPointer;
    }

    @Nullable
    public String getMessage() {
      return message;
    }

    @Nullable
    public String getLocation() {
      return location;
    }

    @Nullable
    public String getSchemaResource() {
      return schemaResource;
    }

    @Nullable
    public String getSchemaPointer() {
      return schemaPointer;
    }
  }
}
