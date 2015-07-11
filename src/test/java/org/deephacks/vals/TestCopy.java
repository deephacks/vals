package org.deephacks.vals;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class TestCopy {

  @Test
  public void testCopy() {
    CompilerUtils.compile(Val3.class, Val4.class);
    Val3 o1 = ValsUtil.val3();
    Val3 o2 = ValsUtil.decode(Val3.class, o1.toByteArray());
    Val3 o3 = ValsUtil.decode(Val3.class, o2.toByteArray());
    assertThat(o3, is(o1));
  }
}
