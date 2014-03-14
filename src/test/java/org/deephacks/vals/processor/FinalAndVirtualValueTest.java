package org.deephacks.vals.processor;

import org.deephacks.vals.processor.FinalAndVirtualValue.VirtualAndFinalValue;
import org.deephacks.vals.processor.FinalAndVirtualValue.VirtualAndFinalValue.VirtualAndFinalBasicValue;
import org.deephacks.vals.processor.Proxy.Builder;
import org.junit.Before;
import org.junit.Test;

public class FinalAndVirtualValueTest extends BaseTest {


  @Before
  public void setup() {
    compile(FinalAndVirtualValue.class);
  }

  @Test
  public void testCombination() {

    VirtualAndFinalBasicValue vfb = new Builder<>(VirtualAndFinalBasicValue.class, VirtualAndFinalBasicValue.class.getPackage())
            .set(VirtualAndFinalBasicValue::getInteger, 2)
            .build().get();

    VirtualAndFinalValue vf = new Builder<>(VirtualAndFinalValue.class, VirtualAndFinalValue.class.getPackage())
            .set(VirtualAndFinalValue::getVirtualAndFinalBasicValue, vfb)
            .build().get();

    FinalAndVirtualValue fv = new Builder<>(FinalAndVirtualValue.class)
            .set(FinalAndVirtualValue::getVirtualAndFinalValue, vf)
            .build().get();
  }
}
