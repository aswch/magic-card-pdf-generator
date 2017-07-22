package cc.blunet.common;

public final class Unchecked {
  private Unchecked() {}

  public static void rethrow(final Throwable checkedException) {
    Unchecked.<RuntimeException>thrownInsteadOf(checkedException);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> void thrownInsteadOf(Throwable t) throws T {
    throw (T) t;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Throwable> T cast(final Throwable checkedException) {
    return (T) checkedException;
  }
}
