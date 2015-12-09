package com.moozvine.detox.processor;

/**
* Created by rich on 17/07/14.
*/
class InvalidTypeException extends Exception {
  InvalidTypeException() {
  }

  InvalidTypeException(final String message) {
    super(message);
  }
}
