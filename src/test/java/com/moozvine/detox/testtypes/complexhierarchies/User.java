package com.moozvine.detox.testtypes.complexhierarchies;

import com.moozvine.detox.GenerateDTO;
import com.moozvine.detox.Serializable;

@GenerateDTO
public interface User extends Serializable {
  Token getToken();
}
