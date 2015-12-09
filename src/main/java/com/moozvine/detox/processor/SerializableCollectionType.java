package com.moozvine.detox.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public enum SerializableCollectionType {
  ITERABLE("Iterable", "ArrayList", "Collections.unmodifiableList", "Collections.emptyList()"),
  COLLECTION("Collection", "ArrayList", "Collections.unmodifiableList", "Collections.emptyList()"),
  SET("Set", "HashSet", "Collections.unmodifiableSet", "Collections.emptySet()"),
  SORTED_SET("SortedSet", "TreeSet", "Collections.unmodifiableSortedSet", "new java.util.TreeSet()"),
  LIST("List", "ArrayList", "Collections.unmodifiableList", "Collections.emptyList()");
  // TODO: Support for Map<String, ?>

  final String interfaceName;
  final String concreteName;
  final String toImmutable;
  final String toEmpty;

  SerializableCollectionType(
      final String interfaceName,
      final String concreteName,
      final String toImmutable,
      final String toEmpty) {
    this.interfaceName = interfaceName;
    this.concreteName = concreteName;
    this.toImmutable = toImmutable;
    this.toEmpty = toEmpty;
  }

  public static SerializableCollectionType fromTypeMirror(final TypeMirror value) {
    if (value.getKind().equals(TypeKind.ARRAY)) {
      return null;
    }
    if (!value.getKind().equals(TypeKind.DECLARED)) {
      return null;
    }
    final DeclaredType declared = (DeclaredType) value;
    final Element enclosing = declared.asElement().getEnclosingElement();
    if (enclosing.getKind().equals(ElementKind.PACKAGE)
        && ((PackageElement) enclosing).getQualifiedName().toString().equals("java.util")) {
      for (final SerializableCollectionType supportedCollectionType : SerializableCollectionType.values()) {
        if (supportedCollectionType.interfaceName
            .equals(declared.asElement().getSimpleName().toString())) {
          return supportedCollectionType;
        }
      }
    }
    return null;
  }
}
