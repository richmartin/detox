package com.moozvine.detox;

import com.moozvine.detox.testtypes.SimpleType;
import com.moozvine.detox.testtypes.SimpleTypeBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class BuilderCopyConstructorTest {
  @Rule public ExpectedException expected = ExpectedException.none();

  @Test
  public void simpleTypeCopyModifyingFirstField() {
    final SimpleType toCopy = SimpleTypeBuilder.newBuilder()
        .withSomeString("one")
        .withAnInt(2)
        .build();

    final SimpleType value = SimpleTypeBuilder.copyOf(toCopy)
        .withSomeString("two")
        .build();

    assertEquals("two", value.getSomeString());
    assertEquals(2, value.getAnInt());
  }

  @Test
  public void simpleTypeCopyModifyingLastField() {
    final SimpleType toCopy = SimpleTypeBuilder.newBuilder()
        .withSomeString("one")
        .withAnInt(2)
        .build();

    final SimpleType value = SimpleTypeBuilder.copyOf(toCopy)
        .withAnInt(3)
        .build();

    assertEquals("one", value.getSomeString());
    assertEquals(3, value.getAnInt());
  }
}
