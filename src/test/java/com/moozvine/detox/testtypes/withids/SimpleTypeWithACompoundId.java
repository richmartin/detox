package com.moozvine.detox.testtypes.withids;

import com.moozvine.detox.GenerateDTO;
import com.moozvine.detox.Id;
import com.moozvine.detox.Serializable;

@GenerateDTO
public interface SimpleTypeWithACompoundId extends Serializable {
  @Id int getIntegerId();
  @Id String getStringId();
  String getNotId();
}
