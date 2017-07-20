package cc.blunet.mtg.core;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

import cc.blunet.common.BaseEntity;

public class Deck extends BaseEntity<String> {

  private final Multiset<Card> cards;

  Deck() {
    super("empty");
    cards = ImmutableMultiset.of();
  }

  Deck(String name, Multiset<Card> cards) {
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

  // - types

  public static class Card extends BaseEntity<String> {

    public Card(String name) {
      super(name);
    }

    public String name() {
      return id();
    }
  }

  // TODO do this for split cards as well...
  public static class DoubleFacedCard extends Card {
    private final Card front;
    private final Card back;

    public DoubleFacedCard(Card front, Card back) {
      super(front.id() + " / " + back.id());
      this.front = checkNotNull(front);
      this.back = checkNotNull(back);
    }

    @Override
    public String name() {
      return front.name();
    }

    public Card back() {
      return back;
    }
  }

  // - factory

  private static final Deck EMPTY = new Deck();

  public static Deck empty() {
    return EMPTY;
  }

  public static Deck of(String name, Multiset<Card> cards) {
    return new Deck(name, cards);
  }
}
