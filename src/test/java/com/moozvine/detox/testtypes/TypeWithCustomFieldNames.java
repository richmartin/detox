package com.moozvine.detox.testtypes;

import com.moozvine.detox.GenerateDTO;
import com.moozvine.detox.JsonFieldName;
import com.moozvine.detox.Serializable;

import javax.annotation.Nullable;
import java.util.Date;

@GenerateDTO
public interface TypeWithCustomFieldNames extends Serializable {
  @JsonFieldName("required_field_1") String getFirstRequiredField();
  @JsonFieldName("required_field_2") Date getSecondRequiredField();
  @JsonFieldName("optional_field_1") @Nullable Integer getFirstOptionalField();
  @JsonFieldName("optional_field_2") @Nullable Float getSecondOptionalField();
}
