package org.deephacks.vals;

public final class Sizes {

  public static final int getSize(boolean v) {
    return 1;
  }
  public static final int getSize(boolean[] v) {
    return v.length + 4;
  }

  public static final int getSize(Boolean v) {
    return 1;
  }

  public static final int getSize(char v) {
    return 2;
  }

  public static final int getSize(char[] v) {
    return 2 * v.length + 4;
  }

  public static final int getSize(Character c) {
    return 2;
  }

  public static final int getSize(byte v) {
    return 1;
  }

  public static final int getSize(byte[] v) {
    return v.length + 4;
  }

  public static final int getSize(Byte v) {
    return 1;
  }

  public static final int getSize(short v) {
    return 2;
  }

  public static final int getSize(short[] v) {
    return 2 * v.length + 4;
  }

  public static final int getSize(Short s) {
    return 2;
  }

  public static final int getSize(int v) {
    return 4;
  }

  public static final int getSize(int[] v) {
    return 4 * v.length + 4;
  }

  public static final int getSize(Integer v) {
    return 4;
  }

  public static final int getSize(long v) {
    return 8;
  }

  public static final int getSize(long[] v) {
    return 8 * v.length + 4;
  }

  public static final int getSize(Long v) {
    return 8;
  }

  public static final int getSize(float v) {
    return 4;
  }

  public static final int getSize(float[] v) {
    return 4 * v.length + 4;
  }

  public static final int getSize(Float v) {
    return 4;
  }

  public static final int getSize(double v) {
    return 8;
  }

  public static final int getSize(double[] v) {
    return 8 * v.length + 4;
  }

  public static final int getSize(Double v) {
    return 8;
  }

  public static final long getSize(Enum e) {
    return 4;
  }

  public static final long getSize(Encodable e) {
    return e.getTotalSize();
  }

  public static final long getSize(ByteString str) {
    return str.size() + 4;
  }
}
