package com.moozvine.detox.validators;

import com.moozvine.detox.FieldValidator;

public class PositiveLong extends FieldValidator<Long> {
  @Override
  public void validate(final String fieldName, final Long value) {
    if (value <= 0) {
      throw new IllegalArgumentException("Given value (" + value + ") for " + fieldName + " is not positive.");
    }
  }
}
