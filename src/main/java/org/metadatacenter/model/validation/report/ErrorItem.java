package org.metadatacenter.model.validation.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonPropertyOrder({"message", "location", "additionalInfo"})
public class ErrorItem {

  private final String message;
  private final String location;
  private final Map<String, Object> additionalInfo = Maps.newHashMap();

  public ErrorItem(@Nullable String message) {
    this(message, null);
  }

  public ErrorItem(@Nullable String message, @Nullable String location) {
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

  public void addAdditionalInfo(@Nonnull String key, @Nullable Object value) {
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
    if (!(o instanceof ErrorItem)) {
      return false;
    }
    ErrorItem other = (ErrorItem) o;
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
