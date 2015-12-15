package com.moozvine.detox.testtypes.withids;

import com.moozvine.detox.GenerateDTO;
import com.moozvine.detox.Id;
import com.moozvine.detox.Serializable;

@GenerateDTO
public interface SimpleTypeWithAFloatId extends Serializable {
  @Id float getFloatId();
  String getNotId();
}
