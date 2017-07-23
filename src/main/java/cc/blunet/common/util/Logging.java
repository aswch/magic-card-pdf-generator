package cc.blunet.common.util;

import java.util.function.Supplier;

public final class Logging {
  private Logging() {}

  public static Object toStringSupplier(Supplier<String> s) {
    return new Object() {
      @Override
      public String toString() {
        return s.get();
      }
    };
  }
}
