package com.moozvine.detox;

public class SerializationError extends RuntimeException {
  public SerializationError(final String message) {
    super(message);
  }

  public SerializationError(final Exception e) {
    super(e);
  }

  public SerializationError(
      final String message,
      final Throwable cause) {
    super(message, cause);
  }
}
