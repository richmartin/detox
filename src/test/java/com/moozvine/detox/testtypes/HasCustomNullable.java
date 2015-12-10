package com.moozvine.detox.testtypes;

import com.moozvine.detox.GenerateBuilder;

@GenerateBuilder
public interface HasCustomNullable {
  @Nullable String getSomeNullableString();

  @interface Nullable {}
}
