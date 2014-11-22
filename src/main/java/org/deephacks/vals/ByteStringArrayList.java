package org.deephacks.vals;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class ByteStringArrayList extends AbstractList<String> {

  private final List<ByteString> list;

  public ByteStringArrayList() {
    list = new ArrayList<>();
  }

  public ByteStringArrayList(List<String> from) {
    list = new ArrayList<>(from.size());
    for (String s : from) {
      list.add(new ByteString(s));
      modCount++;
    }
  }

  public List<ByteString> byteStrings() {
    return list;
  }

  public boolean add(ByteString value) {
    return list.add(value);
  }


  @Override
  public String get(int index) {
    ByteString o = list.get(index);
    return o.getString();
  }

  @Override
  public int size() {
    return list.size();
  }
}
