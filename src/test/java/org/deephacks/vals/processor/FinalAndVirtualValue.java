package org.deephacks.vals.processor;

import org.deephacks.vals.FinalValue;
import org.deephacks.vals.VirtualValue;

@FinalValue
public interface FinalAndVirtualValue {

  VirtualAndFinalValue getVirtualAndFinalValue();

  @VirtualValue
  public static interface VirtualAndFinalValue {

    VirtualAndFinalBasicValue getVirtualAndFinalBasicValue();

    @FinalValue
    public static interface VirtualAndFinalBasicValue {
      int getInteger();
    }
  }
}
