package com.moozvine.detox;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TestInstanceGenerator {


  private final Map<Class, Function<String, ? extends Object>> valueGenerators = coreValueGenerators();
  private final Map<Key, Function<String, ? extends Object>> overrideGenerators = new HashMap<>();

  private Map<Class, Function<String, ? extends Object>> coreValueGenerators() {
    try {
      final Map<Class, Function<String, ? extends Object>> result = new HashMap<>();
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
      builder = invokeSetter(builder, Iterables.getOnlyElement(listSetters(builder)));
    }
    for (final Method setter : listSetters(builder)) {
      invokeSetter(builder, setter);
    }
    return (T) invokeBuild(builder);
  }

  public <T> void setValueGeneratorForClass(
      final Class<T> memberClass,
      final Function<String, T> valueGenerator) {
    valueGenerators.put(memberClass, valueGenerator);
  }

  public <T> void setValueGeneratorForField(
      final Class<T> memberClass,
      final String fieldName,
      final Function<String, T> valueGenerator) {
    overrideGenerators.put(new Key(memberClass, fieldName), valueGenerator);
  }

  public static <T> Function<String, T> constant(final T value) {
    return new Function<String, T>() {
      @Override public T apply(final String input) {
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
    final ImmutableList.Builder<Method> result = ImmutableList.builder();
    for (final Method method : builder.getClass().getMethods()) {
      if (method.getName().startsWith("with") && method.getParameterTypes().length == 1) {
        result.add(method);
      }
    }
    return result.build();
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
      return Objects.equal(type, key.type) &&
          Objects.equal(fieldName, key.fieldName);
    }

    @Override public int hashCode() {
      return Objects.hashCode(type, fieldName);
    }
  }
}