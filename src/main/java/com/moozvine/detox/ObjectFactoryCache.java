package com.moozvine.detox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ObjectFactoryCache {
  private final Map<Key, ObjectFactory<?>> factories = new HashMap<>();

  private final class Key {
    final Class objectClass;
    final Class bound;

    private Key(
        final Class objectClass,
        final Class bound) {
      this.objectClass = objectClass;
      this.bound = bound;
    }

    public Key(final Class objectClass) {
      this.objectClass = objectClass;
      this.bound = null;
    }

    @Override public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      final Key key = (Key) o;

      if (objectClass != null
          ? !objectClass.equals(key.objectClass)
          : key.objectClass != null) return false;
      return !(bound != null
          ? !bound.equals(key.bound)
          : key.bound != null);

    }

    @Override public int hashCode() {
      int result = objectClass != null
          ? objectClass.hashCode()
          : 0;
      result = 31 * result + (bound != null
          ? bound.hashCode()
          : 0);
      return result;
    }

    @Override public String toString() {
      return this.getClass().getName() + " {"
          + "objectClass: " + objectClass
          + "bound:" + bound
          + "}";
    }
  }

  /**
   * Returns an ObjectFactory for the only @GenerateDTO annotated interface of the given object.
   * If the given object does not extend a @GenerateDTO annotated interface, or if it extends more than one of them,
   * a SerializationError is thrown.
   */
  ObjectFactory getFactory(final Serializable obj) throws SerializationError {
    final Class<? extends Serializable> targetClass = obj.getClass();
    final Key key = new Key(targetClass);
    if (!factories.containsKey(key)) {
      register(targetClass, findOnlyGeneratableInterface(targetClass), null);
    }
    return factories.get(key);
  }

  /**
   * Returns an ObjectFactory for the highest @GenerateDTO annotated interface of the given object that is, or is a
   * subtype of, the given bound interface.
   */
  <T extends BOUND, BOUND extends Serializable> ObjectFactory getFactory(final T obj, final Class<BOUND> bound)
      throws SerializationError {
    final Class<T> targetClass = (Class<T>) obj.getClass();
    final Key key = new Key(targetClass, bound);
    if (!factories.containsKey(key)) {
      register(targetClass, findHighestGeneratableInterfaceWithinBound(targetClass, bound), bound);
    }
    return factories.get(key);
  }

  private <T extends GEN, GEN extends BOUND, BOUND extends Serializable> Class<GEN> findHighestGeneratableInterfaceWithinBound(
      final Class<T> objClass,
      final Class<BOUND> bound) {
    final List<Class<?>> result = new ArrayList<>();
    for (final Class<? extends Serializable> candidate : findAllGeneratableInterfaces(objClass)) {
      if (bound.isAssignableFrom(candidate)) {
        result.add(candidate);
      }
    }
    if (result.size() == 0) {
      throw new SerializationError(
          "No @GenerateDTO interfaces found for class " + objClass + " within bound " + bound.getName());
    }

    return (Class<GEN>) result.get(result.size() - 1);
  }

  private <T extends GEN, GEN extends Serializable> Class<GEN> findOnlyGeneratableInterface(final Class<T> objClass) {

    final List<Class<GEN>> result = findAllGeneratableInterfaces(objClass);
    if (result.size() == 0) {
      throw new SerializationError("No @GenerateDTO interfaces found for class " + objClass);
    }
    if (result.size() > 1) {
      throw new SerializationError("Multiple @GenerateDTO interfaces found for class " + objClass + ": " + result);
    }
    return result.get(0);
  }

  private <T extends GEN, GEN extends Serializable> List<Class<GEN>> findAllGeneratableInterfaces(
      final Class<T> objClass) {

    final List<Class<GEN>> result = new ArrayList<>();
    for (final Class<?> candidate : allInterfacesOf(objClass)) {
      if (candidate.getAnnotation(GenerateDTO.class) != null) {
        if (Serializable.class.isAssignableFrom(candidate)) {
          //noinspection unchecked
          result.add((Class<GEN>) candidate);
        } else {
          throw new SerializationError("Class " + candidate + " is tagged with @GenerateDTO, but is not Serializable.");
        }
      }
    }
    return result;
  }

  private static Iterable<Class> allInterfacesOf(final Class targetClass) {
    final List<Class> result = new ArrayList<>();
    for (Class currentClass = targetClass; currentClass != null; currentClass = currentClass.getSuperclass()) {
      for (final Class directInterface : currentClass.getInterfaces()) {
        if (!result.contains(directInterface)) {
          result.add(directInterface);
        }
      }
      for (final Class directInterface : currentClass.getInterfaces()) {
        for (final Class superInterface : allInterfacesOf(directInterface)) {
          if (!result.contains(superInterface)) {
            result.add(superInterface);
          }
        }
      }
    }
    return result;
  }

  /**
   * Returns an ObjectFactory for the specified @GenerateDTO annotated interface.
   */
  <T extends Serializable> ObjectFactory<T> getFactory(final Class<T> generatableInterface) {
    final Key key = new Key(generatableInterface);
    if (!factories.containsKey(key)) {
      register(generatableInterface, generatableInterface, null);
    }
    return (ObjectFactory<T>) factories.get(key);
  }

  ObjectFactory getFactory(final String generatableInterfaceName) throws DeserializationException {
    try {
      final Class<?> targetClass = Class.forName(generatableInterfaceName);
      if (Serializable.class.isAssignableFrom(targetClass)) {
        return getFactory((Class<? extends Serializable>) targetClass);
      } else {
        throw new DeserializationException(
            "Type: " + generatableInterfaceName + " does not extend " + Serializable.class.getName());
      }
    } catch (final ClassNotFoundException e) {
      throw new DeserializationException("Unknown type: " + generatableInterfaceName);
    }
  }

  private <T extends Serializable> void register(
      final Key key,
      final ObjectFactory<T> factory) {
    factories.put(key, factory);
  }

  @SuppressWarnings("unchecked")
  private <T extends GEN, GEN extends BOUND, BOUND extends Serializable> void register(
      final Class<T> objectClass, final Class<GEN> generatableInterface, final Class<BOUND> bound) {
    try {
      final Class<ObjectFactory<T>> factoryClazz
          = (Class<ObjectFactory<T>>) Class.forName(generatableInterface.getName() + "DTO$Factory");
      register(new Key(objectClass, bound), factoryClazz.newInstance());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      throw new IllegalArgumentException("Failed to register " + objectClass
          + ". Check that the class implements Serializable, is tagged " +
          "with the @GenerateDTO annotation and has been compiled.", e);
    }
  }
}
