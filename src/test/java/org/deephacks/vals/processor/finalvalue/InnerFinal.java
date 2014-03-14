package org.deephacks.vals.processor.finalvalue;


import org.deephacks.vals.FinalValue;

public class InnerFinal {

  @FinalValue
  public static interface InnerClass1 {
    String getValue();

    @FinalValue
    public static interface InnerClass2 {
      String getValue();
    }

  }
}
