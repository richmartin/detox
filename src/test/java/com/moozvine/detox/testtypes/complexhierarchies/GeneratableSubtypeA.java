package com.moozvine.detox.testtypes.complexhierarchies;

import com.moozvine.detox.GenerateDTO;

@GenerateDTO
public interface GeneratableSubtypeA extends NonGeneratableType {
  String getA();
}
