package com.moozvine.detox;

import com.google.common.collect.ImmutableList;
import com.moozvine.detox.testtypes.withids.*;
import org.junit.Test;

import javax.mail.internet.InternetAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class BuilderEqualityTest {
  @Test
  public void typesWithSameStringIdFieldShouldBeEqual() {
    final SimpleTypeWithAStringId value1 = SimpleTypeWithAStringIdBuilder.newBuilder()
        .withStringId("string")
        .withNotId("something")
        .build();

    final SimpleTypeWithAStringId value2 = SimpleTypeWithAStringIdBuilder.newBuilder()
        .withStringId("string")
        .withNotId("something else")
        .build();

    assertEquals(value1, value2);
    assertEquals(value1.hashCode(), value2.hashCode());
  }

  @Test
  public void typesWithSameBooleanIdFieldShouldBeEqual() {
    final SimpleTypeWithABooleanId value1 = SimpleTypeWithABooleanIdBuilder.newBuilder()
        .withBooleanId(true)
        .withNotId("something")
        .build();

    final SimpleTypeWithABooleanId value2 = SimpleTypeWithABooleanIdBuilder.newBuilder()
        .withBooleanId(true)
        .withNotId("something else")
        .build();

    assertEquals(value1, value2);
    assertEquals(value1.hashCode(), value2.hashCode());
  }

  @Test
  public void typesWithSameCollectionIdFieldShouldBeEqual() {
    final SimpleTypeWithACollectionId value1 = SimpleTypeWithACollectionIdBuilder.newBuilder()
        .withCollectionId(ImmutableList.of("string"))
        .withNotId("something")
        .build();

    final SimpleTypeWithACollectionId value2 = SimpleTypeWithACollectionIdBuilder.newBuilder()
        .withCollectionId(ImmutableList.of("string"))
        .withNotId("something else")
        .build();

    assertEquals(value1, value2);
    assertEquals(value1.hashCode(), value2.hashCode());
  }

  @Test
  public void typesWithSameDoubleIdFieldShouldBeEqual() {
    final SimpleTypeWithADoubleId value1 = SimpleTypeWithADoubleIdBuilder.newBuilder()
        .withDoubleId(1.23)
        .withNotId("something")
        .build();

    final SimpleTypeWithADoubleId value2 = SimpleTypeWithADoubleIdBuilder.newBuilder()
        .withDoubleId(1.23)
        .withNotId("something else")
        .build();

    assertEquals(value1, value2);
    assertEquals(value1.hashCode(), value2.hashCode());
  }

  @Test
  public void typesWithSameFloatIdFieldShouldBeEqual() {
    final SimpleTypeWithAFloatId value1 = SimpleTypeWithAFloatIdBuilder.newBuilder()
        .withFloatId(1.23F)
        .withNotId("something")
        .build();

    final SimpleTypeWithAFloatId value2 = SimpleTypeWithAFloatIdBuilder.newBuilder()
        .withFloatId(1.23F)
        .withNotId("something else")
        .build();

    assertEquals(value1, value2);
    assertEquals(value1.hashCode(), value2.hashCode());
  }

  @Test
  public void typesWithSameLongIdFieldShouldBeEqual() {
    final SimpleTypeWithALongId value1 = SimpleTypeWithALongIdBuilder.newBuilder()
        .withLongId(123L)
        .withNotId("something")
        .build();

    final SimpleTypeWithALongId value2 = SimpleTypeWithALongIdBuilder.newBuilder()
        .withLongId(123L)
        .withNotId("something else")
        .build();

    assertEquals(value1, value2);
    assertEquals(value1.hashCode(), value2.hashCode());
  }

  @Test
  public void typesWithSameIntegerIdFieldShouldBeEqual() {
    final SimpleTypeWithAnIntegerId value1 = SimpleTypeWithAnIntegerIdBuilder.newBuilder()
        .withIntegerId(123)
        .withNotId("something")
        .build();

    final SimpleTypeWithAnIntegerId value2 = SimpleTypeWithAnIntegerIdBuilder.newBuilder()
        .withIntegerId(123)
        .withNotId("something else")
        .build();

    assertEquals(value1, value2);
    assertEquals(value1.hashCode(), value2.hashCode());
  }

  @Test
  public void typesWithSameObjectIdFieldShouldBeEqual() throws Exception {
    final SimpleTypeWithAnObjectId value1 = SimpleTypeWithAnObjectIdBuilder.newBuilder()
        .withObjectId(new InternetAddress("foo@bar.baz"))
        .withNotId("something")
        .build();

    final SimpleTypeWithAnObjectId value2 = SimpleTypeWithAnObjectIdBuilder.newBuilder()
        .withObjectId(new InternetAddress("foo@bar.baz"))
        .withNotId("something else")
        .build();

    assertEquals(value1, value2);
    assertEquals(value1.hashCode(), value2.hashCode());
  }

  @Test
  public void typesWithSameCompoundIdFieldsShouldBeEqual() {

    final SimpleTypeWithACompoundId value1 = SimpleTypeWithACompoundIdBuilder.newBuilder()
        .withIntegerId(2)
        .withStringId("string")
        .withNotId("something")
        .build();

    final SimpleTypeWithACompoundId value2 = SimpleTypeWithACompoundIdBuilder.newBuilder()
        .withIntegerId(2)
        .withStringId("string")
        .withNotId("something else")
        .build();

    assertEquals(value1, value2);
    assertEquals(value1.hashCode(), value2.hashCode());
  }

  @Test
  public void typesWithDifferentCompoundIdFieldsShouldBeUnequal() {

    final SimpleTypeWithACompoundId value1 = SimpleTypeWithACompoundIdBuilder.newBuilder()
        .withIntegerId(2)
        .withStringId("string")
        .withNotId("something")
        .build();

    final SimpleTypeWithACompoundId value2 = SimpleTypeWithACompoundIdBuilder.newBuilder()
        .withIntegerId(2)
        .withStringId("different string")
        .withNotId("something else")
        .build();

    assertNotEquals(value1, value2);

    final SimpleTypeWithACompoundId value3 = SimpleTypeWithACompoundIdBuilder.newBuilder()
        .withIntegerId(2)
        .withStringId("string")
        .withNotId("something")
        .build();

    final SimpleTypeWithACompoundId value4 = SimpleTypeWithACompoundIdBuilder.newBuilder()
        .withIntegerId(3)
        .withStringId("string")
        .withNotId("something else")
        .build();

    assertNotEquals(value3, value4);
  }

}
