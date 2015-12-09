package com.moozvine.detox.processor;

import com.moozvine.detox.GenerateBuilder;
import com.moozvine.detox.GenerateDTO;
import com.moozvine.detox.Id;
import com.moozvine.detox.JsonFieldName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ElementToProcess {
  private final ProcessingEnvironment processingEnvironment;

  private final PackageElement packageElement;
  private final String interfaceSimpleName;
  private final List<SerializableMember> members;
  private final String validatorFullName;

  public ElementToProcess(
      final ProcessingEnvironment processingEnvironment,
      final TypeElement interfaceElement) {
    this.processingEnvironment = processingEnvironment;

    Element enclosing = interfaceElement.getEnclosingElement();
    String interfaceName = interfaceElement.getSimpleName().toString();
    while (!enclosing.getKind().equals(ElementKind.PACKAGE)) {
      interfaceName = enclosing.getSimpleName() + "." + interfaceName;
      enclosing = enclosing.getEnclosingElement();
    }
    interfaceSimpleName = interfaceName;
    packageElement = (PackageElement) enclosing;
    members = buildMemberList(interfaceElement);

    if (getAnnotation(interfaceElement, GenerateDTO.class) != null
        && getAnnotation(interfaceElement, GenerateBuilder.class) != null) {
      throw new IllegalStateException(
          "Type " + interfaceElement + " must not be annotated with both GenerateDTO and GenerateBuilder. " +
              "Just use GenerateDTO.");
    }

    final AnnotationValue validatorValue = getValidatorValue(interfaceElement);
    validatorFullName = validatorValue == null
        ? null
        : validatorValue.getValue().toString();
  }

  private AnnotationValue getValidatorValue(
      final TypeElement interfaceElement) {
    final AnnotationMirror generateDtoAnnotation = getAnnotation(interfaceElement, GenerateDTO.class);
    final AnnotationMirror annotation = generateDtoAnnotation != null
        ? generateDtoAnnotation
        : getAnnotation(interfaceElement, GenerateBuilder.class);

    if (annotation == null) {
      throw new IllegalStateException("No GenerateDTO or GenerateBuilder annotation on type " + interfaceElement);
    }

    for (final Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
        : annotation.getElementValues().entrySet()) {
      if (entry.getKey().getSimpleName().toString().equals("validator")) {
        return entry.getValue();
      }
    }
    return null;
  }

  private AnnotationMirror getAnnotation(
      final TypeElement interfaceElement,
      final Class annotationClass) {
    for (final AnnotationMirror annotationMirror : interfaceElement.getAnnotationMirrors()) {
      if (annotationMirror.getAnnotationType().toString().equals(annotationClass.getCanonicalName())) {
        return annotationMirror;
      }
    }
    return null;
  }

  public String getPackageName() {
    return packageElement.getQualifiedName().toString();
  }

  public String getDtoSimpleName() {
    return interfaceSimpleName.replace('.', '$') + "DTO";
  }

  public String getDtoFullName() {
    return getPackageName() + "." + getDtoSimpleName();
  }

  public String getBuilderSimpleName() {
    return interfaceSimpleName.replace('.', '$') + "Builder";
  }

  public String getInterfaceSimpleName() {
    return interfaceSimpleName;
  }

  public List<SerializableMember> getSerializableMembers() {
    return members;
  }

  public List<SerializableMember> getNullableMembers() {
    final List<SerializableMember> result = new ArrayList<>();
    for (final SerializableMember serializableMember : getSerializableMembers()) {
      if (serializableMember.getNullableAnnotation() != null) {
        result.add(serializableMember);
      }
    }
    return result;
  }

  public List<SerializableMember> getNonNullMembers() {
    final List<SerializableMember> result = new ArrayList<>(members);
    result.removeAll(getNullableMembers());
    return result;
  }

  private List<SerializableMember> buildMemberList(final TypeElement toSerialize) {
    final List<SerializableMember> result = new ArrayList<>();
    for (final Element element : toSerialize.getEnclosedElements()) {
      if (element.getKind().equals(ElementKind.METHOD)) {
        final ExecutableElement method = (ExecutableElement) element;
        final String methodName = method.getSimpleName().toString();
        if (Util.isAMember(method, methodName)) {
          final String fieldName = Util.getterToFieldName(methodName);
          final String jsonFieldName = method.getAnnotation(JsonFieldName.class) == null
              ? fieldName
              : method.getAnnotation(JsonFieldName.class).value();
          final TypeMirror returnType = method.getReturnType();
          final SerializableMember member = new SerializableMember(
              method,
              methodName,
              fieldName,
              jsonFieldName,
              returnType,
              method.getAnnotation(Id.class) != null);
          if (!result.contains(member)) {
            result.add(member);
          }
        } else {
          processingEnvironment.getMessager().printMessage(
              Diagnostic.Kind.WARNING,
              "Unsupported method on " + toSerialize.getSimpleName() + ": " +
                  method.getSimpleName() + " is not a simple getter.");
        }
      }
    }
    for (final TypeMirror superInterface : toSerialize.getInterfaces()) {
      if (superInterface.getKind().equals(TypeKind.DECLARED)) {
        final List<SerializableMember> superMembers = buildMemberList((TypeElement) ((DeclaredType) superInterface).asElement());
        final List<SerializableMember> superMembersToInclude = new ArrayList<>();
        for (final SerializableMember superMember : superMembers) {
          if (!result.contains(superMember)) {
            superMembersToInclude.add(superMember);
          }
        }
        result.addAll(0, superMembersToInclude);
      }
    }
    resolveOverrides(result);
    return result;
  }

  private void resolveOverrides(final List<SerializableMember> members) {
    final List<SerializableMember> toRemove = new ArrayList<>();
    for (final SerializableMember member : members) {
      for (final SerializableMember other : members) {
        if (!member.equals(other) && member.getGetterName().equals(other.getGetterName())) {
          if (!member.getTypeMirror().getKind().equals(TypeKind.DECLARED) ||
              !other.getTypeMirror().getKind().equals(TypeKind.DECLARED)) {
            processingEnvironment.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                String.format("Both %s and %s declare the member %s, but they have incompatible types (%s & %s)",
                    member,
                    other,
                    member.getFieldName(),
                    member.getTypeMirror(),
                    other.getTypeMirror()));
            throw new IllegalStateException();
          }
          if (Util.allSupertypesOf((DeclaredType) member.getTypeMirror()).contains(other.getTypeMirror())) {
            toRemove.add(other);
          }
        }
      }
    }
    members.removeAll(toRemove);
  }

  public String getBuilderFullName() {
    return getPackageName() + "." + getBuilderSimpleName();
  }

  public String getValidatorFullName() {
    return validatorFullName;
  }
}
