package cc.blunet.mtg.core;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Multiset;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class PrintedDeck extends Deck {

  PrintedDeck() {}

  PrintedDeck(String name, Multiset<PrintedCard> cards) {
    super(name, (Multiset) cards);
  }

  // TODO fixme
  public Multiset<PrintedCard> printedCards() {
    return (Multiset) super.cards();
  }

  public static class PrintedCard extends Card {
    private final Card card;
    private final MagicSet edition;
    private final int variation;

      // TODO fixme
    public PrintedCard(Card card, MagicSet edition, int variation) {
      super(card.name());
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
  }

  // - factory

  private static final PrintedDeck EMPTY = new PrintedDeck();

  public static PrintedDeck empty() {
    return EMPTY;
  }

  // TODO fixme
  public static PrintedDeck _of(String name, Multiset<PrintedCard> cards) {
    return new PrintedDeck(name, cards);
  }
}
