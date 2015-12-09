package com.moozvine.detox.testtypes;

import com.moozvine.detox.GenerateDTO;
import com.moozvine.detox.Serializable;

@GenerateDTO
public interface SimpleType extends Serializable {
  String getSomeString();
  int getAnInt();
}
