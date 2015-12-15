package com.moozvine.detox.testtypes.withids;

import com.moozvine.detox.GenerateDTO;
import com.moozvine.detox.Id;
import com.moozvine.detox.Serializable;

import javax.mail.internet.InternetAddress;

@GenerateDTO
public interface SimpleTypeWithAnObjectId extends Serializable {
  @Id InternetAddress getObjectId();
  String getNotId();
}
