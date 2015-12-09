package com.moozvine.detox.testtypes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubclassSerializableImpl implements SubclassSerializable {
  private boolean aBoolean;
  private int anInt;
  private long aLong;
  private float aFloat;
  private double aDouble;
  private char aChar;
  private byte aByte;
  private short aShort;
  private String aString;
  private String aSubclassMember;
  private CollaboratorSerializable aChild;
  private CollaboratorSerializable[] childArray;
  private List<CollaboratorSerializable> childList;

  public SubclassSerializableImpl() {
  }

  public SubclassSerializableImpl(SubclassSerializable toCopy) {
    aBoolean = toCopy.getABoolean();
    anInt = toCopy.getAnInt();
    aLong = toCopy.getALong();
    aFloat = toCopy.getAFloat();
    aDouble = toCopy.getADouble();
    aChar = toCopy.getAChar();
    aByte = toCopy.getAByte();
    aShort = toCopy.getAShort();
    aString = toCopy.getAString();
//    aChild = toCopy.getAChild();
//    childArray = toCopy.getChildArray();
    childList = toCopy.getChildList();
    aSubclassMember = toCopy.getASubclassMember();
  }

  public boolean getABoolean() {
    return aBoolean;
  }

  public SubclassSerializableImpl setABoolean(boolean aBoolean) {
    this.aBoolean = aBoolean;
    return this;
  }

  public int getAnInt() {
    return anInt;
  }

  public SubclassSerializableImpl setAnInt(int anInt) {
    this.anInt = anInt;
    return this;
  }

  public long getALong() {
    return aLong;
  }

  public SubclassSerializableImpl setALong(long aLong) {
    this.aLong = aLong;
    return this;
  }

  public float getAFloat() {
    return aFloat;
  }

  public SubclassSerializableImpl setAFloat(float aFloat) {
    this.aFloat = aFloat;
    return this;
  }

  public double getADouble() {
    return aDouble;
  }

  public SubclassSerializableImpl setADouble(double aDouble) {
    this.aDouble = aDouble;
    return this;
  }

  public char getAChar() {
    return aChar;
  }

  public SubclassSerializableImpl setAChar(char aChar) {
    this.aChar = aChar;
    return this;
  }

  public byte getAByte() {
    return aByte;
  }

  public SubclassSerializableImpl setAByte(byte aByte) {
    this.aByte = aByte;
    return this;
  }

  public short getAShort() {
    return aShort;
  }

  public SubclassSerializableImpl setAShort(short aShort) {
    this.aShort = aShort;
    return this;
  }

  public String getAString() {
    return aString;
  }

  @Override
  public List<String> getSomeStrings() {
    return Arrays.asList("one string", "two string", "red string", "blue string");
  }

  @Override
  public Set<Integer> getSomeIntegers() {
    return new HashSet<>(Arrays.<Integer>asList(10, 9, 8));
  }

  public SubclassSerializableImpl setAString(String aString) {
    this.aString = aString;
    return this;
  }

  public CollaboratorSerializable getAChild() {
    return aChild;
  }

  public SubclassSerializableImpl setACollaborator(CollaboratorSerializable aChild) {
    this.aChild = aChild;
    return this;
  }

  public CollaboratorSerializable[] getChildArray() {
    return childArray;
  }

  public SubclassSerializableImpl setChildArray(CollaboratorSerializable[] childArray) {
    this.childArray = childArray;
    return this;
  }

  public List<CollaboratorSerializable> getChildList() {
    return childList;
  }

  public SubclassSerializableImpl setChildList(List<CollaboratorSerializable> childList) {
    this.childList = childList;
    return this;
  }

  public String getASubclassMember() {
    return aSubclassMember;
  }

  public SubclassSerializableImpl setASubclassMember(String aSubclassMember) {
    this.aSubclassMember = aSubclassMember;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SubclassSerializable)) return false;

    SubclassSerializable that = (SubclassSerializable) o;

    if (aBoolean != that.getABoolean()) return false;
    if (aByte != that.getAByte()) return false;
    if (aChar != that.getAChar()) return false;
    if (Double.compare(that.getADouble(), aDouble) != 0) return false;
    if (Float.compare(that.getAFloat(), aFloat) != 0) return false;
    if (aLong != that.getALong()) return false;
    if (aShort != that.getAShort()) return false;
    if (anInt != that.getAnInt()) return false;
    if (aString != null ? !aString.equals(that.getAString()) : that.getAString() != null) return false;
    if (aSubclassMember != null ? !aSubclassMember.equals(that.getASubclassMember()) : that.getASubclassMember() != null)
      return false;
    if (aChild != null ? !aChild.equals(that.getAChild()) : that.getAChild() != null) return false;
    if (childList != null ? !childList.equals(that.getChildList()) : that.getChildList() != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = (aBoolean ? 1 : 0);
    result = 31 * result + anInt;
    result = 31 * result + (int) (aLong ^ (aLong >>> 32));
    result = 31 * result + (aFloat != +0.0f ? Float.floatToIntBits(aFloat) : 0);
    temp = Double.doubleToLongBits(aDouble);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (int) aChar;
    result = 31 * result + (int) aByte;
    result = 31 * result + (int) aShort;
    result = 31 * result + (aString != null ? aString.hashCode() : 0);
    result = 31 * result + (aSubclassMember != null ? aSubclassMember.hashCode() : 0);
    return result;
  }
}
