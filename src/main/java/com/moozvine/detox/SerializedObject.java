package com.moozvine.detox;

public interface SerializedObject<T> {
  T getSerializedType();
  T getStoredInstance();
  T createNewStoredInstance();
  void updateStoredInstance();
}
