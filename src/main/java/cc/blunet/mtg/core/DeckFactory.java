package cc.blunet.mtg.core;

import static cc.blunet.common.util.Paths2.fileName;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.substring;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;

import cc.blunet.common.Unchecked;
import cc.blunet.mtg.core.Deck.Card;
import cc.blunet.mtg.core.PrintedDeck.PrintedCard;
import cc.blunet.mtg.db.Db;

public final class DeckFactory {

  private DeckFactory() {}

  private static final Logger LOG = LoggerFactory.getLogger(DeckFactory.class);

  private static final Pattern section = Pattern.compile("^$|^(Mainboard|Sorcer(y|ies)|(Commander|Instant|" //
      + "|Planeswalker|Creature|Enchantment|Artifact|Land)s?)(\\s+\\(\\d+\\))?$", CASE_INSENSITIVE);
  private static final Pattern deckLine = Pattern.compile("^(\\w[^\\[\\{]+(\\[\\w{3}\\])?)( \\{\\w+\\})?$");
  private static final Pattern cardLine = Pattern.compile("^(\\d+)x?\\s+([^\\[]+)(\\s+\\[(\\w{3})\\])?$");

  // deck separator: -+
  // cards: lines starting with a digit are
  // sections: ((Mainboard|Commander|Instant|Creature|Enchantment|Artifact|Land)s?|Sorcer(y|ies))(\s+\(\d+\))?
  // deck title: \w[^\[{]+
  public static PrintedDeck createFrom(List<String> lines, String defaultName) {
    final AtomicReference<String> name = new AtomicReference<>(defaultName);
    final AtomicReference<Optional<MagicSet>> defaultSet = new AtomicReference<>(Optional.empty());

    ImmutableMultiset.Builder<PrintedCard> cards = ImmutableMultiset.builder();
    for (String line : lines) {
      line = line.trim(); // remove leading & trailing spaces
      Matcher matcher = cardLine.matcher(line);
      if (matcher.find()) {
        int count = Integer.parseInt(matcher.group(1));
        String cardName = matcher.group(2).trim() //
            .replace("Æ", "Ae"); // canonical English Db naming is "Ae"
        Optional<Card> card = Db.readCard(cardName);
        if (card.isPresent()) {
          MagicSet set = Db.readSet(matcher.group(4)) //
              .orElseGet(() -> defaultSet.get().orElseGet(() -> Db.sets(card.get()).first()));
          PrintedCard pCard = new PrintedCard(card.get(), set);
          cards.addAll(Collections.nCopies(count, pCard));
        } else {
          LOG.warn("Omitting unknown card: {}", cardName);
        }
      } else {
        matcher = section.matcher(line);
        if (matcher.find()) {
          // eat line
        } else {
          matcher = deckLine.matcher(line);
          if (matcher.find()) {
            Optional.ofNullable(trimToNull(matcher.group(1))).ifPresent(name::set);
            defaultSet.set(Optional.ofNullable(matcher.group(3)).flatMap(Db::readSet));
          } else {
            // TODO handle Sideboard/Mainboard...
            LOG.info("Omitting non-matching line: {}", line);
          }
        }
      }
    }
    return new PrintedDeck(name.get(), cards.build());
  }

  // TODO extract shit to adapter:

  public static Set<PrintedDeck> createFrom(Path path) {
    try {
      String fileName = substring(fileName(path), 0, -4);
      return splitDecks(readDeckFile(path)).stream()//
          .map(lines -> createFrom(lines, fileName)) //
          .collect(toImmutableSet());
    } catch (IOException ex) {
      Unchecked.rethrow(ex);
      return null; // unreachable
    }
  }

  private static final Pattern basicMd = Pattern.compile("^[#*+-]+\\s"); // '#'-headings and '*'-,'+'-,'-'-lists
  private static final Pattern linkMd = Pattern.compile("\\[(.+)\\]\\(http://[-A-Za-z0-9_.@:/?&=!$'()*+,;~]+\\)");

  public static List<String> readDeckFile(Path path) throws IOException {
    return Files.readAllLines(path, Charsets.UTF_8).stream() //
        .filter(line -> !line.startsWith("by ")) //
        .map(StringEscapeUtils::unescapeHtml4) // handle tappedout.net md files
        .map(line -> basicMd.matcher(line).replaceFirst("")) // remove basic markdown
        .map(line -> linkMd.matcher(line).replaceFirst("$1")) // remove markdown links
        .collect(toList());
  }

  public static List<List<String>> splitDecks(List<String> lines) {
    ImmutableList.Builder<List<String>> result = ImmutableList.builder();
    ImmutableList.Builder<String> part = ImmutableList.builder();
    for (String line : lines) {
      if (line.matches("^[-_*]{3,}$")) {
        result.add(part.build());
        part = ImmutableList.builder();
      } else {
        part.add(line);
      }
    }
    result.add(part.build());
    return result.build();
  }
}
