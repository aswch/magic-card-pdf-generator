package cc.blunet.mtg.tools;

import static cc.blunet.common.util.Paths2.fileName;
import static cc.blunet.common.util.Paths2.stripFileSuffix;
import static cc.blunet.mtg.db.Repository.defaultFilters;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.commons.lang3.StringUtils.substring;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import cc.blunet.common.io.compression.ZipArchive;
import cc.blunet.common.util.Collections2;
import cc.blunet.mtg.core.MagicSet;
import cc.blunet.mtg.db.Repository;

/**
 * Extracts Magic the Gathering Images from Set.zip files.
 *
 * @author claude.nobs@blunet.cc
 */
public class ImageSetExtractorApp {

  private static final Logger LOG = LoggerFactory.getLogger(ImageSetExtractorApp.class);

  private static final Pattern endsWithDigit = Pattern.compile("^(.+)(\\d+)$");

  public static void main(String[] args) throws Exception {
    Path source = Paths.get("/", "Users", "bernstein", "XLHQ-Sets-Torrent");
    Path target = source.resolve("_all");

    Repository repo = new Repository(Collections2.concat(defaultFilters(), s -> true /* your filter */));

    // find all set zips
    Pattern setZip = Pattern.compile("^\\w+.zip$");
    Set<String> images = Files.find(source, 1, (p, bfa) -> setZip.matcher(fileName(p)).matches()) //
        .map(p -> mtgSetCode(p)) //
        .collect(toImmutableSet());
    Set<String> sets = repo.sets().stream().map(MagicSet::id).collect(toImmutableSet());

    SetView<String> matching = Sets.intersection(images, sets);
    LOG.info("Extracting : {}", matching);
    SetView<String> noImagesForSet = Sets.difference(sets, images);
    LOG.info("No images found for sets : {}", noImagesForSet);
    SetView<String> noSetForImages = Sets.difference(images, sets);
    LOG.info("No set found for images : {}", noSetForImages);

    matching.stream().sorted().forEach(code -> {
      Path file = source.resolve(code + ".zip");

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

    });
  }

  private static String mtgSetCode(Path path) {
    return stripFileSuffix(fileName(path));
  }
}
