package org.deephacks.vals.proto;

import org.deephacks.vals.CompilerUtils;
import org.deephacks.vals.DirectBuffer;
import org.deephacks.vals.Val8;

import java.util.UUID;

public class ProtoTest {
  public static String str;

  public static void main(String[] args) {
    CompilerUtils.compile(Val8.class);
    str = UUID.randomUUID().toString();
    DirectBuffer buffer = new DirectBuffer(new byte[1024 * 1000]);
    for (int i = 0; i < 10; i++) {
      proto(buffer);
      vals(buffer);
    }
  }

  public static void proto(DirectBuffer buffer) {
    System.out.println("proto");
    int total = 0;
    for (int j = 0; j < 10; j++) {
      long now = System.currentTimeMillis();
      for (int i = 0; i < 1000000; i++) {
        byte[] bytes1 = ProtoMessages.Val5Message.newBuilder()
          .setValue(str)
          .setMobileInfo(ProtoMessages.TimeUnit.MINUTES)
          .setId(1).build().toByteArray();
        buffer.putBytes(0, bytes1);
      }
      long took = System.currentTimeMillis() - now;
      total += took;
      System.out.println(took);
    }
    System.out.println("proto AVG " + (float) (10000000 / total));
  }

  public static void vals(DirectBuffer buffer) {
    /*
    System.out.println("vals");
    int total = 0;
    for (int j = 0; j < 10; j++) {
      long now = System.currentTimeMillis();
      for (int i = 0; i < 1000000; i++) {
        Val8 build = new Val8Builder().withId(str).withPInt(1).withTime(TimeUnit.DAYS).build();
        build.writeTo(buffer, 0);
      }
      long took = System.currentTimeMillis() - now;
      System.out.println(took);
      total += took;
    }
    System.out.println("vals AVG " + (float) (10000000 / total));
    */
  }
}
