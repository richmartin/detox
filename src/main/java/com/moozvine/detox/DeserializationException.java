package com.moozvine.detox;

public class DeserializationException extends Exception {
  public DeserializationException(String message) {
    super(message);
  }

  public DeserializationException(Exception e) {
    super(e);
  }

  public DeserializationException(
      final String message,
      final Throwable cause) {
    super(message, cause);
  }
}
