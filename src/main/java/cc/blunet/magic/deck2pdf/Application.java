package cc.blunet.magic.deck2pdf;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.io.Files;

public class Application {

  // create pdf for tokens/emblems: create a decklist!

  // TODO support these as commandline args
  public static void main(String[] args) throws IOException {
    // where to find a list of existing cards
    Optional<Path> collection = Optional.empty();
    // where to search for deck lists
    Path deckPath = Paths.get("/", "Users", "bernstein", "XLHQ-Sets-Torrent", "C11");
    // where to read the images
    Path imagesPath = deckPath; // Paths.get("C:", "Users", "ra8v", "Desktop");
    // where to write the resulting pdf
    Path resultPath = deckPath.getParent().resolve(deckPath.getFileName() + ".pdf");

    new Application().run(deckPath, collection, imagesPath, resultPath);
  }

  private static final Predicate<String> filter = n -> n.endsWith(".txt") && n.equals("Shared.txt");

  public void run(Path deckPath, Optional<Path> collectionPath, Path imagesPath, Path resultPath) throws IOException {
    Deck collection = collectionPath.map(this::createDeck).orElse(Deck.empty());

    Collection<Deck> decks = java.nio.file.Files //
        .find(deckPath, 99, (path, bfa) -> filter.test(path.toFile().getName())) //
        .map(this::createDeck) //
        .collect(toList());

    createPdf(decks, collection, imagesPath, resultPath);
  }

  private static final Pattern cardLine = Pattern.compile("^\\s*(\\d+)x?\\s+(.+)$");

  public Deck createDeck(Path path) {
    String name = StringUtils.substring(path.toFile().getName(), 0, -4);
    Multiset<Card> cards = readDeck(path).map(Card::new) //
        .collect(ImmutableMultiset.toImmutableMultiset());
    return new Deck(name, cards);
  }

  public Stream<String> readDeck(Path file) {
    try {
      List<String> lines = Files.readLines(file.toFile(), Charsets.UTF_8);
      return lines.stream() //
          .flatMap(line -> {
            Matcher matcher = cardLine.matcher(line);
            if (matcher.find()) {
              int count = Integer.valueOf(matcher.group(1));
              String card = matcher.group(2).trim();
              return Collections.nCopies(count, card).stream();
            }
            return Stream.<String>empty();
          });
    } catch (IOException ex) {
      Unchecked.rethrow(ex);
      return null; // unreachable
    }
  }

  private static final float POINTS_PER_MM = 2.834646F;

  private void createPdf(Collection<Deck> decks, Deck collection, Path imagesPath, Path resultPath) {
    List<Float> x = ImmutableList.of(11.0f, 74.0f, 137.0f);
    List<Float> y = ImmutableList.of(16.0f, 104.0f, 192.0f).reverse();

    try (final PDDocument document = new PDDocument()) {
      for (Multimap<Deck, Card> part : paged(decks, collection)) {

        final PDPage page = new PDPage(PDRectangle.A4);
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

          int i = 0;
          for (Card card : part.values()) {
            // add image to document (NOTE: images of same path are only added once)
            String imagePath = imagesPath.resolve(normalize(card.name()) + ".xlhq.jpg").toString();
            PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, document);
            // place image in page
            contentStream.drawImage(pdImage, //
                x.get(i % 3) * POINTS_PER_MM, //
                y.get(i / 3) * POINTS_PER_MM, //
                63.0f * POINTS_PER_MM, //
                88.0f * POINTS_PER_MM);
            i++;
          }
          // write deckname onto page
          String text = part.keySet().stream() //
              .map(deck -> deck.name() + " (" + part.get(deck).size() + ")") //
              .reduce((a, b) -> a + " / " + b) //
              .get();
          drawText(contentStream, 11.0f * POINTS_PER_MM, 285.0f * POINTS_PER_MM, text);
        }
        document.addPage(page);
      }
      document.save(resultPath.toString());

    } catch (IOException ex) {
      Unchecked.rethrow(ex);
    }
  }

  private void drawText(PDPageContentStream content, float x, float y, String text) throws IOException {
    content.setNonStrokingColor(0, 0, 0); // black text
    content.beginText();
    content.setFont(PDType1Font.HELVETICA_BOLD, 12);
    content.newLineAtOffset(x, y);
    content.showText(text);
    content.endText();
  }

  private String normalize(String card) {
    card = StringUtils.stripAccents(card //
        .replace("\"", "") //
        .replace("//", "-"));
    // hack for C11/C13/C14/15/16, might not work for other sets
    return card.matches("Forest|Island|Mountain|Plains|Swamp") //
        ? card + "1" //
        : card;
  }

  // paged list of cards to be printed from given decks, minus those already in given collection
  private List<Multimap<Deck, Card>> paged(Collection<Deck> decks, Deck collection) {
    List<Multimap<Deck, Card>> result = new ArrayList<>();
    Multimap<Deck, Card> page = null;
    int counter = 0;
    for (Deck deck : decks) {
      for (Card card : deck.cards()) {
        if (collection.cards().contains(card)) {
          continue;
        }
        if (counter++ % 9 == 0) {
          page = LinkedListMultimap.create();
          result.add(page);
        }
        page.put(deck, card);
      }
    }
    return result;
  }

  public static final class Deck {
    private static final Deck EMPTY = new Deck("empty", ImmutableMultiset.of());

    private final String name;
    private final Multiset<Card> cards;

    public static Deck empty() {
      return EMPTY;
    }

    public Deck(String name, Multiset<Card> cards) {
      this.name = checkNotNull(name);
      this.cards = ImmutableMultiset.copyOf(checkNotNull(cards));
    }

    public String name() {
      return name;
    }

    public Multiset<Card> cards() {
      return cards;
    }
  }

  public static class Card {
    private final String name;

    public Card(String name) {
      this.name = name;
    }

    public String name() {
      return name;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof Card)) {
        return false;
      }
      return equalTo((Card) obj);
    }

    public boolean equalTo(Card other) {
      return name.equals(other);
    }
  }
}
