package org.deephacks.vals.processor.finalvalue;


import org.deephacks.vals.FinalValue;

@FinalValue
public interface DefaultFinal {

  default String getDefaultValue() {
    return "default";
  }

  String getValue();

  int getInteger();

}
