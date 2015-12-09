package com.moozvine.detox.testtypes;

import com.moozvine.detox.GenerateDTO;
import com.moozvine.detox.Serializable;

import java.util.List;

@GenerateDTO
public interface SerializableWithList extends Serializable {
  List<String> getSomeStrings();
}
