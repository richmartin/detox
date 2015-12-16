package com.moozvine.detox;

import com.moozvine.detox.repackaged.org.json.JSONException;
import com.moozvine.detox.repackaged.org.json.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractSerializationService implements SerializationService {

  private final Map<Class, Serializer> serializers = new HashMap<>();
  private final ObjectFactoryCache factoryCache = new ObjectFactoryCache();
  private int indentFactor = 0;

  public AbstractSerializationService() {
    registerSerializer(StandardSerializers.INTEGER);
    registerSerializer(StandardSerializers.LONG);
    registerSerializer(StandardSerializers.BIG_INTEGER);
    registerSerializer(StandardSerializers.FLOAT);
    registerSerializer(StandardSerializers.DOUBLE);
    registerSerializer(StandardSerializers.BIG_DECIMAL);
    registerSerializer(StandardSerializers.BOOLEAN);
    registerSerializer(StandardSerializers.STRING);
  }

  @Override
  public Object deserialize(final InputStream stream)
      throws DeserializationException, IOException {
    return deserialize(new InputStreamReader(stream, "UTF-8"));
  }

  @Override
  public Object deserialize(final Reader reader)
      throws DeserializationException, IOException {
    return deserialize(reader, Object.class);
  }

  @Override
  public <T> T deserialize(
      final InputStream stream,
      final Class<T> expectedType)
      throws DeserializationException, IOException {
    return deserialize(new InputStreamReader(stream, "UTF-8"), expectedType);
  }

  @Override
  public <T> T deserialize(
      final Reader reader,
      final Class<T> expectedType)
      throws DeserializationException, IOException {
    try (BufferedReader buffer = new BufferedReader(reader)) {
      final StringBuilder result = new StringBuilder();
      for (String line = buffer.readLine(); line != null; line = buffer.readLine()) {
        result.append(line);
        result.append('\n');
      }
      return deserialize(result.toString(), expectedType);
    }
  }

  @Override
  public Object deserialize(final String serialized) throws DeserializationException {
    return deserialize(serialized, Object.class);
  }

  @Override
  public <T> T deserialize(
      final String serialized,
      final Class<T> expectedType) throws DeserializationException {

    try {
      if (serializers.containsKey(expectedType)) {
        return (T) serializers.get(expectedType).fromString(serialized);
      } else {
        return deserialize(new JSONObject(serialized), expectedType);
      }
    } catch (JSONException | IllegalArgumentException e) {
      throw new DeserializationException("Failed to parse JSON string:\n" + serialized, e);
    }
  }

  @Override
  public Object deserialize(final JSONObject json) throws DeserializationException {
    return deserialize(json, Object.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T deserialize(
      final Object object,
      final Class<T> expectedType) throws DeserializationException {
    if (object.equals(JSONObject.NULL)) {
      return null;
    }
    if (serializers.containsKey(expectedType)) {
      if (object instanceof String) {
        return (T) serializers.get(expectedType).fromString((String) object);
      } else if (object instanceof JSONObject) {
        return (T) serializers.get(expectedType).fromJSONObject((JSONObject) object);
      } else {
        throw new DeserializationException(
            "Attempt to deserialize from unexpected type: " + object.getClass() + ": " + object);
      }
    }

    // If we don't have a serializer registered for this type, then we need it to be a
    // JSONObject with a serializedType field.
    if (!(object instanceof JSONObject)) {
      throw new DeserializationException(
          "Cannot deserialize a " + expectedType + " from a " + object.getClass() + ": " + object);
    }

    final JSONObject jsonObject = (JSONObject) object;
    final String serializedType = jsonObject.getString("serializedType");
    final ObjectFactory factory = factoryCache.getFactory(serializedType);
    final DTO dto = factory.createDTO(this, jsonObject);
    try {
      return (T) dto;
    } catch (final ClassCastException e) {
//      return null;
      throw new SerializationError("Deserialized object of type " + serializedType
          + " (" + dto.getClass() + ") "
          + " could not be cast to " + expectedType);
    }
  }

  @Override
  public Object toJson(final Object obj) throws SerializationError {
    if (Serializable.class.isAssignableFrom(obj.getClass())) {
      return createJSONObject((Serializable) obj).toString(indentFactor);
    } else if (Enum.class.isAssignableFrom(obj.getClass())) {
      return ((Enum) obj).name();
    } else {
      if (!serializers.containsKey(obj.getClass())) {
        throw new SerializationError(
            "Object is not Serializable and no custom serializer registered for type " + obj.getClass());
      }
      return serializers.get(obj.getClass()).toJson(obj);
    }
  }

  @Override
  public void write(
      final Serializable obj,
      final OutputStream out) throws IOException {
    out.write(serialize(obj).getBytes("UTF-8"));
  }

  @Override
  public <T extends Serializable> void write(
      final T obj,
      final Class<T> interfaceType,
      final OutputStream out)
      throws SerializationError, IOException {
    out.write(serialize(obj, interfaceType).getBytes("UTF-8"));
  }

  public void setPrettyPrint() {
    indentFactor = 2;
  }

  public void setUglyPrint() {
    indentFactor = 0;
  }

  @Override
  public <T extends Serializable> String serialize(
      final T obj,
      final Class<T> interfaceType)
      throws SerializationError {
    return createJSONObject(obj, interfaceType).toString(indentFactor);
  }

  @Override
  public <T extends Serializable> JSONObject createJSONObject(
      final T obj,
      final Class<T> interfaceClass) {
    return createDTO(obj, interfaceClass).toJson();
  }

  public <T extends Serializable> DTO createDTO(
      final T obj,
      final Class<T> clazz) {
    return factoryCache.getFactory(obj, clazz).createDTO(this, obj);
  }


  /**
   * Given a Serializable object and no hint from the caller as to what type it should be serialized as, we need to
   * throw an error if the object implements more than one (or zero) @GenerateDTO interfaces.
   */
  @Override
  public String serialize(final Serializable obj) throws SerializationError {
    return createJSONObject(obj).toString(indentFactor);
  }

  public JSONObject createJSONObject(final Serializable obj) {
    return createDTO(obj).toJson();
  }

  @SuppressWarnings("unchecked")
  public DTO createDTO(final Serializable obj) {
    return factoryCache.getFactory(obj).createDTO(this, obj);
  }

  protected final void registerSerializer(final Serializer<?> serializer) {
    serializers.put(serializer.getTargetType(), serializer);
  }

  public interface Serializer<T> {
    Class<T> getTargetType();
    T fromString(String value) throws DeserializationException;
    T fromJSONObject(JSONObject value) throws DeserializationException;
    Object toJson(T value);
  }
}
