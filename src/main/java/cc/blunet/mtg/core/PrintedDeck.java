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
    private final MagicSet edition;

    public PrintedCard(Card card, MagicSet edition) {
      // TODO fixme
      super(card.name());
      this.edition = checkNotNull(edition);
    }

    public MagicSet edition() {
      return edition;
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
