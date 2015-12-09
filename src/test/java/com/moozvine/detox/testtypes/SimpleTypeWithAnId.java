package com.moozvine.detox.testtypes;

import com.moozvine.detox.GenerateDTO;
import com.moozvine.detox.Id;
import com.moozvine.detox.Serializable;

@GenerateDTO
public interface SimpleTypeWithAnId extends Serializable {
  @Id int getIntegerId();
  @Id String getStringId();
  String getNotId();
}
