package com.moozvine.detox;

import com.moozvine.detox.repackaged.org.json.JSONArray;
import com.moozvine.detox.repackaged.org.json.JSONException;
import com.moozvine.detox.repackaged.org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class SerializableUtil {
  private SerializableUtil(){}

  public static void verifyUnchanged(JSONObject json, String fieldName, String expected) throws SerializationError {
    if (!requiredStringField(json, fieldName).equals(expected)) {
      throw new SerializationError("Value of field " + fieldName + " cannot be changed.");
    }
  }

  public static void verifyUnchanged(JSONObject json, String fieldName, long expected) throws SerializationError {
    if (requiredLongField(json, fieldName) != expected) {
      throw new SerializationError("Value of field " + fieldName + " cannot be changed.");
    }
  }

  public static void verifyUnchanged(JSONObject json, String fieldName, boolean expected) throws SerializationError {
    if (requiredBooleanField(json, fieldName) != expected) {
      throw new SerializationError("Value of field " + fieldName + " cannot be changed.");
    }
  }

  public static void verifyUnchanged(JSONObject json, String fieldName, double expected) throws SerializationError {
    if (requiredDoubleField(json, fieldName) != expected) {
      throw new SerializationError("Value of field " + fieldName + " cannot be changed.");
    }
  }

  public static void verifyUnchanged(JSONObject json, String fieldName, int expected) throws SerializationError {
    if (requiredIntField(json, fieldName) != expected) {
      throw new SerializationError("Value of field " + fieldName + " cannot be changed.");
    }
  }

  public static String requiredStringField(JSONObject json, String fieldName) throws SerializationError {
    try {
      return json.getString(fieldName);
    } catch (JSONException e) {
      throw new SerializationError(e);
    }
  }

  public static long requiredLongField(JSONObject json, String fieldName) throws SerializationError {
    try {
      return json.getLong(fieldName);
    } catch (JSONException e) {
      throw new SerializationError(e);
    }
  }

  public static boolean requiredBooleanField(JSONObject json, String fieldName) throws SerializationError {
    try {
      return json.getBoolean(fieldName);
    } catch (JSONException e) {
      throw new SerializationError(e);
    }
  }

  public static double requiredDoubleField(JSONObject json, String fieldName) throws SerializationError {
    try {
      return json.getDouble(fieldName);
    } catch (JSONException e) {
      throw new SerializationError(e);
    }
  }

  public static int requiredIntField(JSONObject json, String fieldName) throws SerializationError {
    try {
      return json.getInt(fieldName);
    } catch (JSONException e) {
      throw new SerializationError(e);
    }
  }

  public static JSONObject requiredObjectField(JSONObject json, String fieldName) throws SerializationError {
    try {
      return json.getJSONObject(fieldName);
    } catch (JSONException e) {
      throw new SerializationError(e);
    }
  }

  public static JSONArray requiredArrayField(JSONObject json, String fieldName) throws SerializationError {
    try {
      return json.getJSONArray(fieldName);
    } catch (JSONException e) {
      throw new SerializationError(e);
    }
  }

  public static <T> List<T> requiredArrayFieldAsList(JSONObject json, String fieldName) throws SerializationError {
    try {
      return asList(json.getJSONArray(fieldName));
    } catch (JSONException e) {
      throw new SerializationError(e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> List<T> asList(JSONArray array) throws SerializationError {
    try {
      final List<T> result = new ArrayList<>();
      for (int i = 0; i < array.length(); ++i) {
        result.add((T) array.get(i));
      }
      return result;
    } catch (JSONException e) {
      throw new SerializationError(e);
    }
  }
}
