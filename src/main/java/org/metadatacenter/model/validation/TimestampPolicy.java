package org.metadatacenter.model.validation;

import java.time.OffsetDateTime;
import org.metadatacenter.model.policy.ProvenanceTimestamp;

/** Public validation API for the shared provenance timestamp contract. */
public final class TimestampPolicy {
  private TimestampPolicy() {}
  public static OffsetDateTime parse(String value) { return ProvenanceTimestamp.parse(value); }
  public static String format(OffsetDateTime value) { return ProvenanceTimestamp.format(value); }
}
