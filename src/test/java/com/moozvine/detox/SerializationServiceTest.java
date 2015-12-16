package com.moozvine.detox;

import com.moozvine.detox.testtypes.*;
import com.moozvine.detox.repackaged.org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(JUnit4.class)
public class SerializationServiceTest {
  private SerializationService service;

  @Before
  public void registerClasses() {
    service = new AbstractSerializationService() {
      {
//        register(CollaboratorSubclass1.class);
//        register(CollaboratorSubclass2.class);
//        register(SubclassSerializable.class);
        registerSerializer(new StringSerializer<Date>(Date.class) {
          @Override
          public Class<Date> getTargetType() {
            return Date.class;
          }

          @Override
          public Date fromString(final String value) throws DeserializationException {
            return new Date(Long.parseLong(value));
          }

          @Override
          public String toJson(final Date value) {
            return String.valueOf(value.getTime());
          }
        });
        setPrettyPrint();
      }
    };
  }

  @Test
  public void simpleSerializeDeserialize() throws Exception {
    final CollaboratorSubclass1Impl collaborator = new CollaboratorSubclass1Impl()
        .setAChildMember("collaborator member");

    final List<CollaboratorSerializable> childList = new ArrayList<>();
    childList.add(new CollaboratorSubclass1Impl().setAChildMember("list member 1"));
    childList.add(new CollaboratorSubclass2Impl());

    final SubclassSerializableImpl subclassSerializable = new SubclassSerializableImpl()
        .setABoolean(true)
        .setAByte((byte) 124)
        .setAChar('x')
        .setADouble(1234d)
        .setAFloat(1.3f)
        .setALong(1378L)
        .setAnInt(255)
        .setAShort((short) 127)
        .setAString("some string")
        .setACollaborator(collaborator)
        .setChildList(childList)
        .setASubclassMember("subclass member");

    final String serialForm = service.serialize(subclassSerializable);
    final SubclassSerializable deserialized = (SubclassSerializable) service.deserialize(serialForm);
    final String secondSerialForm = service.serialize(deserialized);
    assertEquals(serialForm, secondSerialForm);
    assertEquals(subclassSerializable, deserialized);
  }

  @Test
  public void serializedNullListShouldAppearAsEmptyArray() throws Exception {
    final String serialized = service.serialize(new SerializableWithListImpl(null));
    final JSONObject obj = new JSONObject(serialized);
    assertEquals(0, obj.getJSONArray("someStrings").length());
  }

  @Test
  public void deserializingObjectWithMissingNullableFieldShouldSucceed() throws Exception {
    final TypeWithOptionalFields deserialized = service.deserialize(
        "{" +
            "serializedType: 'com.moozvine.detox.testtypes.TypeWithOptionalFields'," +
            "firstRequiredField: 'some value'," +
            "secondRequiredField: '1'" +
            "}", TypeWithOptionalFields.class);
    assertEquals("some value", deserialized.getFirstRequiredField());
    assertEquals(new Date(1L), deserialized.getSecondRequiredField());
    assertNull(deserialized.getFirstOptionalField());
    assertNull(deserialized.getSecondOptionalField());
  }

  @Test
  public void serializeObjectWithCustomFieldNames() throws Exception {
    final String serialized = service.serialize(TypeWithCustomFieldNamesBuilder.newBuilder()
        .withFirstRequiredField("one")
        .withSecondRequiredField(new Date(456L))
        .withFirstOptionalField(123)
        .withSecondOptionalField(123.4F)
        .build());
    final JSONObject obj = new JSONObject(serialized);
    assertEquals("one", obj.getString("required_field_1"));
    assertEquals("456", obj.getString("required_field_2"));
    assertEquals(123, obj.getInt("optional_field_1"));
    assertEquals(123.4, obj.getDouble("optional_field_2"), 0.1);
  }

  @Test
  public void deserializeObjectWithCustomFieldNames() throws Exception {
    final TypeWithCustomFieldNames deserialized = service.deserialize(
        "{" +
            "serializedType: 'com.moozvine.detox.testtypes.TypeWithCustomFieldNames'," +
            "required_field_1: 'some value'," +
            "required_field_2: '1'," +
            "optional_field_1: 123," +
            "optional_field_2: 123.4," +
            "}", TypeWithCustomFieldNames.class);
    assertEquals("some value", deserialized.getFirstRequiredField());
    assertEquals(new Date(1L), deserialized.getSecondRequiredField());
    assertEquals(123L, (long) deserialized.getFirstOptionalField());
    assertEquals(123.4, (double) deserialized.getSecondOptionalField(), 0.1);
  }
}
