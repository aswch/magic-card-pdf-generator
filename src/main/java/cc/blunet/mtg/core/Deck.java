package cc.blunet.mtg.core;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

import cc.blunet.common.BaseEntity;

public class Deck extends BaseEntity<String> {

  private final Multiset<Card> cards;

  public Deck(String name, Multiset<Card> cards) {
    super(name);
    this.cards = ImmutableMultiset.copyOf(checkNotNull(cards));
    checkArgument(!cards.isEmpty());
  }

  public String name() {
    return id();
  }

  public Multiset<Card> cards() {
    return cards;
  }
}
