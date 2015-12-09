package com.moozvine.detox;

import com.google.common.collect.ImmutableSet;
import com.moozvine.detox.testtypes.*;
import org.junit.Test;

import java.util.Date;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class BuildlerToStringTest {
  @Test
  public void simpleTypeShouldRenderToString() {
    final SimpleType built = SimpleTypeBuilder.newBuilder()
        .withSomeString("one")
        .withAnInt(2)
        .build();

    final String expected = "{\n" +
        "  \"serializedType\": \"com.moozvine.detox.testtypes.SimpleType\",\n" +
        "  \"someString\": \"one\",\n" +
        "  \"anInt\": 2\n" +
        "}";
    assertEquivalent(expected, built.toString());
  }

  @Test
  public void copiedTypeShouldRenderToString() {
    final SimpleType toCopy = SimpleTypeBuilder.newBuilder()
        .withSomeString("one")
        .withAnInt(2)
        .build();

    final SimpleType copied = SimpleTypeBuilder.copyOf(toCopy).build();

    final String expected = "{\n" +
        "  \"serializedType\": \"com.moozvine.detox.testtypes.SimpleType\",\n" +
        "  \"someString\": \"one\",\n" +
        "  \"anInt\": 2\n" +
        "}";
    assertEquivalent(expected, copied.toString());
  }

  @Test
  public void typeWithIDShouldRenderToString() {
    final SimpleTypeWithAnId built = SimpleTypeWithAnIdBuilder.newBuilder()
        .withIntegerId(2)
        .withStringId("id")
        .withNotId("anything")
        .build();

    final String expected = "{\n" +
        "  \"serializedType\": \"com.moozvine.detox.testtypes.SimpleTypeWithAnId\",\n" +
        "  \"stringId\": \"id\",\n" +
        "  \"integerId\": 2,\n" +
        "  \"notId\": \"anything\"\n" +
        "}";
    assertEquivalent(expected, built.toString());
  }

  @Test
  public void copiedTypeWithIDShouldRenderToString() {
    final SimpleTypeWithAnId toCopy = SimpleTypeWithAnIdBuilder.newBuilder()
        .withIntegerId(2)
        .withStringId("id")
        .withNotId("anything")
        .build();

    final SimpleTypeWithAnId copied = SimpleTypeWithAnIdBuilder.copyOf(toCopy)
        .build();

    final String expected = "{\n" +
        "  \"serializedType\": \"com.moozvine.detox.testtypes.SimpleTypeWithAnId\",\n" +
        "  \"stringId\": \"id\",\n" +
        "  \"integerId\": 2,\n" +
        "  \"notId\": \"anything\"\n" +
        "}";
    assertEquivalent(expected, copied.toString());
  }

  @Test
  public void typeWithUnserializableFieldShouldRenderToString() {
    final TypeWithOptionalFields built = TypeWithOptionalFieldsBuilder.newBuilder()
        .withFirstRequiredField("required1")
        .withSecondRequiredField(new Date(0))
        .build();

    final String expected = "{\n" +
        "  \"serializedType\": \"com.moozvine.detox.testtypes.TypeWithOptionalFields\",\n" +
        "  \"firstRequiredField\": \"required1\",\n" +
        "  \"secondRequiredField\": \"Thu Jan 01 01:00:00 CET 1970\"\n" +
        "}";
    assertEquivalent(expected, built.toString());
  }

  /**
   * Because JSONObject.toString iterates over the Map keys, the order of the members is undefined. We therefore need
   * to compare the strings in an order-independent way. This method is a imperfect first-approximation. It could
   * produce false-positives in some situations, e.g.
   * x: {
   *   y: "one"
   * },
   * z: {
   *   y: "two"
   * }
   *
   * will appear equal to:
   *
   * x: {
   *   y: "two"
   * },
   * z: {
   *   y: "one"
   * }
   *
   */
  private static void assertEquivalent(final String jsonLeft, final String jsonRight) {
    final String leftCanonical = jsonLeft.replaceAll(", *\n", "\n");
    final String rightCanonical = jsonRight.replaceAll(", *\n", "\n");
    final Set<String> linesLeft = ImmutableSet.copyOf(leftCanonical.split("\n"));
    final Set<String> linesRight = ImmutableSet.copyOf(rightCanonical.split("\n"));
    assertEquals(linesLeft, linesRight);
  }
}
