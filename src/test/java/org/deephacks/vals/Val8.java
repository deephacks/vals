package org.deephacks.vals;

import java.util.concurrent.TimeUnit;

@Val
public interface Val8 extends Encodable {
  @Id(0) String getId();
  @Id(1) int getPInt();
  @Id(2) TimeUnit getTime();
}
