package com.moozvine.detox;

import com.moozvine.detox.testtypes.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FailureTests {
  @Rule public ExpectedException expected = ExpectedException.none();

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
  public void serializingANonGenerateDTOObject() throws Exception {
    final SerializableWithoutGenerate toSerialize = new SerializableWithoutGenerate() {
      @Override public String getValue() {
        return "a value";
      }
    };

    expected.expect(SerializationError.class);
    expected.expectMessage("No @GenerateDTO");
    service.serialize(toSerialize);
  }

  /**
   * Compare with {@link SerializationServiceWithComplexHierarchies#serializingAnObjectWithMultipleGenerateDTOInterfacesToSpecifiedInterface()}
   */
  @Test
  public void serializingAnObjectWithMultipleGenerateDTOInterfacesToUnspecifiedInterface() throws Exception {
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

    expected.expect(SerializationError.class);
    expected.expectMessage("Multiple @GenerateDTO");
    service.serialize(new MultipleGenerateDTOs());
  }
}
