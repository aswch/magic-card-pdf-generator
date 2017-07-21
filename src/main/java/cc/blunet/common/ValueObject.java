package cc.blunet.common;

public abstract class ValueObject {

  @Override
  public abstract boolean equals(Object obj);

  @Override
  public abstract int hashCode();
}
