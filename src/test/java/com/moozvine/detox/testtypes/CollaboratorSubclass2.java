package com.moozvine.detox.testtypes;

import com.moozvine.detox.GenerateDTO;

import java.util.List;

@GenerateDTO
public interface CollaboratorSubclass2 extends CollaboratorSerializable {
  String getFromSubclass2();
  List<AnEnum> getTheEnums();
}
