package org.deephacks.vals;

import java.util.*;

public class ByteStringHashMap<K, V> extends AbstractMap<K, V> {

  private final Map<MaybeByteString, MaybeByteString> map;

  public ByteStringHashMap() {
    map = new HashMap<>();
  }

  public ByteStringHashMap(Map<? extends K, ? extends V> from) {
    map = new HashMap<>();
    for (Entry<? extends K, ? extends V> e : from.entrySet()) {
      map.put(new MaybeByteString(e.getKey()), new MaybeByteString(e.getValue()));
    }
  }

  public Object put(Object key, Object value) {
    MaybeByteString v = map.put(new MaybeByteString(key), new MaybeByteString(value));
    if (v == null) {
      return null;
    }
    if (v.isByteString()) {
      return v.getByteString();
    }
    return v.getObject();
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    HashSet<Entry<K, V>> set = new HashSet<>();
    for (Entry<MaybeByteString, MaybeByteString> e : map.entrySet()) {
      set.add(new MaybeByteStringEntry<K, V>(e));
    }
    return set;
  }

  @Override
  public V get(Object key) {
    MaybeByteString value = map.get(new MaybeByteString(key));
    if (value != null && value.isByteString()) {
      return (V) value.getByteString().getString();
    } else if (value != null) {
      return (V) value.getObject();
    }
    return null;
  }

  public Set<ByteString> byteStringKeySet() {
    final Iterator<MaybeByteString> it = map.keySet().iterator();
    return new AbstractSet<ByteString>() {
      @Override
      public Iterator<ByteString> iterator() {

        return new Iterator<ByteString>() {
          @Override
          public boolean hasNext() {
            return it.hasNext();
          }

          @Override
          public ByteString next() {
            return it.next().getByteString();
          }
        };
      }

      @Override
      public int size() {
        return map.size();
      }
    };
  }

  public ByteString getByteString(Object object) {
    MaybeByteString value = map.get(new MaybeByteString(object));
    return value.getByteString();
  }

  static class MaybeByteStringEntry<K, V> extends SimpleEntry<K, V> {

    public MaybeByteStringEntry(Entry<MaybeByteString, MaybeByteString> e) {
      super((Entry<K, V>) e);
    }

    @Override
    public K getKey() {
      MaybeByteString key = (MaybeByteString) super.getKey();
      if (key.isByteString()) {
        return (K) key.getByteString().getString();
      }
      return (K) key.getObject();
    }

    @Override
    public V getValue() {
      MaybeByteString key = (MaybeByteString) super.getValue();
      if (key.isByteString()) {
        return (V) key.getByteString().getString();
      }
      return (V) key.getObject();
    }
  }

  static class MaybeByteString {
    Object object;

    public MaybeByteString(Object object) {
      if (object instanceof String) {
        this.object = new ByteString((String) object);
      } else {
        this.object = object;
      }
    }

    public Object getObject() {
      return object;
    }

    public boolean isByteString() {
      return object instanceof ByteString;
    }

    public ByteString getByteString() {
      return (ByteString) object;
    }

    @Override
    public String toString() {
      return String.valueOf(object);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      MaybeByteString that = (MaybeByteString) o;

      if (object != null ? !object.equals(that.object) : that.object != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return object != null ? object.hashCode() : 0;
    }
  }
}
