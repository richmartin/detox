package com.moozvine.detox;

import com.moozvine.detox.repackaged.org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

public interface SerializationService {

  // TODO: serialize* methods are about turning our Java objects into strings; if this fails it
  // it a programming error and should be an unchecked exception. deserialize* methods are about
  // turning data provided by the service caller into Java objects; if this fails it is a data
  // error and should be a checked exception. Split SerializationException into two.

  <T extends Serializable> JSONObject createJSONObject(
      T obj,
      Class<T> interfaceClass);
  String serialize(Serializable obj) throws SerializationError;

  Object toJson(Object obj) throws SerializationError;

  void write(
      Serializable obj,
      OutputStream out) throws IOException;

  <T extends Serializable> void write(
      T obj,
      Class<T> interfaceType,
      OutputStream out)
      throws SerializationError, IOException;

  /**
   * Serializes the given object to a serial form with the given interface type.
   */
  <T extends Serializable> String serialize(T obj, Class<T> interfaceType)
    throws SerializationError;

  /**
   * Deserializes the given string, returning an object of the type specified in the serial form.
   */
  Object deserialize(String serialized) throws DeserializationException;

  /**
   * Deserializes the given string, returning an object of the given type. If the type specified in the serial form does
   * not match the given type, a serialization exception is thrown.
   */
  <T> T deserialize(String serialized, Class<T> expectedType) throws DeserializationException;

  Object deserialize(JSONObject json) throws DeserializationException;

  <T> T deserialize(Object object, Class<T> expectedType) throws DeserializationException;

  Object deserialize(InputStream stream)
      throws DeserializationException, IOException;

  Object deserialize(Reader reader) throws DeserializationException, IOException;

  <T> T deserialize(
      InputStream stream,
      Class<T> expectedType)
      throws DeserializationException, IOException;

  <T> T deserialize(Reader reader, Class<T> expectedType)
      throws DeserializationException, IOException;

//  @SuppressWarnings("unchecked")
//  <T extends Serializable> void register(Class<T> clazz);
}
