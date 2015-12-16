package com.moozvine.detox;

/**
 * Something that can produce a value for a given field name.
 */
public interface ValueGenerator<T> {
  T apply(String fieldName);
}
