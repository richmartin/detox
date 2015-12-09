package com.moozvine.detox.testtypes;

import java.util.List;

/**
 * Created by rich on 15/07/14.
 */
public class SerializableWithListImpl implements SerializableWithList {
  private final List<String> someStrings;

  public SerializableWithListImpl(final List<String> someStrings) {
    this.someStrings = someStrings;
  }

  @Override
  public List<String> getSomeStrings() {
    return someStrings;
  }
}
