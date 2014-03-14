package org.deephacks.vals.processor.virtualvalue;

import org.deephacks.vals.VirtualValue;

import javax.annotation.Nullable;

@VirtualValue
public interface NullablePrimitiveVirtual {

  @Nullable
  int getInteger();
}
