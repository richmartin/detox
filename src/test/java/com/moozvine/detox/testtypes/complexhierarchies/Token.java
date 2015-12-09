package com.moozvine.detox.testtypes.complexhierarchies;

import com.moozvine.detox.GenerateDTO;
import com.moozvine.detox.Serializable;

@GenerateDTO
public interface Token extends Serializable {
  String getValue();
}
