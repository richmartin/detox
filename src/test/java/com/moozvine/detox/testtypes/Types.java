package com.moozvine.detox.testtypes;

import com.moozvine.detox.GenerateDTO;
import com.moozvine.detox.Serializable;

import javax.mail.internet.InternetAddress;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class Types {
  @GenerateDTO
  public interface SerializableWithIntegerMember extends Serializable {
    int getAnInt();
  }

  @GenerateDTO
  public interface SerializableWithLongMember extends Serializable {
    long getALong();
  }

  @GenerateDTO
  public interface SerializableWithFloatMember extends Serializable {
    float getAFloat();
  }

  @GenerateDTO
  public interface SerializableWithDoubleMember extends Serializable {
    double getADouble();
  }

  @GenerateDTO
  public interface SerializableWithBigDecimalMember extends Serializable {
    BigDecimal getABigDecimal();
  }

  @GenerateDTO
  public interface SerializableWithBigIntegerMember extends Serializable {
    BigInteger getABigInteger();
  }

  @GenerateDTO
  public interface SerializableWithStringMember extends Serializable {
    String getAString();
  }

  @GenerateDTO
  public interface SerializableWithSerializableMember extends Serializable {
    SerializableWithStringMember getMember();
  }

  @GenerateDTO
  public interface SerializableWithSerializableMemberList extends Serializable {
    List<SerializableWithStringMember> getMembers();
  }


  @GenerateDTO
  public interface SerializableWithCustomMember extends Serializable {
    InternetAddress getInternetAddress();
  }

  @GenerateDTO
  public interface SerializableWithCustomMemberList extends Serializable {
    List<InternetAddress> getInternetAddressList();
  }

  @GenerateDTO
  public interface SerializableWithEnumType extends Serializable {
    enum AnEnum {VALUE1, VALUE2}

    AnEnum getAnEnum();
  }

  @GenerateDTO
  public interface SerializableWithListOfEnumTypes extends Serializable {
    enum AnEnum {VALUE1, VALUE2}

    List<AnEnum> getEnums();
  }

  @GenerateDTO
  public interface SerializableWithStringMapOfStrings extends Serializable {
    Map<String, String> getStringMap();
  }

  @GenerateDTO
  public interface SerializableWithStringMapOfIntegers extends Serializable {
    Map<String, Integer> getStringMap();
  }

  @GenerateDTO
  public interface SerializableWithStringMapOfSerializables extends Serializable {
    Map<String, SerializableWithStringMember> getStringMap();
  }

  @GenerateDTO
  public interface SerializableWithStringMapOfInternetAddresses extends Serializable {
    Map<String, InternetAddress> getStringMap();
  }

  @GenerateDTO
  public interface SerializableWithEnumMapOfStrings extends Serializable {
    Map<AnEnum, String> getEnumMap();
  }

  /* TODO...
  @GenerateDTO
  public interface SerializableWithStringMapOfLists extends Serializable {
    Map<String, List<SerializableWithStringMember>> getStringMap();
  }
  */
}
