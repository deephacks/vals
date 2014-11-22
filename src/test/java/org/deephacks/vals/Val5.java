package org.deephacks.vals;

import java.util.concurrent.TimeUnit;

@Val
public interface Val5 extends Encodable {
  @Id(0) long getValue();
  @Id(1) TimeUnit getTime();
  @Id(2) String getRandom();
}

