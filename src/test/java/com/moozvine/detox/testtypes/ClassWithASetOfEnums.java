package com.moozvine.detox.testtypes;

import com.moozvine.detox.GenerateDTO;
import com.moozvine.detox.Serializable;

import java.util.Set;

@GenerateDTO
public interface ClassWithASetOfEnums extends Serializable {
  Set<AnEnum> getSomeEnums();
}
