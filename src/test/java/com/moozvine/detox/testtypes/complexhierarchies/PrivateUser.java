package com.moozvine.detox.testtypes.complexhierarchies;

import com.moozvine.detox.GenerateDTO;

@GenerateDTO
public interface PrivateUser extends User {
  String getPrivateInformation();
}
