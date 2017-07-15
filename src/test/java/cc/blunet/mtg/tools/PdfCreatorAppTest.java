package cc.blunet.mtg.tools;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class PdfCreatorAppTest {

  public static void main(String[] args) throws URISyntaxException, IOException {
    Path root = Paths.get(PdfCreatorAppTest.class.getResource(".").toURI());

    Path deckPath = root;
    Optional<Path> collection = Optional.empty();
    Path imagesPath = root;
    Path resultPath = root;

    new PdfCreatorApp().run(deckPath, collection, imagesPath, resultPath);
  }
}
