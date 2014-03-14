package org.deephacks.vals.processor.finalvalue;

import org.deephacks.vals.FinalValue;

import java.util.List;
import java.util.Map;
import java.util.Set;

@FinalValue
public interface TypesFinal {

  String getString();
  Byte getByteObject();
  Short getShortObject();
  Integer getIntegerObject();
  Long getLongObject();
  Float getFloatObject();
  Double getDoubleObject();
  Character getCharObject();
  Boolean getBooleanObject();

  byte getBytePrim();
  byte[] getBytePrimArray();
  short getShortPrim();
  short[] getShortPrimArray();
  int getIntPrim();
  int[] getIntPrimArray();
  long getLongPrim();
  long[] getLongPrimArray();
  float getFloatPrim();
  float[] getFloatPrimArray();
  double getDoublePrim();
  double[] getDoublePrimArray();
  char getCharPrim();
  char[] getCharPrimArray();
  boolean getBooleanPrim();
  boolean[] getBooleanPrimArray();

  ReferenceClass getReferenceClass();
  List<ReferenceClass> getReferenceClassList();
  Map<String, ReferenceClass> getReferenceClassMap();
  Set<ReferenceClass> getReferenceClassSet();

  // check that we can have not-getter default methods
  default void getValue() {

  }

  @FinalValue
  public static interface ReferenceClass {
    public String getValue();
    public int getIntValue();
  }
}
