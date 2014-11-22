package org.deephacks.vals;


import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.deephacks.vals.ValsUtil.newHashMap;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SerializationTest {

  @Test
  public void testSimplest() throws IOException {
    CompilerUtils.compile(Val7.class);
    Val7 val7 = new CompilerUtils.BuilderProxy.Builder<>(Val7.class)
      .set(Val7::getId, "1")
      .set(Val7::getPInt, 1)
      .set(Val7::getTime, TimeUnit.HOURS)
      .set(Val7::getValues, Arrays.asList("1", "2", "3"))
      .set(Val7::getValueMap, newHashMap().with("1", "2").with("3", "4").build())
      .set(Val7::getLongArray, new long[] {1, 2, 3})
      .build().get();
    Val7 o2 = ValsUtil.decode(Val7.class, val7.toByteArray());
    System.out.println(o2);
    assertThat(o2, is(val7));
  }

  @Test
  public void testSingleSimple() throws IOException {
    CompilerUtils.compile(Val6.class);
    Val6 o1 = ValsUtil.val6("1");
    Val6 o2 = ValsUtil.decode(Val6.class, o1.toByteArray());
    assertThat(o2, is(o1));
  }

  @Test
  public void testSingle() throws IOException {
    CompilerUtils.compile(Val1.class);
    Val1 o1 = ValsUtil.val1("1");
    Val1 o2 = ValsUtil.decode(Val1.class, o1.toByteArray());
    System.out.println(o2);
    assertThat(o2, is(o1));
  }

  @Test
  public void testSimpleNested() throws IOException {
    CompilerUtils.compile(Val3.class, Val4.class);
    Val3 o1 = ValsUtil.val3();
    Val3 o2 = ValsUtil.decode(Val3.class, o1.toByteArray());
    assertThat(o2, is(o1));
  }

  @Test
  public void testNested() throws IOException {
    CompilerUtils.compile(Val1.class, Val2.class);
    Val2 o1 = ValsUtil.val2("2");
    Val2 o2 = ValsUtil.decode(Val2.class, o1.toByteArray());
    assertThat(o2, is(o1));
  }

  @Test
  public void testDecodeMany() throws IOException {
    CompilerUtils.compile(Val1.class, Val2.class);
    DirectBuffer buffer = new DirectBuffer(new byte[1024 * 100]);
    ArrayList<Val2> vals = new ArrayList<>();
    int offset = 0;
    for (int i = 0; i < 10; i++) {
      Val2 val2 = ValsUtil.val2(Integer.toString(i));
      vals.add(val2);
      val2.writeTo(buffer, offset);
      offset += val2.getTotalSize();
    }
    offset = 0;
    for (int i = 0; i < 10; i++) {
      Val2 val2 = ValsUtil.decode(Val2.class, buffer, offset);
      assertThat(val2, is(vals.get(i)));
      offset += val2.getTotalSize();
    }
  }
}
