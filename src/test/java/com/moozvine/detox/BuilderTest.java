package com.moozvine.detox;

import com.moozvine.detox.testtypes.*;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Date;

import static org.junit.Assert.*;

public class BuilderTest {
  @Rule public ExpectedException expected = ExpectedException.none();

  @Test
  public void simpleTypeBuilder() {
    final SimpleType value = SimpleTypeBuilder.newBuilder()
        .withSomeString("one")
        .withAnInt(2)
        .build();

    assertEquals("one", value.getSomeString());
    assertEquals(2, value.getAnInt());
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void shouldThrowWhenNullPassedToNonNullField() {
    expected.expect(IllegalArgumentException.class);
    expected.expectMessage(CoreMatchers.containsString("SimpleType.someString"));
    SimpleTypeBuilder.newBuilder()
        .withSomeString(null)
        .withAnInt(2)
        .build();
    fail();
  }

  @Test
  public void validatedSimpleTypeShouldPassValidation() {
    final ValidatedSimpleType value = ValidatedSimpleTypeBuilder.newBuilder()
        .withUpperCaseString("ALL UPPER CASE")
        .withSomethingOverTwenty(21)
        .build();

    assertEquals("ALL UPPER CASE", value.getUpperCaseString());
  }

  @Test
  public void validatedSimpleTypeShouldFailInstanceValidation() {
    expected.expect(IllegalStateException.class);
    ValidatedSimpleTypeBuilder.newBuilder()
        .withUpperCaseString("NOT all UPPER CASE")
        .withSomethingOverTwenty(21)
        .build();
  }

  @Test
  public void validatedSimpleTypeShouldFailFieldValidation() {
    expected.expect(IllegalArgumentException.class);
    ValidatedSimpleTypeBuilder.newBuilder()
        .withUpperCaseString("NOT all UPPER CASE")
        .withSomethingOverTwenty(19)
        .build();
  }

  @Test
  public void shouldBuildTypeThatOmitsOptionalFields() {
    final TypeWithOptionalFields value = TypeWithOptionalFieldsBuilder.newBuilder()
        .withFirstRequiredField("Something")
        .withSecondRequiredField(new Date(123L))
        .build();

    assertEquals("Something", value.getFirstRequiredField());
    assertEquals(new Date(123L), value.getSecondRequiredField());
    assertNull(value.getFirstOptionalField());
    assertNull(value.getSecondOptionalField());
  }

  @Test
  public void typesWithFormsIdFieldsShouldBeEqual() {

    final SimpleTypeWithAnId value1 = SimpleTypeWithAnIdBuilder.newBuilder()
        .withIntegerId(2)
        .withStringId("string")
        .withNotId("something")
        .build();

    final SimpleTypeWithAnId value2 = SimpleTypeWithAnIdBuilder.newBuilder()
        .withIntegerId(2)
        .withStringId("string")
        .withNotId("something else")
        .build();

    assertEquals(value1, value2);
    assertEquals(value1.hashCode(), value2.hashCode());

  }

  @Test
  public void nonSerializableTypeShouldBeBuildable() {
    final NonSerializableType value = NonSerializableTypeBuilder.newBuilder()
        .withAString("something")
        .build();

    assertEquals("something", value.getAString());
  }
}
