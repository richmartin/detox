package com.moozvine.detox.testtypes;

import com.moozvine.detox.GenerateDTO;

import java.util.Set;

@GenerateDTO
public interface SubclassSerializable extends ParentSerializable {
  String getASubclassMember();
  Set<Integer> getSomeIntegers();
}
