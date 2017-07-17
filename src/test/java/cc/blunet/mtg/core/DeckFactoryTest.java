package cc.blunet.mtg.core;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.junit.jupiter.api.Assertions;

public class DeckFactoryTest {
  public static void main(String[] args) throws URISyntaxException {
    DeckFactoryTest test = new DeckFactoryTest();
    test.tappedoutMd();
    test.duelDeck();
  }

  public void tappedoutMd() throws URISyntaxException {
    // given
    Path md = Paths.get(DeckFactory.class.getResource("Who's a Heretic Now.tappedout.md").toURI());

    // when
    Collection<PrintedDeck> decks = DeckFactory.createFrom(md);

    // then
    Assertions.assertTrue(decks.size() == 1);
    decks.forEach(deck -> Assertions.assertTrue(deck.cards().size() == 100));
  }

  public void duelDeck() throws URISyntaxException {
    // given
    Path root = Paths.get(DeckFactory.class.getResource("/decks/").toURI());
    Path dda = root.resolve("DDA - Elves vs. Goblins (2007).txt");

    // when
    Collection<PrintedDeck> decks = DeckFactory.createFrom(dda);

    // then
    Assertions.assertTrue(decks.size() == 2);
    decks.forEach(deck -> Assertions.assertTrue(deck.cards().size() == 60));
  }
}
