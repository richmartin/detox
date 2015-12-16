package com.moozvine.detox;

import com.moozvine.detox.repackaged.org.json.JSONObject;

/**
 * Created by rich on 06/07/15.
 */
public abstract class StringSerializer<T> implements AbstractSerializationService.Serializer<T> {
  private final Class<T> targetType;

  public StringSerializer(final Class<T> targetType) {
    this.targetType = targetType;
  }

  @Override public Class<T> getTargetType() {
    return targetType;
  }

  @Override final public T fromJSONObject(final JSONObject value) throws DeserializationException {
    throw new DeserializationException("From JSONObject not implemented on Serializer " + this.getClass());
  }

  @Override public abstract T fromString(final String value) throws DeserializationException;

  @Override public abstract String toJson(final T value);
}
