package cc.blunet.mtg.core;

import static cc.blunet.common.util.Paths2.fileName;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;
import static org.apache.commons.lang3.StringUtils.substring;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;

import cc.blunet.common.Unchecked;
import cc.blunet.mtg.core.Deck.Card;
import cc.blunet.mtg.core.PrintedDeck.PrintedCard;
import cc.blunet.mtg.db.Db;

public final class DeckFactory {

  private DeckFactory() {}

  private static final Logger LOG = LoggerFactory.getLogger(DeckFactory.class);

  public static Set<PrintedDeck> uncheckedCreateFrom(Path path) {
    try {
      return createFrom(path);
    } catch (IOException ex) {
      Unchecked.rethrow(ex);
      return null; // unreachable
    }
  }

  private static final Pattern section = Pattern.compile("^$|^(Mainboard|Sorcer(y|ies)|(Commander|Instant|" //
      + "|Planeswalker|Creature|Enchantment|Artifact|Land)s?)(\\s+\\(\\d+\\))?$", CASE_INSENSITIVE);
  private static final Pattern deckLine = Pattern.compile("^(\\w[^\\[\\{]+(\\[\\w{3}\\])?)( \\{\\w+\\})?$");
  private static final Pattern cardLine = Pattern.compile("^(\\d+)x?\\s+([^\\[]+)(\\s+\\[(\\w{3})\\])?$");

  private static final Pattern basicMd = Pattern.compile("^[#*-]+\\s");
  private static final Pattern linkMd = Pattern.compile("\\[(.+)\\]\\(http://[-A-Za-z0-9_.@:/?&=!$'()*+,;~]+\\)");

  // deck separator: -+
  // cards: lines starting with a digit are
  // sections: ((Mainboard|Commander|Instant|Creature|Enchantment|Artifact|Land)s?|Sorcer(y|ies))(\s+\(\d+\))?
  // deck title: \w[^\[{]+
  public static Set<PrintedDeck> createFrom(Path path) throws IOException {
    final AtomicReference<String> name = new AtomicReference<>(substring(fileName(path), 0, -4));
    final AtomicReference<Optional<MagicSet>> defaultSet = new AtomicReference<>(Optional.empty());

    ImmutableSet.Builder<PrintedDeck> result = ImmutableSet.builder();
    ImmutableMultiset.Builder<PrintedCard> cards = ImmutableMultiset.builder();
    for (String line : Files.readAllLines(path, Charsets.UTF_8)) {
      line = unescapeHtml4(line) // handle tappedout.net md files
          .trim() // remove leading & trailing spaces
          .replaceFirst("^[#*-]+\\s", "") // remove basic markdown
          .replaceFirst("\\[(.+)\\]\\(http://[-A-Za-z0-9_.@:/?&=!$'()*+,;~]+\\)", "$1") // remove markdown links
      ;
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
        } else if (line.matches("^-+$")) {
          result.add(new PrintedDeck(name.get(), cards.build()));
          cards = ImmutableMultiset.builder();
        } else {
          matcher = deckLine.matcher(line);
          if (!line.startsWith("by") && matcher.find()) {
            Optional.ofNullable(trimToNull(matcher.group(1))).ifPresent(name::set);
            defaultSet.set(Optional.ofNullable(matcher.group(3)).flatMap(Db::readSet));
          } else {
            LOG.info("Omitting non-matching line: {}", line);
          }
        }
      }
    }
    result.add(new PrintedDeck(name.get(), cards.build()));
    return result.build();
  }
}
