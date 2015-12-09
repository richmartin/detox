package com.moozvine.detox.testtypes;

import com.moozvine.detox.Serializable;

import java.util.List;

public interface ParentSerializable extends Serializable {
  boolean getABoolean();
  int getAnInt();
  long getALong();
  float getAFloat();
  double getADouble();
  byte getAByte();
  short getAShort();
  String getAString();
  List<String> getSomeStrings();

  CollaboratorSerializable getAChild();
//  CollaboratorSerializable[] getChildArray();
  List<CollaboratorSerializable> getChildList();

  char getAChar();
}
