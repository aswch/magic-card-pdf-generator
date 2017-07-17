package cc.blunet.mtg.tools;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import cc.blunet.mtg.core.Deck;
import cc.blunet.mtg.core.DeckFactory;
import cc.blunet.mtg.core.PrintedDeck;

public class PdfCreatorAppTest {

  public static void main(String[] args) throws URISyntaxException, IOException {
    // given
    Path root = Paths.get(PdfCreatorAppTest.class.getResource(".").toURI());
    Collection<PrintedDeck> decks = Arrays.asList(DeckFactory.createFrom(root.resolve("decklist.txt")));
    Optional<Deck> collection = Optional.empty();
    Path imagesPath = root;
    Path resultPath = root;

    // when
    new PdfCreatorApp().createPdf(decks, collection, imagesPath, resultPath);

    // then
  }
}
