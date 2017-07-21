package cc.blunet.mtg.core;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMultiset.toImmutableMultiset;

import java.util.Objects;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

import cc.blunet.common.BaseEntity;
import cc.blunet.common.ValueObject;
import cc.blunet.mtg.core.Deck.Card;

public final class PrintedDeck extends BaseEntity<String> {

  private final Multiset<PrintedCard> cards;

  public PrintedDeck(String name, Multiset<PrintedCard> cards) {
    super(name);
    this.cards = ImmutableMultiset.copyOf(checkNotNull(cards));
    checkArgument(!cards.isEmpty());
  }

  public String name() {
    return id();
  }

  public Multiset<PrintedCard> cards() {
    return cards;
  }

  public Deck asDeck() {
    return new Deck(name(), cards.stream() //
        .map(PrintedCard::card) //
        .collect(toImmutableMultiset()));
  }

  public static final class PrintedCard extends ValueObject {
    private final Card card;
    private final MagicSet edition;
    private final int variation;

    public PrintedCard(Card card, MagicSet edition, int variation) {
      this.card = checkNotNull(card);
      this.edition = checkNotNull(edition);
      this.variation = variation;
    }

    public Card card() {
      return card;
    }

    public MagicSet edition() {
      return edition;
    }

    public int variation() {
      return variation;
    }

    @Override
    public boolean equals(Object obj) {
      return this == obj //
          || (obj != null //
              && getClass() == obj.getClass() //
              && Objects.equals(card, ((PrintedCard) obj).card) //
              && Objects.equals(edition, ((PrintedCard) obj).edition) //
              && Objects.equals(variation, ((PrintedCard) obj).variation));
    }

    @Override
    public final int hashCode() {
      return Objects.hash(card, edition, variation);
    }
  }
}
