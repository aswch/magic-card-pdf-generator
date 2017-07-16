package cc.blunet.mtg.core;

import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDate;
import java.util.Set;

import cc.blunet.common.BaseEntity;
import cc.blunet.mtg.core.Deck.Card;

public final class MagicSet extends BaseEntity<String> {

  private final MagicSetType type;
  private final String name;
  private final LocalDate releasedOn;
  private final Set<Card> cards;

  public MagicSet(MagicSetType type, String id, String name, LocalDate releasedOn, Set<Card> cards) {
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

  public Set<Card> cards() {
    return cards;
  }
}
