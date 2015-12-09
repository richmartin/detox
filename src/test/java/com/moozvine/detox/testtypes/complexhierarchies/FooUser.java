package com.moozvine.detox.testtypes.complexhierarchies;

import com.moozvine.detox.GenerateDTO;

@GenerateDTO
public interface FooUser extends User {
  FooToken getToken();
}
