package com.moozvine.detox.processor;

import javax.lang.model.type.TypeMirror;

public enum NativeType {
  BOOLEAN(Boolean.class.getName(), "boolean"),
  BYTE(Byte.class.getName(), "byte"),
  CHARACTER(Character.class.getName(), "char"),
  SHORT(Short.class.getName(), "short"),
  STRING(String.class.getName(), "String"),
  INTEGER(Integer.class.getName(), "int"),
  LONG(Long.class.getName(), "long"),
  DOUBLE(Double.class.getName(), "double"),
  FLOAT(Float.class.getName(), "float");

  private final String typeName;
  private final String primitiveName;

  NativeType(
      final String typeName,
      final String primitiveName) {
    this.typeName = typeName;
    this.primitiveName = primitiveName;
  }

  public String getTypeName() {
    return typeName;
  }

  private String getPrimitiveName() {
    return primitiveName;
  }

  public static boolean isNativeType(final TypeMirror value) {
    for (final NativeType nativeType : values()) {
      if (nativeType.getTypeName().equals(value.toString())) {
        return true;
      }
      if (nativeType.getPrimitiveName().equals(value.toString())) {
        return true;
      }
    }
    return false;
  }
}
