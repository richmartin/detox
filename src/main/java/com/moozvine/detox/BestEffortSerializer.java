package com.moozvine.detox;


import com.moozvine.detox.processor.Util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A serializer that will attempt to serialize objects on a best-effort basis. Rather than throw SerializationError
 * when it fails to serialize a field it will degrade to String.valueOf(obj). This will result in objects which
 * cannot be deserialized, making this class unsuitable which deserialization is also required. It is only useful in
 * situations such as logging, where an approximate serial form is better than none.
 */
public class BestEffortSerializer extends AbstractSerializationService {

  public BestEffortSerializer() {
    setPrettyPrint();
  }

  @Override public Object toJson(final Object obj) {
    try {
      return super.toJson(obj);
    } catch (final SerializationError e) {
      return String.valueOf(obj);
    }
  }

  @Override public String serialize(final Serializable obj) {
    try {
      return super.serialize(obj);
    } catch (final SerializationError serializationError) {
      return getGuavaString(obj);
    }
  }

  @Override public <T extends Serializable> String serialize(
      final T obj,
      final Class<T> interfaceType) {
    try {
      return super.serialize(obj, interfaceType);
    } catch (final SerializationError serializationError) {
      return getGuavaString(obj);
    }
  }

  public String toString(final Object obj) {
    if (obj instanceof Serializable) {
      return serialize((Serializable) obj);
    }
    return getGuavaString(obj);
  }

  private String getGuavaString(final Object obj) {
    final StringBuilder result = new StringBuilder(obj.getClass().getName() + ":{");
    for (final Method method : obj.getClass().getMethods()) {
      if (Util.isAMember(method)) {
        final String fieldName = Util.getterToFieldName(method.getName());
        try {
          result.append(fieldName);
          result.append(":");
          result.append(method.invoke(obj));
        } catch (IllegalAccessException | InvocationTargetException e) {
          result.append(fieldName);
          result.append(": <");
          result.append(e.getMessage());
          result.append(">");
        }
        result.append(", ");
      }
    }
    return result.toString().replaceAll(", $", "") + "}";
  }
}
