package com.moozvine.detox.validators;

import com.moozvine.detox.FieldValidator;

public class NonEmptyString extends FieldValidator<String> {
  @Override
  public void validate(final String fieldName, final String value) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(fieldName + " must not be empty.");
    }
  }
}
