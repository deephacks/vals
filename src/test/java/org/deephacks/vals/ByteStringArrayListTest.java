package org.deephacks.vals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ByteStringArrayListTest {

  @Test
  public void testString() {
    ArrayList<String> list = new ArrayList<>();
    list.addAll(Arrays.asList("1", "2"));

    ByteStringArrayList byteStringMap = new ByteStringArrayList(list);
    assertThat(byteStringMap.size(), is(2));
    assertThat(byteStringMap.get(0), is("1"));
    assertThat(byteStringMap.get(1), is("2"));
    Iterator<String> iterator = byteStringMap.iterator();
    assertThat(iterator.next(), is("1"));
    assertThat(iterator.next(), is("2"));
  }
}
