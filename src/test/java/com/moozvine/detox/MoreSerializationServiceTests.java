package com.moozvine.detox;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.moozvine.detox.testtypes.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class MoreSerializationServiceTests {

  private SerializationService service;

  @Before
  public void registerClasses() {
    service = new AbstractSerializationService() {
      {
        registerSerializer(new StringSerializer<InternetAddress>(InternetAddress.class) {
          @Override
          public Class<InternetAddress> getTargetType() {
            return InternetAddress.class;
          }

          @Override
          public InternetAddress fromString(final String value) throws DeserializationException {
            try {
              return new InternetAddress(value);
            } catch (final AddressException e) {
              throw new DeserializationException(e);
            }
          }

          @Override
          public String toJson(final InternetAddress value) {
            return String.valueOf(value);
          }
        });
        setPrettyPrint();
      }
    };
  }

  @Test
  public void shouldSerializeWithIntegerMember() throws Exception {
    final Types.SerializableWithIntegerMember from
        = Types$SerializableWithIntegerMemberBuilder.newBuilder()
        .withAnInt(123)
        .build();

    final String asString = service.serialize(from);
    Assert.assertTrue(asString.contains("serializedType"));
    Assert.assertTrue(asString.contains(Types.SerializableWithIntegerMember.class.getName()));

    final Types.SerializableWithIntegerMember to = service.deserialize(asString, Types.SerializableWithIntegerMember.class);
    assertEquals(from.getAnInt(), to.getAnInt());
  }

  @Test
  public void shouldSerializeWithLongMember() throws Exception {
    final Types.SerializableWithLongMember from
        = Types$SerializableWithLongMemberBuilder.newBuilder()
        .withALong(123L)
        .build();

    final String asString = service.serialize(from);

    final Types.SerializableWithLongMember to = service.deserialize(asString, Types.SerializableWithLongMember.class);
    assertEquals(from.getALong(), to.getALong());
  }

  @Test
  public void shouldSerializeWithFloatMember() throws Exception {
    final Types.SerializableWithFloatMember from
        = Types$SerializableWithFloatMemberBuilder.newBuilder()
        .withAFloat(12.3F)
        .build();

    final String asString = service.serialize(from);

    final Types.SerializableWithFloatMember to = service.deserialize(asString, Types.SerializableWithFloatMember.class);
    assertEquals(from.getAFloat(), to.getAFloat(), 0.1F);
  }

  @Test
  public void shouldSerializeWithDoubleMember() throws Exception {
    final Types.SerializableWithDoubleMember from
        = Types$SerializableWithDoubleMemberBuilder.newBuilder()
        .withADouble(12.3D)
        .build();

    final String asString = service.serialize(from);

    final Types.SerializableWithDoubleMember to = service.deserialize(asString, Types.SerializableWithDoubleMember.class);
    assertEquals(from.getADouble(), to.getADouble(), 0.1F);
  }

  @Test
  public void shouldSerializeWithBigDecimalMember() throws Exception {
    final Types.SerializableWithBigDecimalMember from
        = Types$SerializableWithBigDecimalMemberBuilder.newBuilder()
        .withABigDecimal(new BigDecimal("12.5"))
        .build();

    final String asString = service.serialize(from);

    final Types.SerializableWithBigDecimalMember to = service.deserialize(asString, Types.SerializableWithBigDecimalMember.class);
    assertEquals(from.getABigDecimal(), to.getABigDecimal());
  }

  @Test
  public void shouldSerializeWithBigIntegerMember() throws Exception {
    final Types.SerializableWithBigIntegerMember from
        = Types$SerializableWithBigIntegerMemberBuilder.newBuilder()
        .withABigInteger(new BigInteger("126"))
        .build();

    final String asString = service.serialize(from);

    final Types.SerializableWithBigIntegerMember to = service.deserialize(asString, Types.SerializableWithBigIntegerMember.class);
    assertEquals(from.getABigInteger(), to.getABigInteger());
  }

  @Test
  public void shouldSerializeWithStringMember() throws Exception {
    final Types.SerializableWithStringMember from
        = Types$SerializableWithStringMemberBuilder.newBuilder()
        .withAString("a string")
        .build();

    final String asString = service.serialize(from);

    final Types.SerializableWithStringMember to = service.deserialize(asString, Types.SerializableWithStringMember.class);
    assertEquals(from.getAString(), to.getAString());
  }

  @Test
  public void shouldSerializeWithSerializableMember() throws Exception {
    final Types.SerializableWithSerializableMember from
        = Types$SerializableWithSerializableMemberBuilder.newBuilder()
        .withMember(Types$SerializableWithStringMemberBuilder.newBuilder()
            .withAString("a string")
            .build())
        .build();

    final String asString = service.serialize(from);

    final Types.SerializableWithSerializableMember to = service.deserialize(asString, Types.SerializableWithSerializableMember.class);
    assertEquals(from.getMember().getAString(), to.getMember().getAString());
  }

  @Test
  public void shouldSerializeWithSerializableMemberList() throws Exception {
    final Types.SerializableWithSerializableMemberList from
        = Types$SerializableWithSerializableMemberListBuilder.newBuilder()
        .withMembers(
            ImmutableList.of(
                Types$SerializableWithStringMemberBuilder.newBuilder()
                    .withAString("a string")
                    .build(),
                Types$SerializableWithStringMemberBuilder.newBuilder()
                    .withAString("a different string")
                    .build()))
        .build();

    final String asString = service.serialize(from);
    final Types.SerializableWithSerializableMemberList to = service.deserialize(asString, Types.SerializableWithSerializableMemberList.class);
    assertEquals(from.getMembers().get(0).getAString(), to.getMembers().get(0).getAString());
    assertEquals(from.getMembers().get(1).getAString(), to.getMembers().get(1).getAString());
    assertEquals(asString, service.serialize(to));
  }

  @Test
  public void shouldSerializeWithCustomMember() throws Exception {
    final Types.SerializableWithCustomMember from
        = Types$SerializableWithCustomMemberBuilder.newBuilder()
        .withInternetAddress(new InternetAddress("rich@moozvine.com"))
        .build();

    final String asString = service.serialize(from);

    final Types.SerializableWithCustomMember to = service.deserialize(asString, Types.SerializableWithCustomMember.class);
    assertEquals(from.getInternetAddress(), to.getInternetAddress());
  }

  @Test
  public void shouldSerializeWithCustomMemberList() throws Exception {
    final Types.SerializableWithCustomMemberList from
        = Types$SerializableWithCustomMemberListBuilder.newBuilder()
        .withInternetAddressList(ImmutableList.of(
            new InternetAddress("first@moozvine.com"),
            new InternetAddress("second@moozvine.com")))
        .build();

    final String asString = service.serialize(from);

    final Types.SerializableWithCustomMemberList to = service.deserialize(asString, Types.SerializableWithCustomMemberList.class);
    assertEquals(2, to.getInternetAddressList().size());
    assertEquals(new InternetAddress("first@moozvine.com"), to.getInternetAddressList().get(0));
    assertEquals(new InternetAddress("second@moozvine.com"), to.getInternetAddressList().get(1));
  }

  @Test
  public void shouldSerializeWithEnumType() throws Exception {
    final Types.SerializableWithEnumType from
        = Types$SerializableWithEnumTypeBuilder.newBuilder()
        .withAnEnum(Types.SerializableWithEnumType.AnEnum.VALUE2)
        .build();

    final String asString = service.serialize(from);

    final Types.SerializableWithEnumType to = service.deserialize(asString, Types.SerializableWithEnumType.class);
    assertEquals(from.getAnEnum(), to.getAnEnum());
  }

  @Test
  public void shouldSerializeWithListOfEnumTypes() throws Exception {
    final Types.SerializableWithListOfEnumTypes from
        = Types$SerializableWithListOfEnumTypesBuilder.newBuilder()
        .withEnums(ImmutableList.of(
            Types.SerializableWithListOfEnumTypes.AnEnum.VALUE2,
            Types.SerializableWithListOfEnumTypes.AnEnum.VALUE1))
        .build();

    final String asString = service.serialize(from);

    final Types.SerializableWithListOfEnumTypes to = service.deserialize(asString, Types.SerializableWithListOfEnumTypes.class);
    assertEquals(from.getEnums(), to.getEnums());
  }

  @Test
  public void shouldSerializeWithStringMapOfStrings() throws Exception {
    final Types.SerializableWithStringMapOfStrings from
        = Types$SerializableWithStringMapOfStringsBuilder.newBuilder()
        .withStringMap(ImmutableMap.of(
            "a", "one",
            "b", "two"
        ))
        .build();

    final String asString = service.serialize(from);

    final Types.SerializableWithStringMapOfStrings to = service.deserialize(asString, Types.SerializableWithStringMapOfStrings.class);
    assertEquals(from.getStringMap(), to.getStringMap());
  }

  @Test
  public void shouldSerializeWithStringMapContainingNulls() throws Exception {
    final Map<String, String> mapWithNullValue = new HashMap<>();
    mapWithNullValue.put("a", null);
    mapWithNullValue.put("b", "two");

    final Types.SerializableWithStringMapOfStrings from
        = Types$SerializableWithStringMapOfStringsBuilder.newBuilder()
        .withStringMap(mapWithNullValue)
        .build();

    final String asString = service.serialize(from);

    final Types.SerializableWithStringMapOfStrings to = service.deserialize(asString, Types.SerializableWithStringMapOfStrings.class);
    assertEquals(from.getStringMap(), to.getStringMap());
  }

  @Test
  public void shouldSerializeWithStringMapOfIntegers() throws Exception {
    final Types.SerializableWithStringMapOfIntegers from
        = Types$SerializableWithStringMapOfIntegersBuilder.newBuilder()
        .withStringMap(ImmutableMap.of(
            "a", 1,
            "b", 2
        ))
        .build();

    final String asString = service.serialize(from);

    final Types.SerializableWithStringMapOfIntegers to = service.deserialize(asString, Types
        .SerializableWithStringMapOfIntegers.class);
    assertEquals(from.getStringMap(), to.getStringMap());
  }

  @Test
  public void shouldSerializeWithStringMapOfSerializables() throws Exception {
    final Types.SerializableWithStringMapOfSerializables from
        = Types$SerializableWithStringMapOfSerializablesBuilder.newBuilder()
        .withStringMap(ImmutableMap.of(
            "a", Types$SerializableWithStringMemberBuilder.newBuilder().withAString("one").build(),
            "b", Types$SerializableWithStringMemberBuilder.newBuilder().withAString("two").build()
        ))
        .build();

    final String asString = service.serialize(from);

    final Types.SerializableWithStringMapOfSerializables to = service.deserialize(asString, Types
        .SerializableWithStringMapOfSerializables.class);
    assertEquals("one", to.getStringMap().get("a").getAString());
    assertEquals("two", to.getStringMap().get("b").getAString());
  }

  @Test
  public void shouldSerializeWithStringMapOfCustomMembers() throws Exception {
    final Types.SerializableWithStringMapOfInternetAddresses from
        = Types$SerializableWithStringMapOfInternetAddressesBuilder.newBuilder()
        .withStringMap(ImmutableMap.of(
            "a", new InternetAddress("a@moozvine.com"),
            "b", new InternetAddress("b@moozvine.com")
        ))
        .build();

    final String asString = service.serialize(from);

    final Types.SerializableWithStringMapOfInternetAddresses to = service.deserialize(asString, Types
        .SerializableWithStringMapOfInternetAddresses.class);
    assertEquals("a@moozvine.com", to.getStringMap().get("a").getAddress());
    assertEquals("b@moozvine.com", to.getStringMap().get("b").getAddress());
  }

  @Test
  public void shouldSerializeWithEnumMapOfStrings() throws Exception {
    final Types.SerializableWithEnumMapOfStrings from
        = Types$SerializableWithEnumMapOfStringsBuilder.newBuilder()
        .withEnumMap(ImmutableMap.of(
            AnEnum.ONE, "one",
            AnEnum.TWO, "two"
        ))
        .build();

    final String asString = service.serialize(from);

    final Types.SerializableWithEnumMapOfStrings to = service.deserialize(asString, Types.SerializableWithEnumMapOfStrings.class);
    assertEquals(from.getEnumMap(), to.getEnumMap());
  }

  /*
  @Test
  public void shouldSerializeMapOfStringsToLists() throws Exception {
    final Types.SerializableWithStringMapOfLists from
        = Types$SerializableWithStringMapOfLists.newBuilder()
        .withStringMap(ImmutableMap.of(
            "one", ImmutableList.of(
                Types$SerializableWithStringMemberBuilder.newBuilder().withAString("one.1").build(),
                Types$SerializableWithStringMemberBuilder.newBuilder().withAString("one.2").build()),
            "two", ImmutableList.of(
                Types$SerializableWithStringMemberBuilder.newBuilder().withAString("two.1").build(),
                Types$SerializableWithStringMemberBuilder.newBuilder().withAString("two.2").build())
            ))
        .build();

    final String asString = service.serialize(from);

    final Types.SerializableWithStringMapOfLists to = service.deserialize(asString, Types
        .SerializableWithStringMapOfLists.class);
    assertEquals(from.getStringMap().get("one"), to.getStringMap().get("one"));
    assertEquals(from.getStringMap().get("two"), to.getStringMap().get("two"));
  }
*/
  @Test
  public void shouldSerializeAClassWithASetOfEnums() throws Exception {
    final ClassWithASetOfEnums toSerialize = ClassWithASetOfEnumsBuilder.newBuilder()
        .withSomeEnums(EnumSet.of(AnEnum.THREE, AnEnum.TWO))
        .build();

    final String asString = service.serialize(toSerialize);

    final ClassWithASetOfEnums deserialized = service.deserialize(asString, ClassWithASetOfEnums.class);
    assertEquals(toSerialize.getSomeEnums(), deserialized.getSomeEnums());
  }
}
