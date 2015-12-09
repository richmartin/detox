package com.moozvine.detox.testtypes;

public class CollaboratorSubclass1Impl implements CollaboratorSubclass1 {
  private String childMember;

  @Override
  public String getAChildMember() {
    return childMember;
  }

  public CollaboratorSubclass1Impl setAChildMember(String aChildMember) {
    this.childMember = aChildMember;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CollaboratorSerializable)) return false;

    CollaboratorSerializable that = (CollaboratorSerializable) o;

    if (childMember != null ? !childMember.equals(that.getAChildMember()) : that.getAChildMember() != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return childMember != null ? childMember.hashCode() : 0;
  }

  @Override
  public String getFromSubclass1() {
    return "from subclass 1";
  }
}
