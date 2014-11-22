package org.deephacks.vals;

import org.junit.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertTrue;
import static org.deephacks.vals.ValsUtil.newHashMap;
import static org.deephacks.vals.ValsUtil.val1;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CompilerTest {

  @Test
  public void testEqualsHashcode() {
    CompilerUtils.compile(Val1.class, Val2.class);
    String value = "1";
    Val2 val1 = ValsUtil.val2(value);
    Val2 val2 = ValsUtil.decode(Val2.class, val1.toByteArray());

    assertThat(val2.getBytePrimitive(), is((byte) 1));
    assertThat(val2.getBytePrimitiveArray(), is(new byte[]{(byte) 1, (byte) 2, (byte) 3}));
    assertThat(val2.getByteObject(), is((byte) 1));
    assertThat(val2.getByteList(), is(Arrays.asList(Byte.MAX_VALUE, Byte.MIN_VALUE)));
    assertThat(val2.getByteMap(), is(newHashMap().with(Byte.MAX_VALUE, Byte.MIN_VALUE).build()));
    assertThat(val2.getShortPrimitive(), is((short) 123));
    assertThat(val2.getShortPrimitiveArray(), is(new short[] {(short)1, (short)2, (short)12345}));
    assertThat(val2.getShortObject(), is((short) 1));
    assertThat(val2.getShortList(), is(Arrays.asList(Short.MIN_VALUE, Short.MAX_VALUE)));
    assertThat(val2.getShortMap(), is(newHashMap().with(Short.MIN_VALUE, Short.MAX_VALUE).build()));
    assertThat(val2.getIntPrimitive(), is(Integer.MAX_VALUE));
    assertThat(val2.getIntegerObject(), is(Integer.MAX_VALUE));
    assertThat(val2.getIntPrimitiveArray(), is(new int[] {Integer.MAX_VALUE, 1, 1}));
    assertThat(val2.getIntegerList(), is(Arrays.asList(Integer.MIN_VALUE, Integer.MAX_VALUE)));
    assertThat(val2.getIntegerMap(), is(newHashMap().with(Integer.MIN_VALUE, Integer.MAX_VALUE).build()));
    assertThat(val2.getLongPrimitive(), is(Long.MAX_VALUE));
    assertThat(val2.getLongObject(), is(Long.MAX_VALUE));
    assertThat(val2.getLongPrimitiveArray(), is(new long[] {1L, Long.MAX_VALUE, Long.MIN_VALUE}));
    assertThat(val2.getLongList(), is(Arrays.asList(Long.MAX_VALUE, 0L, Long.MIN_VALUE)));
    assertThat(val2.getLongMap(), is(newHashMap().with(Long.MAX_VALUE, Long.MIN_VALUE).build()));
    assertThat(val2.getFloatPrimitive(), is(Float.MAX_VALUE));
    assertThat(val2.getFloatObject(), is(Float.MAX_VALUE));
    assertThat(val2.getFloatPrimitiveArray(), is(new float[]{1f, Float.MAX_VALUE, Float.MIN_VALUE}));
    assertThat(val2.getFloatList(), is(Arrays.asList(Float.MAX_VALUE, 0f, Float.MIN_VALUE)));
    assertThat(val2.getFloatMap(), is(newHashMap().with(Float.MAX_VALUE, Float.MIN_VALUE).build()));
    assertThat(val2.getDoublePrimitive(), is(Double.MAX_VALUE));
    assertThat(val2.getDoubleObject(), is(Double.MAX_VALUE));
    assertThat(val2.getDoublePrimitiveArray(), is(new double[] {1d, Double.MAX_VALUE, Double.MIN_VALUE}));
    assertThat(val2.getDoubleList(), is(Arrays.asList(Double.MAX_VALUE, 0d, Double.MIN_VALUE)));
    assertThat(val2.getDoubleMap(), is(newHashMap().with(Double.MAX_VALUE, Double.MIN_VALUE).build()));
    assertThat(val2.getBoolPrimitive(), is(true));
    assertThat(val2.getBooleanObject(), is(true));
    assertThat(val2.getBoolPrimitiveArray(), is(new boolean[] {false, true, false}));
    assertThat(val2.getBooleanList(), is(Arrays.asList(true, false)));
    assertThat(val2.getBooleanMap(), is(newHashMap().with(true, false).build()));
    assertThat(val2.getCharPrimitive(), is('g'));
    assertThat(val2.getCharacterObject(), is('g'));
    assertThat(val2.getCharPrimitiveArray(), is(new char[]{'a', Character.MIN_VALUE, Character.MAX_VALUE}));
    assertThat(val2.getCharacterList(), is(Arrays.asList(Character.MAX_VALUE, Character.MIN_VALUE)));
    assertThat(val2.getCharacterMap(), is(newHashMap().with(Character.MAX_VALUE, Character.MIN_VALUE).build()));
    assertThat(val2.getString(), is(value));
    assertThat(val2.getStringList(), is(Arrays.asList("1a", "2b", "3c")));
    assertThat(val2.getStringMap(), is(newHashMap().with("a", "b").build()));
    assertThat(val2.getEnumValue(), is(TimeUnit.DAYS));
    assertThat(val2.getEnumList(), is(Arrays.asList(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MILLISECONDS)));
    assertThat(val2.getEnumSet(), is(EnumSet.of(TimeUnit.MICROSECONDS, TimeUnit.HOURS)));
    assertThat(val2.getEnumMap(), is(newHashMap().with(TimeUnit.MICROSECONDS, TimeUnit.MILLISECONDS).build()));
    assertThat(val2.getEmbedded(), is(val1("1")));
    assertThat(val2.getEmbeddedList(), is(Arrays.asList(val1("1"), val1("2"))));
    assertThat(val2.getEmbeddedMap(), is(newHashMap().with("3", val1("3")).with("4", val1("4")).build()));

    assertTrue(val1.equals(val2));
    assertTrue(val2.equals(val1));

    assertTrue(val1.hashCode() == val2.hashCode());
  }
}
