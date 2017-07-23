package cc.blunet.mtg.tools;

import static cc.blunet.common.util.Collections2.set;
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


import cc.blunet.common.io.compression.ZipArchive;
import cc.blunet.mtg.core.MagicSet;
import cc.blunet.mtg.core.MagicSetType;
import cc.blunet.mtg.db.Repository;

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

    Repository repo = new Repository();

    Set<Path> zips = Files.find(source, 1, mtgSetFileFilter(repo, set(CORE, EXPANSION, DECK))) //
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

  private static BiPredicate<Path, BasicFileAttributes> mtgSetFileFilter(Repository repo, Set<MagicSetType> types) {
    Set<String> sets = repo.sets().stream() //
        .filter(s -> types.contains(s.type())) //
        .map(MagicSet::id) //
        .collect(toImmutableSet());

    return (path, bfa) -> fileName(path).endsWith(".zip") && sets.contains(mtgSetCode(path));
  }

  private static String mtgSetCode(Path path) {
    return stripFileSuffix(fileName(path));
  }
}
