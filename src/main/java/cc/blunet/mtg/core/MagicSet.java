package cc.blunet.mtg.core;

import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDate;

import com.google.common.collect.Multiset;

import cc.blunet.common.BaseEntity;

public final class MagicSet extends BaseEntity<String> {

  private final MagicSetType type;
  private final String name;
  private final LocalDate releasedOn;
  private final Multiset<Card> cards; // same card may be in set multiple times...

  public MagicSet(MagicSetType type, String id, String name, LocalDate releasedOn, Multiset<Card> cards) {
    super(id);
    this.type = checkNotNull(type);
    this.name = checkNotNull(name);
    this.releasedOn = checkNotNull(releasedOn);
    this.cards = checkNotNull(cards);
  }

  public MagicSetType type() {
    return type;
  }

  public String name() {
    return name;
  }

  public LocalDate releasedOn() {
    return releasedOn;
  }

  public Multiset<Card> cards() {
    return cards;
  }
}
