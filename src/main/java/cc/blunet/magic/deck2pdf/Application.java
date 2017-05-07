package cc.blunet.magic.deck2pdf;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

public class Application {

  // create pdf for tokens/emblems: create a decklist!

  public static void main(String[] args) throws IOException {
    // TODO support these as commandline args
    Collection<Path> decks = Sets.newHashSet();
    Optional<Path> collection = Optional.empty();
    Path deckPath = Paths.get("C:", "Users", "ra8v", "Desktop", "mtg", "_decks", "C11");
    Path imagesPath = deckPath; // Paths.get("C:", "Users", "ra8v", "Desktop");
    Path resultPath = deckPath.getParent().resolve(deckPath.getFileName() + ".pdf");

    if (deckPath != null) {
      decks.addAll(java.nio.file.Files //
          .find(deckPath, 1, (path, bfa) -> path.toFile().getName().endsWith(".txt")) //
          .collect(Collectors.toList()));
    }
    new Application().run(decks, collection, imagesPath, resultPath);
  }

  public void run(Collection<Path> decks, Optional<Path> collectionPath, Path imagesPath,
      Path resultPath) throws IOException {
    Multiset<String> collection = HashMultiset
        .create(collectionPath.map(this::readDeck).orElse(Stream.empty()).collect(toList()));

    Collection<String> cards = decks.stream() //
        .flatMap(this::readDeck) //
        .filter(card -> !collection.remove(card)) //
        .collect(toList());

    createPdf(cards, imagesPath, resultPath);
  }

  private final Pattern cardLine = Pattern.compile("^\\s*(\\d+)x?\\s+(.+)$");

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

  private void createPdf(Collection<String> cards, Path imagesPath, Path resultPath) {
    List<Float> x = ImmutableList.of(11.0f, 74.0f, 137.0f);
    List<Float> y = ImmutableList.of(16.0f, 104.0f, 192.0f);

    try (final PDDocument document = new PDDocument()) {

      for (List<String> part : Iterables.partition(cards, 9)) {
        final PDPage page = new PDPage(PDRectangle.A4);
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

          for (int i = 0; i < part.size(); i++) {
            String card = remap(part.get(i)) //
                .replace("\"", "") //
                .replace("//", "-");
            card = StringUtils.stripAccents(card);
            // add image to document
            String imagePath = imagesPath.resolve(card + ".xlhq.jpg").toString();
            PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, document);
            // place image in page
            contentStream.drawImage(pdImage, //
                x.get(i % 3) * POINTS_PER_MM, //
                y.get(i / 3) * POINTS_PER_MM, //
                63.0f * POINTS_PER_MM, //
                88.0f * POINTS_PER_MM);
          }
          // TODO write deckname onto page
        }
        document.addPage(page);
      }
      document.save(resultPath.toString());

    } catch (IOException ex) {
      Unchecked.rethrow(ex);
    }
  }

  // hack for C11/C13/C14/15/16, might not work for other sets
  private String remap(String card) {
    return card.matches("Forest|Island|Mountain|Plains|Swamp") //
        ? card + "1" //
        : card;
  }
}


