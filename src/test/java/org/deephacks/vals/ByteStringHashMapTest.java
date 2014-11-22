package org.deephacks.vals;

import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ByteStringHashMapTest {

  @Test
  public void testStringString() {
    HashMap<String, String> map = new HashMap<>();
    map.put("1", "2");
    map.put("3", "4");
    ByteStringHashMap<String, String> byteStringMap = new ByteStringHashMap<>(map);
    assertThat(byteStringMap.get("1"), is("2"));
    assertThat(byteStringMap.get("3"), is("4"));
    Iterator<Map.Entry<String, String>> iterator = byteStringMap.entrySet().iterator();
    Map<String, String> result = new HashMap<>();
    for (Map.Entry<String, String> e : byteStringMap.entrySet()) {
      result.put(e.getKey(), e.getValue());
    }
    assertThat(result.get("1"), is("2"));
    assertThat(result.get("3"), is("4"));
  }

  @Test
  public void testStringInteger() {
    HashMap<String, Integer> map = new HashMap<>();
    map.put("1", 2);
    map.put("3", 4);
    ByteStringHashMap<String, Integer> byteStringMap = new ByteStringHashMap<>(map);
    assertThat(byteStringMap.get("1"), is(2));
    assertThat(byteStringMap.get("3"), is(4));
    Iterator<Map.Entry<String, Integer>> iterator = byteStringMap.entrySet().iterator();
    Map<String, Integer> result = new HashMap<>();
    for (Map.Entry<String, Integer> e : byteStringMap.entrySet()) {
      result.put(e.getKey(), e.getValue());
    }
    assertThat(result.get("1"), is(2));
    assertThat(result.get("3"), is(4));

  }

  @Test
  public void testIntegerString() {
    HashMap<Integer, String> map = new HashMap<>();
    map.put(1, "2");
    map.put(3, "4");
    ByteStringHashMap<Integer, String> byteStringMap = new ByteStringHashMap<>(map);
    assertThat(byteStringMap.get(1), is("2"));
    assertThat(byteStringMap.get(3), is("4"));
    Iterator<Map.Entry<Integer, String>> iterator = byteStringMap.entrySet().iterator();
    Map<Integer, String> result = new HashMap<>();
    for (Map.Entry<Integer, String> e : byteStringMap.entrySet()) {
      result.put(e.getKey(), e.getValue());
    }
    assertThat(result.get(1), is("2"));
    assertThat(result.get(3), is("4"));
  }

  @Test
  public void testEnumString() {
    HashMap<TimeUnit, String> map = new HashMap<>();
    map.put(TimeUnit.SECONDS, "1");
    map.put(TimeUnit.DAYS, "2");
    ByteStringHashMap<TimeUnit, String> byteStringMap = new ByteStringHashMap<>(map);
    assertThat(byteStringMap.get(TimeUnit.SECONDS), is("1"));
    assertThat(byteStringMap.get(TimeUnit.DAYS), is("2"));
    Map<TimeUnit, String> result = new HashMap<>();
    for (Map.Entry<TimeUnit, String> e : byteStringMap.entrySet()) {
      result.put(e.getKey(), e.getValue());
    }
    assertThat(result.get(TimeUnit.SECONDS), is("1"));
    assertThat(result.get(TimeUnit.DAYS), is("2"));
  }

  @Test
  public void testStringEnum() {
    HashMap<String, TimeUnit> map = new HashMap<>();
    map.put("1", TimeUnit.SECONDS);
    map.put("2", TimeUnit.DAYS);
    ByteStringHashMap<String, TimeUnit> byteStringMap = new ByteStringHashMap<>(map);
    assertThat(byteStringMap.get("1"), is(TimeUnit.SECONDS));
    assertThat(byteStringMap.get("2"), is(TimeUnit.DAYS));
    Map<String, TimeUnit> result = new HashMap<>();
    for (Map.Entry<String, TimeUnit> e : byteStringMap.entrySet()) {
      result.put(e.getKey(), e.getValue());
    }
    assertThat(result.get("1"), is(TimeUnit.SECONDS));
    assertThat(result.get("2"), is(TimeUnit.DAYS));
  }

}
