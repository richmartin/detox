package com.moozvine.detox.validators;

import com.moozvine.detox.FieldValidator;

public class NonPositiveInteger extends FieldValidator<Integer> {
  @Override
  public void validate(final String fieldName, final Integer value) {
    if (value > 0) {
      throw new IllegalArgumentException("Given value (" + value + ") for " + fieldName + " is positive.");
    }
  }
}
