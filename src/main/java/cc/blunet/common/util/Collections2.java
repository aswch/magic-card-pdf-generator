package cc.blunet.common.util;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public final class Collections2 {
  private Collections2() {}

  @SafeVarargs
  public static <T> Set<T> set(T... elements) {
    return ImmutableSet.copyOf(elements);
  }

  @SafeVarargs
  public static <T> List<T> list(T... elements) {
    return ImmutableList.copyOf(elements);
  }

  @SafeVarargs
  public static <T> Collection<T> concat(Collection<T> colleciton, T... elements) {
    return ImmutableList.<T>builder().addAll(colleciton).add(elements).build();
  }
}
