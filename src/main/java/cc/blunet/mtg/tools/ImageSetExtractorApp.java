package cc.blunet.mtg.tools;

import static cc.blunet.common.util.Paths2.fileName;
import static cc.blunet.common.util.Paths2.stripFileSuffix;
import static cc.blunet.mtg.core.MagicSetType.CORE;
import static cc.blunet.mtg.core.MagicSetType.DECK;
import static cc.blunet.mtg.core.MagicSetType.EXPANSION;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.commons.lang3.StringUtils.substring;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;

import cc.blunet.common.io.compression.ZipArchive;
import cc.blunet.mtg.core.MagicSet;
import cc.blunet.mtg.core.MagicSetType;
import cc.blunet.mtg.db.Db;

/**
 * Extracts Magic the Gathering Images from Set.zip files.
 *
 * @author claude.nobs@blunet.cc
 */
public class ImageSetExtractorApp {

  private static final Pattern endsWithDigit = Pattern.compile("^(.+)(\\d+)$");

  public static void main(String[] args) throws Exception {
    Path source = Paths.get("/", "Users", "bernstein", "XLHQ-Sets-Torrent");
    Path target = source.resolve("_all");

    Set<Path> zips = Files.find(source, 1, mtgSetFileFilter(CORE, EXPANSION, DECK)) //
        .collect(toImmutableSet());

    for (Path file : zips) {
      final String code = mtgSetCode(file);

      ZipArchive.extract(file, path -> {
        String fileName = fileName(path);
        if (fileName.endsWith(".xlhq.jpg")) {
          String cardName = substring(fileName, 0, -9) //
              .replace("Æ", "Ae") //
              .replace("AE", "Ae");

          Matcher m = endsWithDigit.matcher(cardName);
          fileName = m.matches() //
              ? m.replaceFirst("$1." + code + ".$2.jpg") //
              : cardName + "." + code + ".jpg";
        }
        return target.resolve(fileName);
      });
    }
  }

  private static BiPredicate<Path, BasicFileAttributes> mtgSetFileFilter(MagicSetType... types) {
    Set<String> sets = Db.sets().stream()//
        .filter(s -> ImmutableSet.copyOf(types).contains(s.type())) //
        .map(MagicSet::id) //
        .collect(toImmutableSet());

    return (path, bfa) -> fileName(path).endsWith(".zip") && sets.contains(mtgSetCode(path));
  }

  private static String mtgSetCode(Path path) {
    return stripFileSuffix(fileName(path));
  }
}
