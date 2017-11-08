package org.metadatacenter.model.validation.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class ParsedProcessingMessage {

  private final JsonNode parsedMessage;

  public ParsedProcessingMessage(ProcessingMessage processingMessage) {
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

  public static String getMessage(JsonNode node) {
    JsonNode messageNode = node.path("message");
    return (!messageNode.isMissingNode()) ? prettyText(messageNode.asText()) : null;
  }

  private static String prettyText(String s) {
    return s.replace("\"", "'");
  }

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

  public static String getSchemaResource(JsonNode node) {
    JsonNode schemaResourceNode = node.path("schema").path("loadingURI");
    return (!schemaResourceNode.isMissingNode()) ? schemaResourceNode.asText() : null;

  }

  public static String getSchemaPointer(JsonNode node) {
    JsonNode schemaPointerNode = node.path("schema").path("pointer");
    return (!schemaPointerNode.isMissingNode()) ? schemaPointerNode.asText() : null;
  }

  public class ReportItem {

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
