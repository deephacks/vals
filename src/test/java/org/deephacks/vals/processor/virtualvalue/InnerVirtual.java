package org.deephacks.vals.processor.virtualvalue;


import org.deephacks.vals.VirtualValue;

public class InnerVirtual {

  @VirtualValue
  public static interface InnerClass1 {
    String getValue();

    @VirtualValue
    public static interface InnerClass2 {
      String getValue();
    }

  }
}
