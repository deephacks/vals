package org.deephacks.vals;

public class Sizes {

  public static int getSize(boolean v) {
    return 1;
  }

  public static int getSize(boolean[] v) {
    return v.length + 4;
  }

  public static int getSize(Boolean v) {
    return 1;
  }

  public static int getSize(char v) {
    return 2;
  }

  public static int getSize(char[] v) {
    return 2 * v.length + 4;
  }

  public static int getSize(Character c) {
    return 2;
  }

  public static int getSize(byte v) {
    return 1;
  }

  public static int getSize(byte[] v) {
    return v.length + 4;
  }

  public static int getSize(Byte v) {
    return 1;
  }

  public static int getSize(short v) {
    return 2;
  }

  public static int getSize(short[] v) {
    return 2 * v.length + 4;
  }

  public static int getSize(Short s) {
    return 2;
  }

  public static int getSize(int v) {
    return 4;
  }

  public static int getSize(int[] v) {
    return 4 * v.length + 4;
  }

  public static int getSize(Integer v) {
    return 4;
  }

  public static int getSize(long v) {
    return 8;
  }

  public static int getSize(long[] v) {
    return 8 * v.length + 4;
  }

  public static int getSize(Long v) {
    return 8;
  }

  public static int getSize(float v) {
    return 4;
  }

  public static int getSize(float[] v) {
    return 4 * v.length + 4;
  }

  public static int getSize(Float v) {
    return 4;
  }

  public static int getSize(double v) {
    return 8;
  }

  public static int getSize(double[] v) {
    return 8 * v.length + 4;
  }

  public static int getSize(Double v) {
    return 8;
  }

  public static long getSize(Enum en) {
    return 4;
  }

  public static long getSize(Encodable e) {
    return e.getTotalSize();
  }

  public static long getSize(ByteString str) {
    return str.size() + 4;
  }
}
