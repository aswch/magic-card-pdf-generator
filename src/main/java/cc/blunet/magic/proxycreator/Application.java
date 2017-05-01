package cc.blunet.magic.proxycreator;

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
import java.util.stream.Stream;

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
import com.google.common.io.Files;

public class Application {


  public static void main(String[] args) throws IOException {
    Path deckRoot = Paths.get("C:", "Users", "ra8v", "Desktop");
    ImmutableList<Path> decks = ImmutableList.of(deckRoot.resolve("C16.txt"));

    Optional<Path> collection = Optional.empty();

    Path imagesPath = Paths.get("C:", "Users", "ra8v", "Desktop");

    new Application().start(decks, collection, imagesPath);
  }

  public void start(Collection<Path> decks, Optional<Path> collectionPath, Path imagesPath) {
    Multiset<String> collection = HashMultiset
        .create(collectionPath.map(c -> readDeck(c)).orElse(Stream.empty()).collect(toList()));

    Collection<String> cards = decks.stream() //
        .flatMap(this::readDeck) //
        .filter(card -> !collection.remove(card)) //
        .collect(toList());

    createPdf(imagesPath, cards);
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
              String card = matcher.group(2);
              return Collections.nCopies(count, card).stream();
            }
            return Stream.<String>empty();
          });
    } catch (IOException ex) {
      Unchecked.rethrow(ex);
    }
    return null;
  }

  private static final float POINTS_PER_MM = 2.834646F;

  private void createPdf(Path imagesPath, Collection<String> cards) {
    List<Float> x = ImmutableList.of(11.0f, 74.0f, 137.0f);
    List<Float> y = ImmutableList.of(16.0f, 104.0f, 192.0f);

    try (final PDDocument document = new PDDocument()) {

      for (List<String> card : Iterables.partition(cards, 9)) {
        final PDPage page = new PDPage(PDRectangle.A4);
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

          for (int i = 0; i < card.size(); i++) {
            // add image to document
            String imagePath = imagesPath.resolve(card.get(i) + ".xlhq.jpg").toString();
            PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, document);
            // place image in page
            contentStream.drawImage(pdImage, //
                x.get(i % 3) * POINTS_PER_MM, //
                y.get(i / 3) * POINTS_PER_MM, //
                63.0f * POINTS_PER_MM, //
                88.0f * POINTS_PER_MM);
          }
        }
        document.addPage(page);
      }
      document.save(imagesPath.resolve("document.pdf").toString());

    } catch (IOException ex) {
      Unchecked.rethrow(ex);
    }
  }
}


