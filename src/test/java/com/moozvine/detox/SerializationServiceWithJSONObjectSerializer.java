package com.moozvine.detox;

import com.moozvine.detox.testtypes.HasPair;
import com.moozvine.detox.testtypes.HasPairBuilder;
import com.moozvine.detox.testtypes.Pair;
import com.moozvine.detox.repackaged.org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class SerializationServiceWithJSONObjectSerializer {
  private SerializationService service;

  @Before
  public void registerClasses() {
    service = new AbstractSerializationService(){
      {
        registerSerializer(new JSONObjectSerializer<Pair>(Pair.class) {
          @Override
          public Pair fromJSONObject(final JSONObject value) throws DeserializationException {
            return new Pair(value.getString("first"), value.getString("second"));
          }

          @Override
          public JSONObject toJson(final Pair value) {
            final JSONObject result = new JSONObject();
            result.put("first", value.first);
            result.put("second", value.second);
            return result;
          }
        });
        setPrettyPrint();
      }
    };
  }

  @Test
  public void simpleSerializeDeserialize() throws Exception {
    final HasPair toSerialize = HasPairBuilder.newBuilder()
        .withPair(new Pair("a", "b"))
        .build();

    final String serialized = service.serialize(toSerialize);
    final HasPair deserialized = (HasPair) service.deserialize(serialized);
    assertEquals("a", deserialized.getPair().first);
    assertEquals("b", deserialized.getPair().second);
  }
}
