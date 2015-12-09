package com.moozvine.detox;

public abstract class FieldValidator<T> {
  public abstract void validate(String fieldName, T t);
}
