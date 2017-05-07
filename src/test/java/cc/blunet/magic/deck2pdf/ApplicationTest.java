package cc.blunet.magic.deck2pdf;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import cc.blunet.magic.deck2pdf.Application;

public class ApplicationTest {

  public static void main(String[] args) throws URISyntaxException, IOException {
    Path root = Paths.get(ApplicationTest.class.getResource(".").toURI());

    ImmutableList<Path> decks = ImmutableList.of(root.resolve("decklist.txt"));
    Optional<Path> collection = Optional.empty();
    Path imagesPath = root;
    Path resultPath = root;

    new Application().run(decks, collection, imagesPath, resultPath);
  }
}
