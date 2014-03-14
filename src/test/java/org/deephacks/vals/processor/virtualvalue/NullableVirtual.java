package org.deephacks.vals.processor.virtualvalue;

import org.deephacks.vals.VirtualValue;

@VirtualValue
public interface NullableVirtual {

  @javax.annotation.Nullable
  String getNullable();

  String getValue();
}
