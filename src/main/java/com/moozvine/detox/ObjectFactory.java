package com.moozvine.detox;

import com.moozvine.detox.repackaged.org.json.JSONObject;

/**
 * Created by rich on 09/07/15.
 */
public interface ObjectFactory<T extends Serializable> {
  DTO createDTO(
      AbstractSerializationService service,
      T original);

  DTO createDTO(
      AbstractSerializationService service,
      JSONObject json) throws DeserializationException;
}
