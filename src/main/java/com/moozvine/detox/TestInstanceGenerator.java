package com.moozvine.detox;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public final class TestInstanceGenerator {


  private final Map<Class, ValueGenerator<? extends Object>> valueGenerators = coreValueGenerators();
  private final Map<Key, ValueGenerator<? extends Object>> overrideGenerators = new HashMap<>();

  private Map<Class, ValueGenerator<? extends Object>> coreValueGenerators() {
    try {
      final Map<Class, ValueGenerator<? extends Object>> result = new HashMap<>();
      result.put(String.class, constant("some string"));
      result.put(Byte.class, constant(123));
      result.put(Byte.TYPE, constant(1234));
      result.put(Integer.class, constant(123));
      result.put(Integer.TYPE, constant(1234));
      result.put(Float.class, constant(123.45F));
      result.put(Float.TYPE, constant(1234.67F));
      result.put(Double.class, constant(123.45D));
      result.put(Double.TYPE, constant(1234.67D));
      result.put(Long.class, constant(2445L));
      result.put(Long.TYPE, constant(2445L));
      result.put(Boolean.class, constant(true));
      result.put(Boolean.TYPE, constant(true));
      result.put(URL.class, constant(new URL("http://localhost")));
      return result;
    } catch (final MalformedURLException e) {
      throw new AssertionError(e);
    }
  }

  public <T extends Serializable> T generate(final Class<T> messageClass)
      throws Exception {
    final Class<? extends Serializable> builderClass
        = (Class<? extends Serializable>) Class.forName(messageClass.getName() + "Builder");

    Object builder = builderClass.getMethod("newBuilder").invoke(null);
    while (!hasBuildMethod(builder)) {
      final List<Method> setters = listSetters(builder);
      if (setters.isEmpty()) {
        throw new NoSuchElementException("Iterable is empty.");
      }
      if (setters.size() != 1) {
        throw new IllegalArgumentException("Iterable has more than one element.");
      }

      builder = invokeSetter(builder, setters.get(0));
    }
    for (final Method setter : listSetters(builder)) {
      invokeSetter(builder, setter);
    }
    return (T) invokeBuild(builder);
  }

  public <T> void setValueGeneratorForClass(
      final Class<T> memberClass,
      final ValueGenerator<T> valueGenerator) {
    valueGenerators.put(memberClass, valueGenerator);
  }

  public <T> void setValueGeneratorForField(
      final Class<T> memberClass,
      final String fieldName,
      final ValueGenerator<T> valueGenerator) {
    overrideGenerators.put(new Key(memberClass, fieldName), valueGenerator);
  }

  public static <T> ValueGenerator<T> constant(final T value) {
    return new ValueGenerator<T>() {
      @Override public T apply(final String fieldName) {
        return value;
      }
    };
  }

  private Serializable invokeBuild(final Object builder) throws Exception {
    return (Serializable) builder.getClass().getMethod("build").invoke(builder);
  }

  private Object invokeSetter(
      final Object builder,
      final Method setter) throws Exception {
    final Object dummyValue = getDummyValueForType(setter.getParameterTypes()[0], setterNameToFieldName(setter.getName()));
    System.err.println("Invoking setter " + setter + " with value " + dummyValue);
    return setter.invoke(builder, dummyValue);
  }

  private Object getDummyValueForType(
      final Class<?> memberClass,
      final String fieldName) {
    final Key key = new Key(memberClass, fieldName);
    if (overrideGenerators.containsKey(key)) {
      return overrideGenerators.get(key).apply(fieldName);
    }
    if (valueGenerators.containsKey(memberClass)) {
      return valueGenerators.get(memberClass).apply(fieldName);
    }
    throw new IllegalArgumentException("No value generator registered for class " + memberClass + " needed for field "
        + fieldName);
  }

  private List<Method> listSetters(final Object builder) {
    final List<Method> result = new ArrayList<>();
    for (final Method method : builder.getClass().getMethods()) {
      if (method.getName().startsWith("with") && method.getParameterTypes().length == 1) {
        result.add(method);
      }
    }
    return result;
  }

  private boolean hasBuildMethod(final Object builder) {
    for (final Method method : builder.getClass().getMethods()) {
      if (method.getName().equals("build")) {
        return true;
      }
    }
    return false;
  }

  public String setterNameToFieldName(final String setterName) {
    return setterName.substring(4, 5).toLowerCase() + setterName.substring(5);
  }


  private static class Key {
    private final Class type;
    private final String fieldName;

    private Key(
        final Class type,
        final String fieldName) {
      this.type = type;
      this.fieldName = fieldName;
    }

    @Override public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      final Key key = (Key) o;

      if (type != null
          ? !type.equals(key.type)
          : key.type != null) return false;
      return !(fieldName != null
          ? !fieldName.equals(key.fieldName)
          : key.fieldName != null);

    }

    @Override public int hashCode() {
      int result = type != null
          ? type.hashCode()
          : 0;
      result = 31 * result + (fieldName != null
          ? fieldName.hashCode()
          : 0);
      return result;
    }
  }
}
