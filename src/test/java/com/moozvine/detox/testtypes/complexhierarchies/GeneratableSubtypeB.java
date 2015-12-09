package com.moozvine.detox.testtypes.complexhierarchies;

import com.moozvine.detox.GenerateDTO;

@GenerateDTO
public interface GeneratableSubtypeB extends NonGeneratableType {
  String getB();
}
