package com.moozvine.detox;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Declares the string that should be used as the field name for this field in JSON representations.
 */
@Target(ElementType.METHOD)
public @interface JsonFieldName {
  String value();
}
