package com.moozvine.detox.processor;

import com.moozvine.detox.Serializable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Util {
  private Util() {
  }

  public static String typeToString(final TypeMirror value) throws InvalidTypeException {
    switch (value.getKind()) {
      case BOOLEAN:
      case BYTE:
      case CHAR:
      case DOUBLE:
      case FLOAT:
      case INT:
      case LONG:
      case SHORT:
        return value.getKind().name().toLowerCase();
      case DECLARED:
        return declaredTypeToString((DeclaredType) value);
      default:
        // TODO: support arrays of Serializable types
        throw new InvalidTypeException(value.getKind() + " not currently supported!");
    }
  }

  public static boolean isString(final TypeMirror value) {
    return value.toString().equals("java.lang.String");
  }

  public static String declaredTypeToString(final DeclaredType value) {
    if (isString(value)) {
      return "String";
    }
    // Iterable<Something> -> JSONArray
    if (isCollectionType(value)) {
      return value.asElement().getSimpleName().toString();
    }
    return value.toString();
  }

  public static DeclaredType getCollectionMemberTypeMirror(final TypeMirror type) throws InvalidTypeException {
    if (!(isClassOrInterface(type) && SerializableCollectionType.fromTypeMirror(type) != null)) {
      return null;
    }
    final List<? extends TypeMirror> typeArguments = ((DeclaredType) type).getTypeArguments();
    if (typeArguments.size() != 1) {
      throw new InvalidTypeException(type + " is not a valid serializable return type.");
    }
    return (DeclaredType) typeArguments.get(0);
  }

  static boolean isCollaborator(final TypeMirror value) {
    return isClassOrInterface(value)
        && !NativeType.isNativeType(value)
        && !hasValueOfMethod(value)
        && !isCollectionType(value)
        && !isStringMapType(value);
  }

  public static boolean isCollectionType(final TypeMirror value) {
    return SerializableCollectionType.fromTypeMirror(value) != null;
  }

  public static boolean isStringMapType(final TypeMirror value) {
    return StringMapType.fromTypeMirror(value) != null;
  }

  public static boolean isClassOrInterface(final TypeMirror value) {
    return value.getKind().equals(TypeKind.DECLARED);
  }

  static boolean hasValueOfMethod(final TypeMirror value) {
    if (!isClassOrInterface(value)) {
      return false;
    }
    final DeclaredType declared = (DeclaredType) value;
    final TypeElement typeElement = (TypeElement) declared.asElement();
    for (final Element element : typeElement.getEnclosedElements()) {
      if (element.getKind().equals(ElementKind.METHOD)) {
        final ExecutableElement executableElement = (ExecutableElement) element;
        if (executableElement.getSimpleName().toString().equals("valueOf")
            && executableElement.getReturnType().toString().equals(value.toString())
            && executableElement.getParameters().size() == 1
            && executableElement.getParameters().get(0).asType().toString().equals("java.lang.String")) {
          return true;
        }
      }
    }
    return false;
  }

  static boolean isSerializable(final TypeMirror value) {
    if (!value.getKind().equals(TypeKind.DECLARED)) {
      return false;
    }
    for (final DeclaredType anInterface : allInterfacesOf((DeclaredType) value)) {
      if (String.valueOf(anInterface).equals(Serializable.class.getName())) {
        return true;
      }
    }
    return false;
  }

  public static Set<TypeMirror> allSupertypesOf(final DeclaredType memberType) {
    final Set<TypeMirror> result = new HashSet<>();
    result.addAll(allInterfacesOf(memberType));
    result.addAll(allSuperclassesOf(memberType));
    return result;
  }

  public static TypeElement asTypeElement(final DeclaredType typeMirror) {
    return (TypeElement) typeMirror.asElement();
  }

  public static Set<TypeMirror> allSuperclassesOf(final DeclaredType type) {
    final Set<TypeMirror> result = new HashSet<>();
    for (TypeMirror superType = asTypeElement(type).getSuperclass();
         superType.getKind().equals(TypeKind.DECLARED);
         superType = asTypeElement((DeclaredType) superType).getSuperclass()) {
      result.add(superType);
    }
    return result;
  }

  static Set<DeclaredType> allInterfacesOf(final DeclaredType value) {
    final Set<DeclaredType> interfaces = new HashSet<>();
    accumulateAllInterfacesOf(value, interfaces);
    return interfaces;
  }

  private static void accumulateAllInterfacesOf(
      final DeclaredType value,
      final Set<DeclaredType> accumulator) {
    final TypeElement element = (TypeElement) value.asElement();
    final TypeMirror superclass = element.getSuperclass();
    if (!superclass.getKind().equals(TypeKind.NONE)) {
      accumulateAllInterfacesOf((DeclaredType) superclass, accumulator);
    }
    for (final TypeMirror typeMirror : element.getInterfaces()) {
      final DeclaredType declaredInterface = (DeclaredType) typeMirror;
      accumulator.add(declaredInterface);
      accumulateAllInterfacesOf(declaredInterface, accumulator);
    }
  }

  static boolean isEnumType(final TypeMirror value) {
    if (!isClassOrInterface(value)) {
      return false;
    }
    final DeclaredType declared = (DeclaredType) value;
    final TypeElement typeElement = (TypeElement) declared.asElement();
    return typeElement.getSuperclass().toString().startsWith("java.lang.Enum<");
  }

  static String degenerify(final String possiblyGenerifiedType) {
    return possiblyGenerifiedType.replaceAll("<.*>", "");
  }

  static String typeToJsonArrayGetter(
      final String jsonVariableName,
      final TypeMirror value,
      final String indexVariable)
      throws InvalidTypeException {
    switch (value.getKind()) {
      case BOOLEAN:
      case DOUBLE:
      case INT:
      case LONG:
        return String.format("" +
                "%3$s.get%1$s(%2$s)",
            value.getKind().name().substring(0, 1) + value.getKind().name().substring(1).toLowerCase(),
            indexVariable,
            jsonVariableName
        );
      case BYTE:
      case CHAR:
      case SHORT:
        return String.format("(%1$s) %3$s.getInt(%2$s)",
            value.getKind().name().toLowerCase(),
            indexVariable,
            jsonVariableName);

      case FLOAT:
        return "(float) " + jsonVariableName + ".getDouble(" + indexVariable + ")";

      case DECLARED:
        if (isString(value)) {
          return String.format("%1$s.getString(%2$s)",
              jsonVariableName, indexVariable);
        } else if (NativeType.isNativeType(value)) {
          return jsonVariableName + "." + getPrimitiveJSONGetter((DeclaredType) value) + "(" + indexVariable + ")";
        } else if (hasValueOfMethod(value)) {
          return String.format("%1$s.valueOf(%2$s.getString(%3$s))",
              value.toString(),
              jsonVariableName,
              indexVariable);
        } else {
          return jsonVariableName + ".getJSONObject(" + indexVariable + ")";
        }
      default:
        // TODO: support arrays of Serializable types
        throw new InvalidTypeException(value.getKind() + " not yet currently supported");
    }
  }

  static String typeToJsonGetter(
      final String jsonVariableName,
      final TypeMirror value,
      final String jsonFieldName) throws InvalidTypeException {
    switch (value.getKind()) {
      case BOOLEAN:
      case DOUBLE:
      case INT:
      case LONG:
        return String.format("" +
                "%3$s.get%1$s(\"%2$s\")",
            value.getKind().name().substring(0, 1) + value.getKind().name().substring(1).toLowerCase(),
            jsonFieldName,
            jsonVariableName
        );
      case BYTE:
      case CHAR:
      case SHORT:
        return String.format("(%1$s) %3$s.getInt(\"%2$s\")",
            value.getKind().name().toLowerCase(),
            jsonFieldName,
            jsonVariableName);

      case FLOAT:
        return "(float) " + jsonVariableName + ".getDouble(\"" + jsonFieldName + "\")";

      case DECLARED:
        if (Util.isString(value)) {
          return String.format("%2$s.has(\"%1$s\") && !%2$s.isNull(\"%1$s\")?%2$s.getString(\"%1$s\"):null",
              jsonFieldName, jsonVariableName);
        } else if (Util.hasValueOfMethod(value)) {
          return String.format("%2$s.has(\"%3$s\") && !%2$s.isNull(\"%3$s\")" +
                  "?%1$s.valueOf(%2$s.getString(\"%3$s\")):null",
              value.toString(),
              jsonVariableName,
              jsonFieldName);
        } else {

          return String.format("%2$s.has(\"%1$s\") && !%2$s.isNull(\"%1$s\")" +
                  "?(%3$s)%2$s.getJSONObject(\"%1$s\"):null",
              jsonFieldName,
              jsonVariableName,
              value.toString());
        }
      default:
        // TODO: support arrays of Serializable types
        throw new InvalidTypeException(value.getKind() + " not yet currently supported");
    }
  }

  static String getPrimitiveJSONGetter(final DeclaredType value) throws InvalidTypeException {
    switch (value.asElement().getSimpleName().toString()) {
      case "Integer":
        return "getInt";
      case "Boolean":
        return "getBoolean";
      case "Long":
        return "getLong";
      case "Float":
        return "getDouble";
      case "Double":
        return "getDouble";
      case "Short":
        return "getInt";
      case "Byte":
        return "getInt";
      default:
        throw new InvalidTypeException();
    }
  }

  public static String upperCaseFirstChar(final String value) {
    if (value.length() < 1) {
      return "";
    }
    return value.substring(0, 1).toUpperCase() + value.substring(1);
  }

  public static boolean isAMember(
      final ExecutableElement method,
      final String methodName) {
    return method.getParameters().isEmpty() &&
        (methodName.startsWith("get")
            || methodName.startsWith("has")
            || methodName.startsWith("is"));
  }

  public static boolean isAMember(
      final Method method) {
    return method.getParameterTypes().length == 0 &&
        (method.getName().startsWith("get")
            || method.getName().startsWith("has")
            || method.getName().startsWith("is"));
  }

  public static String getterToFieldName(final String getterName) {

    final int prefixLength;
    if (getterName.startsWith("is")) {
      prefixLength = 2;
    } else {
      prefixLength = 3;
    }
    final String stripped = getterName.substring(prefixLength);
    if (stripped.length() == 1) {
      return stripped.toLowerCase();
    } else {
      return stripped.substring(0, 1).toLowerCase()
          + stripped.substring(1);
    }
  }
}
