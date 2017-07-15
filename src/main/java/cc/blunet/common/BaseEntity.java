package cc.blunet.common;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

import com.google.common.base.MoreObjects;

public abstract class BaseEntity<T> implements Entity<T> {

  private final T id;

  public BaseEntity(T id) {
    this.id = checkNotNull(id);
  }

  @Override
  public final T id() {
    return id;
  }

//  public boolean isEqualTo(ST other) {
//    return Objects.equals(id, other.id());
//  }

  @Override
  public final boolean equals(Object obj) {
    return this == obj //
        || (obj != null //
            && getClass() == obj.getClass() //
            && Objects.equals(id, ((BaseEntity<?>) obj).id()));
  }

  @Override
  public final int hashCode() {
    return id.hashCode();
  }

  @Override
  public final String toString() {
    return MoreObjects.toStringHelper(this).addValue(id).toString();
  }
}
