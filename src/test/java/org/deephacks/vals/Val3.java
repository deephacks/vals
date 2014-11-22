package org.deephacks.vals;

import java.util.List;

@Val
public interface Val3 extends Encodable {
  @Id(0) Val4 getVal4();
  @Id(1) List<Val4> getVal4List();

}
