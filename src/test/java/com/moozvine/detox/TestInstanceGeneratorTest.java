package com.moozvine.detox;

import com.google.common.base.Function;
import com.moozvine.detox.testtypes.SimpleType;
import com.moozvine.detox.testtypes.TypeWithOptionalFields;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Date;

import static com.moozvine.detox.JsonTestUtil.assertJsonEquivalence;

public class TestInstanceGeneratorTest {
  private final TestInstanceGenerator generator = new TestInstanceGenerator();
  private final SerializationService serializationService = new AbstractSerializationService() {
    {
      registerSerializer(new StringSerializer<Date>(Date.class) {
        @Override public Date fromString(final String value) throws DeserializationException {
          return new Date(Long.parseLong(value));
        }

        @Override public String toJson(final Date value) {
          return String.valueOf(value.getTime());
        }
      });
    }
  };

  @Test
  public void simpleTest() throws Exception {
    final SimpleType simpleType = generator.generate(SimpleType.class);
    assertJsonEquivalence(
        "{\"serializedType\":\"com.moozvine.detox.testtypes.SimpleType\",\"someString\":\"some string\",\"anInt\":1234}",
        serializationService.serialize(simpleType));
  }

  @Test
  public void withAddedValueGeneratorForClass() throws Exception {
    generator.setValueGeneratorForClass(Date.class, new Function<String, Date>() {
      @Nullable @Override public Date apply(final String input) {
        return new Date(0);
      }
    });

    final TypeWithOptionalFields object = generator.generate(TypeWithOptionalFields.class);
    assertJsonEquivalence(
        "{\"serializedType\":\"com.moozvine.detox.testtypes.TypeWithOptionalFields\",\"secondOptionalField\":123.45,\"firstOptionalField\":123,\"secondRequiredField\":\"0\",\"firstRequiredField\":\"some string\"}",
        serializationService.serialize(object));
  }

  @Test
  public void withOverriddenValueGeneratorForField() throws Exception {
    generator.setValueGeneratorForField(String.class, "someString", new Function<String, String>() {
      @Nullable @Override public String apply(@Nullable final String input) {
        return "overridden";
      }
    });
    final SimpleType simpleType = generator.generate(SimpleType.class);
    assertJsonEquivalence(
        "{\"serializedType\":\"com.moozvine.detox.testtypes.SimpleType\",\"someString\":\"overridden\",\"anInt\":1234}",
        serializationService.serialize(simpleType));
  }

}
