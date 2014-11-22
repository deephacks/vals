package org.deephacks.vals;

public interface Encodable {
  void writeTo(DirectBuffer buffer, int offset);
  byte[] toByteArray();
  int getTotalSize();
}
