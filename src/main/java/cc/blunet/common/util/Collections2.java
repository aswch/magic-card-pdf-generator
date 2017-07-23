package cc.blunet.common.util;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public final class Collections2 {
  private Collections2() {}

  public static <T> Set<T> set(T... elements) {
    return ImmutableSet.copyOf(elements);
  }
}
