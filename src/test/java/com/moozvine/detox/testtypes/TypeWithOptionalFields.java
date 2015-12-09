package com.moozvine.detox.testtypes;

import com.moozvine.detox.GenerateDTO;
import com.moozvine.detox.Serializable;

import javax.annotation.Nullable;
import java.util.Date;

@GenerateDTO
public interface TypeWithOptionalFields extends Serializable {
  String getFirstRequiredField();
  Date getSecondRequiredField();
  @Nullable Integer getFirstOptionalField();
  @Nullable Float getSecondOptionalField();
}
