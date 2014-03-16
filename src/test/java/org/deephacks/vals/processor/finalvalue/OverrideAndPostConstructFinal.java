package org.deephacks.vals.processor.finalvalue;

import org.deephacks.vals.FinalValue;

@FinalValue
public interface OverrideAndPostConstructFinal {

  String getValue1();

  String getValue2();

  static void postConstruct(OverrideAndPostConstructFinal o) {
    if (o.getValue1().equals("illegal")) {
      throw new IllegalArgumentException("illegal value1");
    }
  }

  static boolean equals(OverrideAndPostConstructFinal o1, OverrideAndPostConstructFinal o2) {
    return o1.getValue1().equals(o2.getValue1());
  }

  static int hashCode(OverrideAndPostConstructFinal o) {
    return o.getValue1().hashCode();
  }

  static String toString(OverrideAndPostConstructFinal o) {
    return o.getValue1();
  }
}
