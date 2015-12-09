package com.moozvine.detox;

import java.math.BigDecimal;
import java.math.BigInteger;

public final class StandardSerializers {
  public static final AbstractSerializationService.Serializer<Boolean> BOOLEAN
      = new StringSerializer<Boolean>(Boolean.class) {
    @Override public Boolean fromString(final String value) throws DeserializationException {
      if (value.equals("null")) {
        return null;
      }
      return Boolean.parseBoolean(value);
    }

    @Override public String toJson(final Boolean value) {
      return String.valueOf(value);
    }
  };

  public static final AbstractSerializationService.Serializer<Long> LONG = new StringSerializer<Long>(Long.class) {
    @Override
    public Long fromString(final String value) throws DeserializationException {
      if (value.equals("null")) {
        return null;
      }
      return Long.parseLong(value);
    }

    @Override
    public String toJson(final Long value) {
      return String.valueOf(value);
    }
  };
  
  public static final AbstractSerializationService.Serializer<Integer> INTEGER = new StringSerializer<Integer>(Integer.class) {
    @Override
    public Integer fromString(final String value) throws DeserializationException {
      if (value.equals("null")) {
        return null;
      }
      return Integer.parseInt(value);
    }

    @Override
    public String toJson(final Integer value) {
      return String.valueOf(value);
    }
  };
  public static final AbstractSerializationService.Serializer<String> STRING = new StringSerializer<String>(String.class) {
    @Override
    public String fromString(final String value) throws DeserializationException {
      return value;
    }

    @Override
    public String toJson(final String value) {
      return value;
    }
  };
  public static final AbstractSerializationService.Serializer<Double> DOUBLE = new StringSerializer<Double>(Double.class) {
    @Override
    public Double fromString(final String value) throws DeserializationException {
      if (value.equals("null")) {
        return null;
      }
      return Double.parseDouble(value);
    }

    @Override
    public String toJson(final Double value) {
      return String.valueOf(value);
    }

  };
  public static final AbstractSerializationService.Serializer<Float> FLOAT = new StringSerializer<Float>(Float.class) {
    @Override
    public Float fromString(final String value) throws DeserializationException {
      if (value.equals("null")) {
        return null;
      }
      return Float.parseFloat(value);
    }

    @Override
    public String toJson(final Float value) {
      return String.valueOf(value);
    }

  };
  public static final AbstractSerializationService.Serializer<BigDecimal> BIG_DECIMAL = new StringSerializer<BigDecimal>(BigDecimal.class) {
    @Override
    public BigDecimal fromString(final String value) throws DeserializationException {
      if (value.equals("null")) {
        return null;
      }
      return new BigDecimal(value);
    }

    @Override
    public String toJson(final BigDecimal value) {
      return String.valueOf(value.toString());
    }
  };
  public static final AbstractSerializationService.Serializer<BigInteger> BIG_INTEGER = new StringSerializer<BigInteger>(BigInteger.class) {
    @Override
    public BigInteger fromString(final String value) throws DeserializationException {
      if (value.equals("null")) {
        return null;
      }
      return new BigInteger(value);
    }

    @Override
    public String toJson(final BigInteger value) {
      return String.valueOf(value.toString());
    }
  };

  private StandardSerializers() {
  }
}
