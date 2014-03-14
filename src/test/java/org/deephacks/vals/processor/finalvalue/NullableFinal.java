package org.deephacks.vals.processor.finalvalue;

import org.deephacks.vals.FinalValue;

@FinalValue
public interface NullableFinal {

  @javax.annotation.Nullable
  String getNullable();

  String getValue();
}
