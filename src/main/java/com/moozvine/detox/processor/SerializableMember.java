package com.moozvine.detox.processor;

import com.moozvine.detox.Validate;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import java.util.List;

/**
* Created by rich on 17/07/14.
*/
public class SerializableMember {
  private static final String[] SUPPORTED_NULLABLE_ANNOTATIONS = new String[]{
      "javax.annotation.Nullable",
      "org.jetbrains.annotations.Nullable",
      "edu.umd.cs.findbugs.annotations.Nullable",
      "android.support.annotation.Nullable"
  };

  final ExecutableElement method;
  private final String getterName;
  private final String fieldName;
  private final String jsonFieldName;
  private final TypeMirror typeMirror;
  private final MemberType memberType;
  private final boolean formsId;

  SerializableMember(
      final ExecutableElement method,
      final String getterName,
      final String fieldName,
      final String jsonFieldName,
      final TypeMirror typeMirror,
      final boolean formsId) {
    this.method = method;
    this.getterName = getterName;
    this.fieldName = fieldName;
    this.jsonFieldName = jsonFieldName;
    this.typeMirror = typeMirror;
    this.memberType = MemberType.fromTypeMirror(typeMirror);
    this.formsId = formsId;
  }

  public boolean hasAnnotation(final String annotationFullName) {
    final List<? extends AnnotationMirror> annotationMirrors = method.getAnnotationMirrors();
    for (final AnnotationMirror annotationMirror : annotationMirrors) {
      if (annotationMirror.getAnnotationType().toString().equals(annotationFullName)) {
        return true;
      }
    }
    return false;
  }

  public String getBuilderInterfaceName() {
    return "Requires" + getFieldName().substring(0, 1).toUpperCase() + getFieldName().substring(1);
  }

  String getGetterName() {
    return getterName;
  }

  String getFieldName() {
    return fieldName;
  }

  String getJsonFieldName() {
    return jsonFieldName;
  }

  TypeMirror getTypeMirror() {
    return typeMirror;
  }

  MemberType getMemberType() {
    return memberType;
  }

  DeclaredType getCollectionMemberType() throws InvalidTypeException {
    if (memberType != MemberType.COLLECTION) {
      throw new IllegalStateException("Cannot get collection member type for a non-collection: " + fieldName);
    }
    return Util.getCollectionMemberTypeMirror(getTypeMirror());
  }

  public boolean hasValidator() {
    return hasAnnotation(Validate.class.getCanonicalName());
  }

  // TODO: This should really return all of the validator up through any overridden members.
  public String getValidator() {
    try {
      return method.getAnnotation(Validate.class).value().getCanonicalName();
    } catch (final MirroredTypeException e) {
      return e.getTypeMirror().toString();
    }
  }

  String getTypeAsString() throws InvalidTypeException {
    return SerializableCollectionType.fromTypeMirror(getTypeMirror()) != null
        ? Util.typeToString(getTypeMirror()) + "<" + Util.getCollectionMemberTypeMirror(getTypeMirror()) + ">"
        : Util.typeToString(getTypeMirror());
  }


  boolean isFormsId() {
    return formsId;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final SerializableMember member = (SerializableMember) o;
    return getFieldName().equals(member.getFieldName())
        && getGetterName().equals(member.getGetterName())
        && getTypeMirror().equals(member.getTypeMirror());
  }

  @Override
  public int hashCode() {
    int result = getGetterName().hashCode();
    result = 31 * result + getFieldName().hashCode();
    result = 31 * result + getTypeMirror().hashCode();
    return result;
  }

  @Override
  public String toString() {
    try {
      return getTypeAsString() + " " + getGetterName() + "()";
    } catch (InvalidTypeException e) {
      return "[UNKNOWN] " + getGetterName() + "()";
    }
  }

  public String getNullableAnnotation() {
    for (final String supportedNullableAnnotation : SUPPORTED_NULLABLE_ANNOTATIONS) {
      if (hasAnnotation(supportedNullableAnnotation)) {
        return supportedNullableAnnotation;
      }
    }
    return null;
  }

  public String getTypeAsCode() {
    switch (getMemberType()) {
      case SERIALIZABLE_TYPE:
      case NON_SERIALIZABLE_TYPE:
        return Util.declaredTypeToString((DeclaredType) getTypeMirror());

      default:
        throw new RuntimeException(new InvalidTypeException());

    }
  }
}
