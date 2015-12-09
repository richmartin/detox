package com.moozvine.detox.testtypes;

import com.moozvine.detox.*;

@GenerateDTO(validator = ValidatedSimpleType.Validator.class)
public interface ValidatedSimpleType extends Serializable {

  String getUpperCaseString();

  @Validate(OverTwenty.class) int getSomethingOverTwenty();

  public static class Validator extends InstanceValidator<ValidatedSimpleType> {
    @Override
    public void validate(final ValidatedSimpleType validatedSimpleType) {
      final String value = validatedSimpleType.getUpperCaseString();
      if (!value.toUpperCase().equals(value)) {
        throw new IllegalStateException(value + " is not an upper case string");
      }
    }
  }

  public static class OverTwenty extends FieldValidator<Integer> {
    @Override
    public void validate(
        final String fieldName,
        final Integer integer) {
      if (integer <= 20) {
        throw new IllegalArgumentException(fieldName + " is not over twenty.");
      }
    }
  }
}
