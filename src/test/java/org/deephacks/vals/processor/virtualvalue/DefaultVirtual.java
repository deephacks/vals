package org.deephacks.vals.processor.virtualvalue;


import org.deephacks.vals.VirtualValue;

@VirtualValue
public interface DefaultVirtual {

  default String getDefaultValue() {
    return "default";
  }

  String getValue();
}
