package com.moozvine.detox.processor;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;


public enum MemberType {
  NATIVE_TYPE,
  ENUM,
  COLLECTION,
  STRING_MAP,
  SERIALIZABLE_TYPE,
  NON_SERIALIZABLE_TYPE,
  IGNORABLE;

  public static MemberType fromTypeMirror(final TypeMirror typeMirror) {
    if (NativeType.isNativeType(typeMirror)) {
      return MemberType.NATIVE_TYPE;
    }
    if (Util.isEnumType(typeMirror)) {
      return MemberType.ENUM;
    }
    if (Util.isCollectionType(typeMirror)) {
      return MemberType.COLLECTION;
    }
    if (Util.isStringMapType(typeMirror)) {
      return MemberType.STRING_MAP;
    }
    if (Util.isSerializable(typeMirror)) {
      return MemberType.SERIALIZABLE_TYPE;
    }
    if (typeMirror.getKind().equals(TypeKind.DECLARED)) {
      return MemberType.NON_SERIALIZABLE_TYPE;
    }
    return IGNORABLE;
  }
}
