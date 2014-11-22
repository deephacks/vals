package org.deephacks.vals;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.deephacks.vals.CompilerUtils.BuilderProxy;

public class ValsUtil {

  public static Val1 val1(String... value) {
    return new BuilderProxy.Builder<>(Val1.class)
      .set(Val1::getBytePrimitive, (byte) 1)
      .set(Val1::getBytePrimitiveArray, new byte[]{(byte) 1, (byte) 2, (byte) 3})
      .set(Val1::getByteObject, (byte) 1)
      .set(Val1::getByteList, Arrays.asList(Byte.MAX_VALUE, Byte.MIN_VALUE))
      .set(Val1::getByteMap, newHashMap().with(Byte.MAX_VALUE, Byte.MIN_VALUE).build())
      .set(Val1::getShortPrimitive, (short) 123)
      .set(Val1::getShortPrimitiveArray, new short[] {(short)1, (short)2, (short)12345})
      .set(Val1::getShortObject, (short) 1)
      .set(Val1::getShortList, Arrays.asList(Short.MIN_VALUE, Short.MAX_VALUE))
      .set(Val1::getShortMap, newHashMap().with(Short.MIN_VALUE, Short.MAX_VALUE).build())
      .set(Val1::getIntPrimitive, Integer.MAX_VALUE)
      .set(Val1::getIntegerObject, Integer.MAX_VALUE)
      .set(Val1::getIntPrimitiveArray, new int[] {Integer.MAX_VALUE, 1, 1})
      .set(Val1::getIntegerList, Arrays.asList(Integer.MIN_VALUE, Integer.MAX_VALUE))
      .set(Val1::getIntegerMap, newHashMap().with(Integer.MIN_VALUE, Integer.MAX_VALUE).build())
      .set(Val1::getLongPrimitive, Long.MAX_VALUE)
      .set(Val1::getLongObject, Long.MAX_VALUE)
      .set(Val1::getLongPrimitiveArray, new long[] {1L, Long.MAX_VALUE, Long.MIN_VALUE})
      .set(Val1::getLongList, Arrays.asList(Long.MAX_VALUE, 0L, Long.MIN_VALUE))
      .set(Val1::getLongMap, newHashMap().with(Long.MAX_VALUE, Long.MIN_VALUE).build())
      .set(Val1::getFloatPrimitive, Float.MAX_VALUE)
      .set(Val1::getFloatObject, Float.MAX_VALUE)
      .set(Val1::getFloatPrimitiveArray, new float[]{1f, Float.MAX_VALUE, Float.MIN_VALUE})
      .set(Val1::getFloatList, Arrays.asList(Float.MAX_VALUE, 0f, Float.MIN_VALUE))
      .set(Val1::getFloatMap, newHashMap().with(Float.MAX_VALUE, Float.MIN_VALUE).build())
      .set(Val1::getDoublePrimitive, Double.MAX_VALUE)
      .set(Val1::getDoubleObject, Double.MAX_VALUE)
      .set(Val1::getDoublePrimitiveArray, new double[] {1d, Double.MAX_VALUE, Double.MIN_VALUE})
      .set(Val1::getDoubleList, Arrays.asList(Double.MAX_VALUE, 0d, Double.MIN_VALUE))
      .set(Val1::getDoubleMap, newHashMap().with(Double.MAX_VALUE, Double.MIN_VALUE).build())
      .set(Val1::getBoolPrimitive, true)
      .set(Val1::getBooleanObject, true)
      .set(Val1::getBoolPrimitiveArray, new boolean[] {false, true, false})
      .set(Val1::getBooleanList, Arrays.asList(true, false))
      .set(Val1::getBooleanMap, newHashMap().with(true, false).build())
      .set(Val1::getCharPrimitive, 'g')
      .set(Val1::getCharacterObject, 'g')
      .set(Val1::getCharPrimitiveArray, new char[]{'a', Character.MIN_VALUE, Character.MAX_VALUE})
      .set(Val1::getCharacterList, Arrays.asList(Character.MAX_VALUE, Character.MIN_VALUE))
      .set(Val1::getCharacterMap, newHashMap().with(Character.MAX_VALUE, Character.MIN_VALUE).build())
      .set(Val1::getString, value.length == 0 ? "value" : value[0])
      .set(Val1::getStringList, Arrays.asList("1a", "2b", "3c"))
      .set(Val1::getStringMap, newHashMap().with("a", "b").with("c", "d").build())
      .set(Val1::getEnumValue, TimeUnit.DAYS)
      .set(Val1::getEnumList, Arrays.asList(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MILLISECONDS))
      .set(Val1::getEnumSet, EnumSet.of(TimeUnit.MICROSECONDS, TimeUnit.HOURS))
      .set(Val1::getEnumMap, newHashMap().with(TimeUnit.MICROSECONDS, TimeUnit.MILLISECONDS).build())
      .build().get();
  }

  public static Val2 val2(String... value) {
    return new BuilderProxy.Builder<>(Val2.class)
      .set(Val2::getBytePrimitive, (byte) 1)
      .set(Val2::getBytePrimitiveArray, new byte[]{(byte) 1, (byte) 2, (byte) 3})
      .set(Val2::getByteObject, (byte) 1)
      .set(Val2::getByteList, Arrays.asList(Byte.MAX_VALUE, Byte.MIN_VALUE))
      .set(Val2::getByteMap, newHashMap().with(Byte.MAX_VALUE, Byte.MIN_VALUE).build())
      .set(Val2::getShortPrimitive, (short) 123)
      .set(Val2::getShortPrimitiveArray, new short[] {(short)1, (short)2, (short)12345})
      .set(Val2::getShortObject, (short) 1)
      .set(Val2::getShortList, Arrays.asList(Short.MIN_VALUE, Short.MAX_VALUE))
      .set(Val2::getShortMap, newHashMap().with(Short.MIN_VALUE, Short.MAX_VALUE).build())
      .set(Val2::getIntPrimitive, Integer.MAX_VALUE)
      .set(Val2::getIntegerObject, Integer.MAX_VALUE)
      .set(Val2::getIntPrimitiveArray, new int[] {Integer.MAX_VALUE, 1, 1})
      .set(Val2::getIntegerList, Arrays.asList(Integer.MIN_VALUE, Integer.MAX_VALUE))
      .set(Val2::getIntegerMap, newHashMap().with(Integer.MIN_VALUE, Integer.MAX_VALUE).build())
      .set(Val2::getLongPrimitive, Long.MAX_VALUE)
      .set(Val2::getLongObject, Long.MAX_VALUE)
      .set(Val2::getLongPrimitiveArray, new long[] {1L, Long.MAX_VALUE, Long.MIN_VALUE})
      .set(Val2::getLongList, Arrays.asList(Long.MAX_VALUE, 0L, Long.MIN_VALUE))
      .set(Val2::getLongMap, newHashMap().with(Long.MAX_VALUE, Long.MIN_VALUE).build())
      .set(Val2::getFloatPrimitive, Float.MAX_VALUE)
      .set(Val2::getFloatObject, Float.MAX_VALUE)
      .set(Val2::getFloatPrimitiveArray, new float[]{1f, Float.MAX_VALUE, Float.MIN_VALUE})
      .set(Val2::getFloatList, Arrays.asList(Float.MAX_VALUE, 0f, Float.MIN_VALUE))
      .set(Val2::getFloatMap, newHashMap().with(Float.MAX_VALUE, Float.MIN_VALUE).build())
      .set(Val2::getDoublePrimitive, Double.MAX_VALUE)
      .set(Val2::getDoubleObject, Double.MAX_VALUE)
      .set(Val2::getDoublePrimitiveArray, new double[] {1d, Double.MAX_VALUE, Double.MIN_VALUE})
      .set(Val2::getDoubleList, Arrays.asList(Double.MAX_VALUE, 0d, Double.MIN_VALUE))
      .set(Val2::getDoubleMap, newHashMap().with(Double.MAX_VALUE, Double.MIN_VALUE).build())
      .set(Val2::getBoolPrimitive, true)
      .set(Val2::getBooleanObject, true)
      .set(Val2::getBoolPrimitiveArray, new boolean[] {false, true, false})
      .set(Val2::getBooleanList, Arrays.asList(true, false))
      .set(Val2::getBooleanMap, newHashMap().with(true, false).build())
      .set(Val2::getCharPrimitive, 'g')
      .set(Val2::getCharacterObject, 'g')
      .set(Val2::getCharPrimitiveArray, new char[]{'a', Character.MIN_VALUE, Character.MAX_VALUE})
      .set(Val2::getCharacterList, Arrays.asList(Character.MAX_VALUE, Character.MIN_VALUE))
      .set(Val2::getCharacterMap, newHashMap().with(Character.MAX_VALUE, Character.MIN_VALUE).build())
      .set(Val2::getString, value.length == 0 ? "value" : value[0])
      .set(Val2::getStringList, Arrays.asList("1a", "2b", "3c"))
      .set(Val2::getStringMap, newHashMap().with("a", "b").build())
      .set(Val2::getEnumValue, TimeUnit.DAYS)
      .set(Val2::getEnumList, Arrays.asList(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MILLISECONDS))
      .set(Val2::getEnumSet, EnumSet.of(TimeUnit.MICROSECONDS, TimeUnit.HOURS))
      .set(Val2::getEnumMap, newHashMap().with(TimeUnit.MICROSECONDS, TimeUnit.MILLISECONDS).build())
      .set(Val2::getEmbedded, val1("1"))
      .set(Val2::getEmbeddedList, Arrays.asList(val1("1"), val1("2")))
      .set(Val2::getEmbeddedMap, newHashMap().with("3", val1("3")).with("4", val1("4")).build())
      .build().get();
  }

  public static Val3 val3() {
    return new BuilderProxy.Builder<>(Val3.class)
      .set(Val3::getVal4, val4("1"))
      .set(Val3::getVal4List, Arrays.asList(val4("2"), val4("3")))
      .build().get();
  }

  public static Val4 val4(String id) {
    return new BuilderProxy.Builder<>(Val4.class)
      .set(Val4::getId, id).build().get();
  }

  public static Val6 val6(String id) {
    return new BuilderProxy.Builder<>(Val6.class)
      .set(Val6::getId, id)
      .set(Val6::getPByteArray, new byte[]{1, 2, 3, 4, 5})
      .set(Val6::getPByte, (byte) 1)
      .set(Val6::getByteList, Arrays.asList((byte) 1, (byte) 2))
      .set(Val6::getByteMap, newHashMap().with((byte) 1, (byte) 2).build())
      .set(Val6::getString, id)
      .set(Val6::getStringList, Arrays.asList(id, id + "2"))
      .set(Val6::getStringMap, newHashMap().with(id + "2", id + "3").with(id + "4", id + "5") .build())
      .set(Val6::getIntegerStringMap, newHashMap().with(1, id + "2").with(2, id + "3").build())
      .set(Val6::getStringIntegerMap, newHashMap().with(id, 1).with(id + "2", 2).build())
      .set(Val6::getEnumStringMap, newHashMap().with(TimeUnit.DAYS, id + "2").with(TimeUnit.HOURS, id + "3").build())
      .set(Val6::getStringEnumMap, newHashMap().with(id, TimeUnit.DAYS).with(id + "2", TimeUnit.HOURS).build())
      .set(Val6::getEnumSet, EnumSet.of(TimeUnit.DAYS, TimeUnit.HOURS))
      .build().get();
  }

  public static <T> T decode(Class<T> cls, DirectBuffer buffer, int offset) {
    try {
      Constructor<T> c = SourceTypeUtil.getGeneratedClassConstructor(cls);
      return c.newInstance(buffer, offset);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T decode(Class<T> cls, byte[] bytes) {
    return decode(cls, new DirectBuffer(bytes), 0);
  }

  public static <T> T decode(Class<T> cls, DirectBuffer buffer) {
    return decode(cls, buffer, 0);
  }

  public static <K, V> HashMapBuilder<K, V> newHashMap() {
    return new HashMapBuilder<>();
  }

  public static class HashMapBuilder<K, V> {
    HashMap<K, V> map = new HashMap<>();

    public HashMapBuilder with(K k, V v) {
      map.put(k, v);
      return this;
    }

    public Map<K, V> build() {
      return map;
    }
  }
}
