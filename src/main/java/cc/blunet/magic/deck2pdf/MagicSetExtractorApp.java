package cc.blunet.magic.deck2pdf;

import static cc.blunet.magic.deck2pdf.MagicSetType.CORE;
import static cc.blunet.magic.deck2pdf.MagicSetType.EXPANSION;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.substring;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;

import cc.blunet.common.Unchecked;
import cc.blunet.common.io.compression.SevenZipExtractor;

/**
 * Extracts Magic the Gathering Images from Set.zip files.
 *
 * @author claude.nobs@blunet.cc
 */
public class MagicSetExtractorApp {

  private static final Pattern endsWithDigit = Pattern.compile("^(.+)(\\d+)$");

  public static void main(String[] args) throws Exception {
    Path source = Paths.get("/", "Users", "bernstein", "XLHQ-Sets-Torrent");
    Path target = source.resolve("_all");

    Set<Path> zips = java.nio.file.Files.find(source, 1, mtgSetFilter(CORE, EXPANSION)).collect(toSet());

    for (Path file : zips) {
      final String code = mtgSetCode(file);

      SevenZipExtractor.extract(file, path -> {
        String cardName = cardName(path);

        Matcher m = endsWithDigit.matcher(cardName);
        String fileName = m.matches() //
            ? m.replaceFirst("$1." + code + ".$2.jpg") //
            : cardName + "." + code + ".jpg";

        return target.resolve(fileName);
      });
    }
  }

  private static BiPredicate<Path, BasicFileAttributes> mtgSetFilter(MagicSetType... types) {
    Set<String> sets = MagicSet.values().stream()//
        .filter(s -> ImmutableSet.copyOf(types).contains(s.type())) //
        .map(MagicSet::id) //
        .collect(toImmutableSet());

    return (path, bfa) -> fileName(path).endsWith(".zip") && sets.contains(mtgSetCode(path));
  }

  private static String cardName(Path path) {
    String fileName = fileName(path);
    if (fileName.endsWith(".xlhq.jpg")) {
      return substring(fileName, 0, -9);
    }
    throw Unchecked.cast(new IOException("Unexpected fileName: " + path));
  }

  private static String mtgSetCode(Path path) {
    return stripFileSuffix(fileName(path));
  }

  private static String fileName(Path file) {
    return file.getFileName().toString();
  }

  private static String stripFileSuffix(String fileName) {
    return substring(fileName, 0, -4);
  }
}
