package cc.blunet.mtg.core;

import static cc.blunet.common.util.Collections2.set;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.apache.commons.lang3.StringUtils.substring;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMultiset;

import cc.blunet.mtg.core.PrintedDeck.PrintedCard;
import cc.blunet.mtg.db.Repository;

/**
 * Creates {@link PrintedDeck}s by parsing lines of text.
 *
 * @author claude.nobs@blunet.cc
 */
public final class DeckFactory {

  private static final Logger LOG = LoggerFactory.getLogger(DeckFactory.class);

  private static final Pattern deckLine = Pattern.compile("^(\\w[^\\[\\{]+(\\[\\w{3}\\])?)( \\{\\w+\\})?$");
  private static final Pattern mainboardLine = Pattern.compile("^mainboard$", CASE_INSENSITIVE);
  private static final Pattern sideboardLine = Pattern.compile("^sideboard$", CASE_INSENSITIVE);
  private static final Pattern sectionLine = Pattern.compile("^(Sorcer(y|ies)|(Commander|Instant|" //
      + "|Planeswalker|Creature|Enchantment|Artifact|Land)s?)(\\s+\\(\\d+\\))?$", CASE_INSENSITIVE);
  private static final Pattern cardLine = Pattern.compile("^(\\d+)x?\\s+([^\\[]+)(\\s+\\[(\\w{3,})(\\d+)?\\])?$");

  private final Repository db;

  public DeckFactory(Repository db) {
    this.db = checkNotNull(db);
  }

  // deck title: first line matching \w[^\[{]+
  // cards: lines starting with a digit
  // sections: ((Mainboard|Commander|Instant|Creature|Enchantment|Artifact|Land)s?|Sorcer(y|ies))(\s+\(\d+\))?
  public PrintedDeck createFrom(List<String> lines, String defaultName) {

    String name = defaultName;
    Optional<String> defaultSet = Optional.empty();

    ImmutableMultiset.Builder<PrintedCard> mainboard = ImmutableMultiset.builder();
    ImmutableMultiset.Builder<PrintedCard> sideboard = ImmutableMultiset.builder();
    ImmutableMultiset.Builder<PrintedCard> board = mainboard; // start with main
    for (String line : lines) {
      Matcher matcher = cardLine.matcher(line);
      if (matcher.find()) {
        int count = Integer.parseInt(matcher.group(1));
        String cardName = matcher.group(2).trim();
        Optional<Card> card = readCard(cardName);
        if (card.isPresent()) {
          PrintedCard pCard = printedCard(card.get(), matcher.group(4), matcher.group(5), defaultSet);
          board.addAll(Collections.nCopies(count, pCard));
        } else {
          LOG.warn("Omitting unknown card: {}", cardName);
        }
      } else {
        matcher = sectionLine.matcher(line);
        if (matcher.find()) {
          // eat line
        } else {
          matcher = deckLine.matcher(lines.get(0));
          if (matcher.find()) {
            name = matcher.group(1).trim();
            defaultSet = Optional.ofNullable(substring(matcher.group(2), 1, -1));
          } else if (mainboardLine.matcher(line).find()) {
            board = mainboard;
          } else if (sideboardLine.matcher(line).find()) {
            board = sideboard;
          } else {
            LOG.info("Omitting unrecognized line: {}", line);
          }
        }
      }
    }
    return new PrintedDeck(name, mainboard.build(), sideboard.build());
  }

  private Optional<Card> readCard(String name) {
    String cardName = name //
        .replace("Æ", "Ae") // canonical naming is "Ae"
        .replace("//", "/") // canonical naming is "/"
        .replaceFirst("(\\w)/(\\w)", "$1 / $2"); // canonical naming for split cards
    return db.readCard(cardName);
  }

  private PrintedCard printedCard(Card card, @Nullable String set, @Nullable String var, Optional<String> defaultSet) {
    final AtomicBoolean isDefault = new AtomicBoolean(false);

    // TODO support "custom" sets
    MagicSet magicSet = Optional.ofNullable(set) //
        .flatMap(db::readSet) //
        .orElseGet(() -> defaultSet //
            .flatMap(db::readSet) //
            .orElseGet(() -> {
              isDefault.set(true);
              return defaultSet(card);
            }));

    int variation = Optional.ofNullable(var) //
        .map(Integer::parseUnsignedInt) //
        .map(num -> num - 1) //
        .orElseGet(() -> defaultVariation(card, isDefault.get()));

    int variants = magicSet.cards().count(card);
    checkState(variants > 0, "Card '%s' not in Set '%s'", card, magicSet);
    checkState(variation < variants, "Only %s variants (not %s) of '%s' in '%s'", variants, variation, card, magicSet);

    return new PrintedCard(card, magicSet, variation);
  }

  private MagicSet defaultSet(Card card) {
    if (set("Wastes").contains(card.name())) {
      return db.readSet("OGW").get();
    }
    if (set("Forest", "Island", "Mountain", "Plains", "Swamp").contains(card.name())) {
      return db.readSet("ZEN").get();
    }
    return db.sets(card).last();
  }

  private Integer defaultVariation(Card card, boolean isDefault) {
    if (isDefault) {
      if (set("Wastes").contains(card.name())) {
        return 1;
      }
      if (set("Forest", "Island", "Mountain", "Plains", "Swamp").contains(card.name())) {
        return 5;
      }
    }
    return 0;
  }
}
