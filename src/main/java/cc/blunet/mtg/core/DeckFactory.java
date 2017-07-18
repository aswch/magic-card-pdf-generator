package cc.blunet.mtg.core;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.apache.commons.lang3.StringUtils.substring;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMultiset;

import cc.blunet.mtg.core.Deck.Card;
import cc.blunet.mtg.core.PrintedDeck.PrintedCard;
import cc.blunet.mtg.db.Db;

public final class DeckFactory {

  private static final Logger LOG = LoggerFactory.getLogger(DeckFactory.class);

  private static final Pattern section = Pattern.compile("^$|^(Mainboard|Sorcer(y|ies)|(Commander|Instant|" //
      + "|Planeswalker|Creature|Enchantment|Artifact|Land)s?)(\\s+\\(\\d+\\))?$", CASE_INSENSITIVE);
  private static final Pattern deckLine = Pattern.compile("^(\\w[^\\[\\{]+(\\[\\w{3}\\])?)( \\{\\w+\\})?$");
  private static final Pattern cardLine = Pattern.compile("^(\\d+)x?\\s+([^\\[]+)(\\s+\\[(\\w{3})\\])?$");

  private final Db db;

  public DeckFactory(Db db) {
    this.db = checkNotNull(db);
  }

  // deck separator: -+
  // cards: lines starting with a digit are
  // sections: ((Mainboard|Commander|Instant|Creature|Enchantment|Artifact|Land)s?|Sorcer(y|ies))(\s+\(\d+\))?
  // deck title: \w[^\[{]+
  public PrintedDeck createFrom(List<String> lines, String defaultName) {
    final AtomicReference<String> name = new AtomicReference<>(defaultName);
    final AtomicReference<Optional<MagicSet>> defaultSet = new AtomicReference<>(Optional.empty());

    ImmutableMultiset.Builder<PrintedCard> cards = ImmutableMultiset.builder();
    for (String line : lines) {
      line = line.trim(); // remove leading & trailing spaces
      Matcher matcher = cardLine.matcher(line);
      if (matcher.find()) {
        int count = Integer.parseInt(matcher.group(1));
        String cardName = matcher.group(2).trim() //
            .replace("Æ", "Ae") // canonical naming is "Ae"
            .replace("//", "/") // canonical naming is "/"
            .replaceFirst("(\\w)/(\\w)", "$1 / $2"); // canonical naming for split cards
        Optional<Card> card = db.readCard(cardName);
        if (card.isPresent()) {
          MagicSet set = db.readSet(matcher.group(4)) //
              .orElseGet(() -> defaultSet.get().orElseGet(() -> db.sets(card.get()).last()));
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
            defaultSet.set(Optional.ofNullable(matcher.group(2)) //
                .flatMap(s -> db.readSet(substring(s, 1, -1))));
          } else {
            // TODO handle Sideboard/Mainboard...
            LOG.info("Omitting non-matching line: {}", line);
          }
        }
      }
    }
    return new PrintedDeck(name.get(), cards.build());
  }
}
