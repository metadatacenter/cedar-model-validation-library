package org.metadatacenter.model.validation.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonPropertyOrder({"message", "location", "additionalInfo"})
public class WarningItem {

  private final String message;
  private final String location;
  private final Map<String, Object> additionalInfo = Maps.newHashMap();

  public WarningItem(String message) {
    this(message, null);
  }

  public WarningItem(String message, String location) {
    this.message = message;
    this.location = location;
  }

  @JsonProperty("message")
  public String getMessage() {
    return message;
  }

  @JsonProperty("location")
  public String getLocation() {
    return location;
  }

  public void addAdditionalInfo(String key, Object value) {
    additionalInfo.put(checkNotNull(key), value);
  }

  @JsonProperty("additionalInfo")
  public Map<String, Object> getAdditionalInfo() {
    return additionalInfo;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (!(o instanceof WarningItem)) {
      return false;
    }
    WarningItem other = (WarningItem) o;
    return Objects.equal(message, other.message)
        && Objects.equal(location, other.location)
        && Objects.equal(additionalInfo, other.additionalInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(message, location, additionalInfo);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .addValue(message)
        .addValue(location)
        .addValue(additionalInfo)
        .toString();
  }
}
