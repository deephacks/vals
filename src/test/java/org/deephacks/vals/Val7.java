package org.deephacks.vals;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Val
public interface Val7 extends Encodable {
  @Id(5) String getId();
  @Id(4) int getPInt();
  @Id(3) TimeUnit getTime();
  @Id(2) List<String> getValues();
  @Id(1) Map<String, String> getValueMap();
  @Id(0) long[] getLongArray();
}
