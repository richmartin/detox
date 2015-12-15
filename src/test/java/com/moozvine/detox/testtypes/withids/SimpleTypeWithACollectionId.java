package com.moozvine.detox.testtypes.withids;

import com.moozvine.detox.GenerateDTO;
import com.moozvine.detox.Id;
import com.moozvine.detox.Serializable;

import java.util.List;

@GenerateDTO
public interface SimpleTypeWithACollectionId extends Serializable {
  @Id List<String> getCollectionId();
  String getNotId();
}
