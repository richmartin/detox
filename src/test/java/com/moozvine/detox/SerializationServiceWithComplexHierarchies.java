package com.moozvine.detox;

import com.google.common.collect.ImmutableList;
import com.moozvine.detox.testtypes.AnotherSimpleType;
import com.moozvine.detox.testtypes.SimpleType;
import com.moozvine.detox.testtypes.complexhierarchies.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4.class)
public class SerializationServiceWithComplexHierarchies {
  private SerializationService service;

  @Before
  public void registerClasses() {
    service = new AbstractSerializationService() {
      {
        setPrettyPrint();
      }
    };
  }

  @Test
  public void simpleSerializeDeserialize() throws Exception {
    final FooUser toSerialize = FooUserBuilder.newBuilder()
        .withToken(FooTokenBuilder.newBuilder()
            .withValue("a token")
            .withFooCount(2)
            .build())
        .build();

    final String serialized = service.serialize(toSerialize, FooUser.class);
    final FooUser deserialized = (FooUser) service.deserialize(serialized);

    assertEquals(toSerialize.getToken().getFooCount(), deserialized.getToken().getFooCount());
    assertEquals(toSerialize.getToken().getValue(), deserialized.getToken().getValue());
  }

  @Test
  public void withOnlySuperclassPreviouslyRegistered() throws Exception {
    final User userToSerialize = UserBuilder.newBuilder()
        .withToken(TokenBuilder.newBuilder()
            .withValue("user value")
            .build())
        .build();

    final String serializedUser = service.serialize(userToSerialize);
    final User deserializedUser = (User) service.deserialize(serializedUser);

    assertEquals(userToSerialize.getToken().getValue(), deserializedUser.getToken().getValue());

    final FooUser fooUserToSerialize = FooUserBuilder.newBuilder()
        .withToken(FooTokenBuilder.newBuilder()
            .withValue("foo user value")
            .withFooCount(2)
            .build())
        .build();

    final String serializedFooUser = service.serialize(fooUserToSerialize, FooUser.class);
    assertThat(serializedFooUser, containsString(FooUser.class.getName()));

    final FooUser deserializedFooUser = (FooUser) service.deserialize(serializedFooUser);

    assertEquals(fooUserToSerialize.getToken().getFooCount(), deserializedFooUser.getToken().getFooCount());
    assertEquals(fooUserToSerialize.getToken().getValue(), deserializedFooUser.getToken().getValue());
  }

  /**
   * Compare with {@link FailureTests#serializingAnObjectWithMultipleGenerateDTOInterfacesToUnspecifiedInterface()}
   */
  @Test
  public void serializingAnObjectWithMultipleGenerateDTOInterfacesToSpecifiedInterface() throws Exception {
    class MultipleGenerateDTOs implements SimpleType, AnotherSimpleType {

      @Override public String getSomeString() {
        return "some string";
      }

      @Override public int getAnInt() {
        return 2;
      }

      @Override public String getSomeOtherString() {
        return "some other string";
      }
    }

    final String serialized = service.serialize(new MultipleGenerateDTOs(), AnotherSimpleType.class);
    assertThat(serialized, containsString(AnotherSimpleType.class.getName()));

    final AnotherSimpleType deserialized = (AnotherSimpleType) service.deserialize(serialized);
    assertEquals("some other string", deserialized.getSomeOtherString());
  }

  @Test
  public void privateDataShouldNotLeakOnPublicResponses() throws Exception {
    final PrivateUser user = PrivateUserBuilder.newBuilder()
        .withToken(TokenBuilder.newBuilder().withValue("token").build())
        .withPrivateInformation("TOP SECRET")
        .build();

    final PublicResponse response = PublicResponseBuilder.newBuilder()
        .withUser(user)
        .build();

    final String serialized = service.serialize(response);
    assertThat(serialized, not(containsString("TOP SECRET")));
  }

  @Test
  public void shouldSerializeHeterogeneousCollections() throws Exception {
    final GeneratableSubtypeA a = GeneratableSubtypeABuilder.newBuilder()
        .withX("X1")
        .withA("a")
        .build();
    final GeneratableSubtypeB b = GeneratableSubtypeBBuilder.newBuilder()
        .withX("X2")
        .withB("b")
        .build();
    final HasHeterogeneousCollection toSerialize = HasHeterogeneousCollectionBuilder.newBuilder()
        .withCollection(ImmutableList.of(a, b))
        .build();

    final String serialized = service.serialize(toSerialize);
    final HasHeterogeneousCollection deserialized = (HasHeterogeneousCollection) service.deserialize(serialized);

    assertEquals("X1", deserialized.getCollection().get(0).getX());
    assertEquals("a", ((GeneratableSubtypeA) deserialized.getCollection().get(0)).getA());
    assertEquals("X2", deserialized.getCollection().get(1).getX());
    assertEquals("b", ((GeneratableSubtypeB) deserialized.getCollection().get(1)).getB());

  }

  @Test
  public void shouldSerializeToCorrectTypeWhenBoundChanges() throws Exception {
    final PrivateUser user = PrivateUserBuilder.newBuilder()
        .withToken(TokenBuilder.newBuilder().withValue("token").build())
        .withPrivateInformation("TOP SECRET")
        .build();

    final String serialized1 = service.serialize(user, PrivateUser.class);
    assertThat(serialized1, containsString(PrivateUser.class.getName()));
    assertThat(serialized1, containsString("TOP SECRET"));

    final String serialized2 = service.serialize(user, User.class);
    assertThat(serialized2, containsString(User.class.getName()));
    assertThat(serialized2, not(containsString("TOP SECRET")));
  }
}
