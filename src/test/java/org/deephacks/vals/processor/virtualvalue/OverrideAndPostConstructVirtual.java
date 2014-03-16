package org.deephacks.vals.processor.virtualvalue;

import org.deephacks.vals.VirtualValue;

@VirtualValue
public interface OverrideAndPostConstructVirtual {

  String getValue1();

  String getValue2();

  static void postConstruct(OverrideAndPostConstructVirtual o) {
    if (o.getValue1().equals("illegal")) {
      throw new IllegalArgumentException("illegal value1");
    }
  }

  static boolean equals(OverrideAndPostConstructVirtual o1, OverrideAndPostConstructVirtual o2) {
    return o1.getValue1().equals(o2.getValue1());
  }

  static int hashCode(OverrideAndPostConstructVirtual o) {
    return o.getValue1().hashCode();
  }

  static String toString(OverrideAndPostConstructVirtual o) {
    return o.getValue1();
  }
}
