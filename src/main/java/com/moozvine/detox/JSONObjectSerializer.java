package com.moozvine.detox;

import org.json.JSONObject;

/**
 * Created by rich on 06/07/15.
 */
public abstract class JSONObjectSerializer<T> implements AbstractSerializationService.Serializer<T> {
  private final Class<T> targetType;

  public JSONObjectSerializer(final Class<T> targetType) {
    this.targetType = targetType;
  }

  @Override public Class<T> getTargetType() {
    return targetType;
  }

  @Override public abstract T fromJSONObject(final JSONObject value) throws DeserializationException;

  @Override final public T fromString(final String value) throws DeserializationException {
    throw new DeserializationException("From String not implemented on Serializer " + this.getClass());
  }

  @Override public abstract JSONObject toJson(final T value);
}
