package com.moozvine.detox.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;

public class StringMapType {

  private final DeclaredType keyType; // String or an enum
  private final DeclaredType memberType;

  public StringMapType(
      final DeclaredType keyType,
      final DeclaredType memberType) {
    this.keyType = keyType;
    this.memberType = memberType;
  }

  public static StringMapType fromTypeMirror(final TypeMirror value) {
    if (value.getKind().equals(TypeKind.ARRAY)) {
      return null;
    }
    if (!value.getKind().equals(TypeKind.DECLARED)) {
      return null;
    }
    final DeclaredType declared = (DeclaredType) value;
    final Element enclosing = declared.asElement().getEnclosingElement();
    if (enclosing.getKind().equals(ElementKind.PACKAGE)
        && ((PackageElement) enclosing).getQualifiedName().toString().equals("java.util")
        && declared.asElement().getSimpleName().toString().equals("Map")) {
      final List<? extends TypeMirror> typeArguments = declared.getTypeArguments();
      if (typeArguments.size() != 2 || !isSupportedKeyType(typeArguments.get(0))) {
        return null;
      }
      final TypeMirror memberType = typeArguments.get(1);
      if (memberType instanceof DeclaredType) {
        return new StringMapType((DeclaredType) typeArguments.get(0), (DeclaredType) memberType);
      }
    }
    return null;
  }

  private static boolean isSupportedKeyType(final TypeMirror type) {
    return type.toString().equals("java.lang.String")
        || Util.isEnumType(type);
  }

  public DeclaredType getKeyType() {
    return keyType;
  }

  public DeclaredType getMemberType() {
    return memberType;
  }
}
