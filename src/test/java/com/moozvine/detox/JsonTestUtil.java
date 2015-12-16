package com.moozvine.detox;

import com.moozvine.detox.repackaged.org.json.JSONArray;
import com.moozvine.detox.repackaged.org.json.JSONObject;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Created by rich on 09/11/15.
 */
public class JsonTestUtil {

  public static void assertJsonEquivalence(final String left, final String right) {
    final JSONObject leftObj = new JSONObject(left);
    final JSONObject rightObj = new JSONObject(right);
    assertJsonEquivalence(leftObj, rightObj, null);
  }

  public static void assertJsonEquivalence(
      final JSONObject leftObj,
      final JSONObject rightObj) {
    assertSuperset(leftObj, rightObj, null);
    assertSuperset(rightObj, leftObj, null);
  }

  public static void assertJsonEquivalence(
      final JSONArray leftObj,
      final JSONArray rightObj) {
    assertSuperset(leftObj, rightObj, null);
    assertSuperset(rightObj, leftObj, null);
  }

  private static void assertJsonEquivalence(
      final JSONObject leftObj,
      final JSONObject rightObj,
      @Nullable final String pathPrefix) {
    assertSuperset(leftObj, rightObj, pathPrefix);
    assertSuperset(rightObj, leftObj, pathPrefix);
  }

  private static void assertJsonEquivalence(
      final JSONArray leftObj,
      final JSONArray rightObj,
      @Nullable final String pathPrefix) {
    assertSuperset(leftObj, rightObj, pathPrefix);
    assertSuperset(rightObj, leftObj, pathPrefix);
  }

  public static void assertSuperset(
      final JSONObject superset,
      final JSONObject subset,
      @Nullable final String pathPrefix) {
    for (final String key : ((Set<String>)subset.keySet())) {
      final Object value = subset.get(key);
      if (!superset.has(key)) {
        throw new AssertionError(String.format("Missing key '%s'%s. Expected '%s' on JSONObject:\n%s",
            key,
            pathPrefix == null ? "" : " at path " + pathPrefix,
            key,
            superset));
      }
      if (!value.getClass().equals(superset.get(key).getClass())) {
        throw new AssertionError(String.format(
            "Key '%s'%s has unexpected type. Expected %s but was %s on JSONObject:\n%s",
            key,
            pathPrefix == null ? "" : " at path " + pathPrefix,
            value.getClass(),
            superset.get(key).getClass(),
            superset));
      }
      if (value instanceof JSONObject) {
        assertJsonEquivalence(
            (JSONObject) value, superset.getJSONObject(key), pathPrefix == null ? key : pathPrefix + "." + key);
      } else if (value instanceof JSONArray) {
        assertJsonEquivalence(
            (JSONArray) value, superset.getJSONArray(key), pathPrefix == null ? key : pathPrefix + "." + key);
      } else if (!value.equals(superset.get(key))) {
        throw new AssertionError(String.format(
            "Key '%s'%s has unexpected value. Expected '%s' but was '%s' on JSONObject:\n%s",
            key,
            pathPrefix == null ? "" : " at path " + pathPrefix,
            value,
            superset.get(key),
            superset));
      }
    }
  }

  public static void assertSuperset(
      final JSONArray superset,
      final JSONArray subset,
      @Nullable final String pathPrefix) {
    if (subset.length() > superset.length()) {
      throw new AssertionError(String.format(
          "JSONArray%s is shorter than expected. Expected length %s but was %s.",
          pathPrefix == null ? "" : " at path " + pathPrefix,
          subset.length(),
          superset.length()));
    }
    for (int i = 0; i < subset.length(); ++i) {
      final Object value = subset.get(i);
      if (value.getClass().equals(superset.get(i).getClass())) {
        throw new AssertionError(String.format(
            "Value of index %s of JSONArray%s has unexpected type. Expected %s but was %s on JSONObject:\n%s",
            i,
            pathPrefix == null ? "" : " at path " + pathPrefix,
            value.getClass(),
            superset.get(i).getClass(),
            superset));
      }
      if (value instanceof JSONObject) {
        assertJsonEquivalence(
            (JSONObject) value, superset.getJSONObject(i),
            pathPrefix == null ? "[" + i + "]" : pathPrefix + "[" + i + "]");
      } else if (value instanceof JSONArray) {
        assertJsonEquivalence(
            (JSONArray) value, superset.getJSONArray(i),
            pathPrefix == null ? "[" + i + "]" : pathPrefix + "[" + i + "]");
      } else if (!value.equals(superset.get(i))) {
        throw new AssertionError(String.format(
            "Index %s of JSONArray%s has unexpected value. Expected '%s' but was '%s' on JSONObject:\n%s",
            i,
            pathPrefix == null ? "" : " at path " + pathPrefix,
            value,
            superset.get(i),
            superset));
      }
    }
  }

}
