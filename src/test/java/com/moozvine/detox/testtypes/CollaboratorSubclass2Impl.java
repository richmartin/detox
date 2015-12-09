package com.moozvine.detox.testtypes;

import java.util.Collections;
import java.util.List;

public class CollaboratorSubclass2Impl implements CollaboratorSubclass2 {
  private String fromSubclass2;
  private String aChildMember;
  private List<AnEnum> theEnums = Collections.emptyList();

  @Override
  public String getFromSubclass2() {
    return fromSubclass2;
  }

  @Override
  public List<AnEnum> getTheEnums() {
    return theEnums;
  }

  @Override
  public String getAChildMember() {
    return aChildMember;
  }

  public CollaboratorSubclass2Impl setFromSubclass2(String fromSubclass2) {
    this.fromSubclass2 = fromSubclass2;
    return this;
  }

  public CollaboratorSubclass2Impl setaChildMember(String aChildMember) {
    this.aChildMember = aChildMember;
    return this;
  }

  public CollaboratorSubclass2Impl setTheEnums(List<AnEnum> theEnums) {
    this.theEnums = theEnums;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CollaboratorSubclass2)) return false;

    CollaboratorSubclass2 that = (CollaboratorSubclass2) o;

    if (aChildMember != null ? !aChildMember.equals(that.getAChildMember()) : that.getAChildMember() != null) return false;
    if (fromSubclass2 != null ? !fromSubclass2.equals(that.getFromSubclass2()) : that.getFromSubclass2() != null) return false;
    if (theEnums != null ? !theEnums.equals(that.getTheEnums()) : that.getTheEnums() != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = fromSubclass2 != null ? fromSubclass2.hashCode() : 0;
    result = 31 * result + (aChildMember != null ? aChildMember.hashCode() : 0);
    result = 31 * result + (theEnums != null ? theEnums.hashCode() : 0);
    return result;
  }
}
