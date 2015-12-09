package com.moozvine.detox.testtypes.complexhierarchies;

import com.moozvine.detox.GenerateDTO;
import com.moozvine.detox.Serializable;

import java.util.List;

@GenerateDTO
public interface HasHeterogeneousCollection extends Serializable {
  List<NonGeneratableType> getCollection();
}
