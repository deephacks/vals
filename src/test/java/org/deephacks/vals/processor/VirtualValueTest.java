package org.deephacks.vals.processor;

import org.deephacks.vals.processor.virtualvalue.DefaultVirtual;
import org.deephacks.vals.processor.virtualvalue.IllegalPropertyVirtual;
import org.deephacks.vals.processor.virtualvalue.InnerVirtual;
import org.deephacks.vals.processor.virtualvalue.InnerVirtual.InnerClass1;
import org.deephacks.vals.processor.virtualvalue.InnerVirtual.InnerClass1.InnerClass2;
import org.deephacks.vals.processor.virtualvalue.NotInterfaceVirtual;
import org.deephacks.vals.processor.virtualvalue.NullablePrimitiveVirtual;
import org.deephacks.vals.processor.virtualvalue.NullableVirtual;
import org.deephacks.vals.processor.virtualvalue.TypesVirtual;
import org.deephacks.vals.processor.virtualvalue.TypesVirtual.ReferenceClass;
import org.deephacks.vals.processor.Proxy.Builder;
import org.junit.Before;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.junit.internal.matchers.StringContains.containsString;

public class VirtualValueTest extends BaseTest {

  @Before
  public void setup() {
    compile(DefaultVirtual.class, TypesVirtual.class, NullableVirtual.class, InnerVirtual.class);
  }

  @Test
  public void testObjectAndPrimitivesClass() throws IOException {
    ReferenceClass r1 = new Builder<>(ReferenceClass.class, ReferenceClass.class.getPackage())
            .set(ReferenceClass::getIntValue, 1)
            .set(ReferenceClass::getValue, "value")
            .build().get();

    TypesVirtual o1 = createObject(r1).build().get();
    TypesVirtual o2 = createObject(r1).build().get();

    assertThat(o1.getBytePrim(), is((byte)1));
    assertThat(o1.getByteObject(), is((byte)2));
    assertThat(o1.getShortPrim(), is((short)3));
    assertThat(o1.getShortObject(), is((short)4));
    assertThat(o1.getIntPrim(), is(3));
    assertThat(o1.getIntegerObject(), is(4));
    assertThat(o1.getLongPrim(), is(5L));
    assertThat(o1.getLongObject(), is(6L));
    assertThat(o1.getFloatPrim(), is(7.0f));
    assertThat(o1.getFloatObject(), is(8.0f));
    assertThat(o1.getDoublePrim(), is(9.0));
    assertThat(o1.getDoubleObject(), is(10.0));
    assertThat(o1.getBooleanPrim(), is(true));
    assertThat(o1.getBooleanObject(), is(true));
    assertThat(o1.getCharPrim(), is('a'));
    assertThat(o1.getCharObject(), is('b'));
    assertThat(o1.getString(), is("string"));

    assertTrue(o1.equals(o2));
    assertTrue(o2.equals(o1));

    assertTrue(o1.hashCode() == o2.hashCode());

    String toString = "TypesVirtual{" +
            "booleanObject=true," +
            "booleanPrim=true," +
            "booleanPrimArray=[true]," +
            "byteObject=2," +
            "bytePrim=1," +
            "bytePrimArray=[1]," +
            "charObject=b," +
            "charPrim=a," +
            "charPrimArray=[a]," +
            "doubleObject=10.0," +
            "doublePrim=9.0," +
            "doublePrimArray=[1.0]," +
            "floatObject=8.0," +
            "floatPrim=7.0," +
            "floatPrimArray=[1.0]," +
            "intPrim=3," +
            "intPrimArray=[1]," +
            "integerObject=4," +
            "longObject=6," +
            "longPrim=5," +
            "longPrimArray=[1]," +
            "referenceClass=ReferenceClass{intValue=1,value=value}," +
            "referenceClassList=[ReferenceClass{intValue=1,value=value}]," +
            "referenceClassMap={ReferenceClass=ReferenceClass{intValue=1,value=value}}," +
            "referenceClassSet=[ReferenceClass{intValue=1,value=value}]," +
            "shortObject=4," +
            "shortPrim=3," +
            "shortPrimArray=[1]," +
            "string=string}";

    assertThat(toString, is(o1.toString()));
    assertThat(toString, is(o2.toString()));

    TypesVirtual o3 = createObject(r1).set(TypesVirtual::getIntPrim, 10).build().get();
    assertFalse(o1.equals(o3));
    assertFalse(o3.equals(o1));
    assertFalse(o1.hashCode() == o3.hashCode());
  }


  @Test
  public void testRejectNonNullable() {
    try {
      new Builder<>(NullableVirtual.class);
    } catch (NullPointerException e) {
      assertThat(e.getMessage(), containsString("value"));
    }
  }

  @Test
  public void testAcceptNullable() {
    NullableVirtual n1 = new Builder<>(NullableVirtual.class).set(NullableVirtual::getValue, "v").build().get();
    NullableVirtual n2 = new Builder<>(NullableVirtual.class).set(NullableVirtual::getValue, "v").build().get();

    assertEquals(n1.getValue(), "v");
    assertEquals(n2.getValue(), "v");

    assertNull(n1.getNullable());
    assertNull(n2.getNullable());

    assertTrue(n1.equals(n2));
    assertTrue(n2.equals(n1));

    assertTrue(n1.hashCode() == n2.hashCode());
    assertEquals(n1.toString(), n2.toString());
  }

  @Test
  public void testInnerClass() {
    InnerClass1 inner1 = new Builder<>(InnerClass1.class, InnerClass1.class.getPackage())
            .set(InnerClass1::getValue, "inner1").build().get();
    assertThat(inner1.getValue(), is("inner1"));

    InnerClass2 inner2 = new Builder<>(InnerClass2.class, InnerClass2.class.getPackage())
            .set(InnerClass2::getValue, "inner2").build().get();
    assertThat(inner2.getValue(), is("inner2"));

  }

  @Test
  public void testRejectDefault() {
    try {
      compile(DefaultVirtual.class);
      new Builder<>(DefaultVirtual.class);
    } catch (NullPointerException e) {
      assertThat(e.getMessage(), containsString("value"));
    }
  }

  @Test
  public void testAcceptDefault() {
    DefaultVirtual n1 = new Builder<>(DefaultVirtual.class).set(DefaultVirtual::getValue, "v").build().get();
    DefaultVirtual n2 = new Builder<>(DefaultVirtual.class).set(DefaultVirtual::getValue, "v").build().get();

    assertEquals(n1.getValue(), "v");
    assertEquals(n2.getValue(), "v");

    assertEquals(n1.getDefaultValue(), "default");
    assertEquals(n2.getDefaultValue(), "default");

    assertTrue(n1.equals(n2));
    assertTrue(n2.equals(n1));

    assertTrue(n1.hashCode() == n2.hashCode());
    assertEquals(n1.toString(), n2.toString());
  }

  @Test
  public void testShouldNotCompileNonInterface() {
    try {
      compileNoClean(NotInterfaceVirtual.class);
      fail("should fail");
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(), JUnitMatchers.containsString("can only be applied to interfaces"));
    }
  }

  @Test
  public void testShouldNotCompileIllegalKeywords() {
    try {
      compileNoClean(IllegalPropertyVirtual.class);
      fail("should fail");
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(), JUnitMatchers.containsString("Java keyword"));
    }
  }

  @Test
  public void testShouldNotCompileNullablePrimitives() {
    try {
      compileNoClean(NullablePrimitiveVirtual.class);
      fail("should fail");
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(), JUnitMatchers.containsString("Primitive fields cannot be nullable"));
    }
  }

  /* Test does not work. For some reason type.getModifiers() always seem to be Modifier.STATIC even though type is not?!
  @Test
  public void testShouldNotCompileNonStaticInner() {
    try {
      compileNoClean(NonStaticInner.class);
      fail("should fail");
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(), JUnitMatchers.containsString("Primitive fields cannot be nullable"));
    }
  }
  */

  private Builder<TypesVirtual> createObject(ReferenceClass r1) {
    List<ReferenceClass> list = Arrays.asList(r1);
    Set<ReferenceClass> set = new HashSet<>(list);
    Map<String, ReferenceClass> map = new HashMap<>();
    map.put("ReferenceClass", r1);

    return new Builder<>(TypesVirtual.class)
            .set(TypesVirtual::getBytePrim, (byte) 1)
            .set(TypesVirtual::getBytePrimArray, new byte[]{1})
            .set(TypesVirtual::getByteObject, (byte) 2)
            .set(TypesVirtual::getShortPrim, (short) 3)
            .set(TypesVirtual::getShortPrimArray, new short[]{1})
            .set(TypesVirtual::getShortObject, (short) 4)
            .set(TypesVirtual::getIntPrim, 3)
            .set(TypesVirtual::getIntPrimArray, new int[] {1})
            .set(TypesVirtual::getIntegerObject, 4)
            .set(TypesVirtual::getLongPrim, 5L)
            .set(TypesVirtual::getLongPrimArray, new long[] {1})
            .set(TypesVirtual::getLongObject, 6L)
            .set(TypesVirtual::getFloatPrim, 7.0f)
            .set(TypesVirtual::getFloatPrimArray,  new float[] {1.0f})
            .set(TypesVirtual::getFloatObject, 8.0f)
            .set(TypesVirtual::getDoublePrim, 9.0)
            .set(TypesVirtual::getDoublePrimArray,  new double[] {1.0f})
            .set(TypesVirtual::getDoubleObject, 10.0)
            .set(TypesVirtual::getBooleanPrim, true)
            .set(TypesVirtual::getBooleanPrimArray, new boolean[]{true})
            .set(TypesVirtual::getBooleanObject, true)
            .set(TypesVirtual::getCharPrim, 'a')
            .set(TypesVirtual::getCharPrimArray, new char[] {'a'})
            .set(TypesVirtual::getCharObject, 'b')
            .set(TypesVirtual::getString, "string")
            .set(TypesVirtual::getReferenceClass, r1)
            .set(TypesVirtual::getReferenceClassList, list)
            .set(TypesVirtual::getReferenceClassSet, set)
            .set(TypesVirtual::getReferenceClassMap, map);
  }
}
