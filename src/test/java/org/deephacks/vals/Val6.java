package org.deephacks.vals;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Val
public interface Val6 extends Encodable {
  @Id(0) String getId();

  @Id(1) byte getPByte();
  @Id(2) byte[] getPByteArray();
  @Id(3) List<Byte> getByteList();
  @Id(4) Map<Byte, Byte> getByteMap();

  @Id(5) String getString();
  @Id(6) List<String> getStringList();
  @Id(7) Map<String, String> getStringMap();

  @Id(8) Map<Integer, String> getIntegerStringMap();
  @Id(9) Map<String, Integer> getStringIntegerMap();
  @Id(10) Map<TimeUnit, String> getEnumStringMap();
  @Id(11) Map<String, TimeUnit> getStringEnumMap();
  @Id(12) EnumSet<TimeUnit> getEnumSet();
}
