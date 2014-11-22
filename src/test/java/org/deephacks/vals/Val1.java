package org.deephacks.vals;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Val
public interface Val1 extends Encodable {

  @Id(0) byte getBytePrimitive();
  @Id(1) byte[] getBytePrimitiveArray();
  @Id(2) Byte getByteObject();
  @Id(3) List<Byte> getByteList();
  @Id(5) Map<Byte, Byte> getByteMap();

  @Id(6) short getShortPrimitive();
  @Id(7) short[] getShortPrimitiveArray();
  @Id(8) Short getShortObject();
  @Id(9) List<Short> getShortList();
  @Id(11) Map<Short, Short> getShortMap();

  @Id(12) int getIntPrimitive();
  @Id(13) int[] getIntPrimitiveArray();
  @Id(14) Integer getIntegerObject();
  @Id(15) List<Integer> getIntegerList();
  @Id(17) Map<Integer, Integer> getIntegerMap();

  @Id(18) long getLongPrimitive();
  @Id(19) long[] getLongPrimitiveArray();
  @Id(20) Long getLongObject();
  @Id(21) List<Long> getLongList();
  @Id(23) Map<Long, Long> getLongMap();


  @Id(24) float getFloatPrimitive();
  @Id(25) float[] getFloatPrimitiveArray();
  @Id(26) Float getFloatObject();
  @Id(27) List<Float> getFloatList();
  @Id(29) Map<Float, Float> getFloatMap();

  @Id(30) double getDoublePrimitive();
  @Id(31) double[] getDoublePrimitiveArray();
  @Id(32) Double getDoubleObject();
  @Id(33) List<Double> getDoubleList();
  @Id(35) Map<Double, Double> getDoubleMap();

  @Id(36) boolean getBoolPrimitive();
  @Id(37) boolean[] getBoolPrimitiveArray();
  @Id(38) Boolean getBooleanObject();
  @Id(39) List<Boolean> getBooleanList();
  @Id(41) Map<Boolean, Boolean> getBooleanMap();

  @Id(42) char getCharPrimitive();
  @Id(43) char[] getCharPrimitiveArray();
  @Id(44) Character getCharacterObject();
  @Id(45) List<Character> getCharacterList();
  @Id(47) Map<Character, Character> getCharacterMap();

  @Id(48) String getString();
  @Id(49) List<String> getStringList();
  @Id(51) Map<String, String> getStringMap();

  @Id(60) TimeUnit getEnumValue();
  @Id(61) List<TimeUnit> getEnumList();
  @Id(62) EnumSet<TimeUnit> getEnumSet();
  @Id(63) Map<TimeUnit, TimeUnit> getEnumMap();

}
