package cc.blunet.mtg.core;

import static org.apache.commons.lang3.StringUtils.substring;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.io.Files;

import cc.blunet.common.Unchecked;
import cc.blunet.mtg.core.Deck.Card;
import cc.blunet.mtg.core.PrintedDeck.PrintedCard;
import cc.blunet.mtg.db.Db;

public final class DeckFactory {

  private DeckFactory() {}

  private static final Logger LOG = LoggerFactory.getLogger(DeckFactory.class);

  public static PrintedDeck createFrom(Path path) {
    String name = substring(path.getFileName().toString(), 0, -4);
    Multiset<PrintedCard> cards = readDeck(path);
    return new PrintedDeck(name, cards);
  }

  private static final Pattern deckLine = Pattern.compile("^.*\\[(\\w{3})\\].*$");
  private static final Pattern cardLine = Pattern.compile("^\\s*(\\d+)x?\\s+([^\\[]+)(\\s+\\[(\\w{3})\\]\\s*)?$");

  private static Multiset<PrintedCard> readDeck(Path file) {
    try {
      final AtomicReference<Optional<MagicSet>> defaultSet = new AtomicReference<>(Optional.empty());

      // FIXME handle dual deck files...
      ImmutableMultiset.Builder<PrintedCard> result = ImmutableMultiset.builder();
      for (String line : Files.readLines(file.toFile(), Charsets.UTF_8)) {
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
            result.addAll(Collections.nCopies(count, pCard));
          } else {
            // TODO skip headings: (Deck|COMMANDER|(Instant|Sorcery|Creature|Enchantment|Artifact|Land)\s+\(\d+\))
            LOG.warn("Omitting unknown card: {}", cardName);
          }
        } else {
          matcher = deckLine.matcher(line);
          if (matcher.find()) {
            defaultSet.set(Db.readSet(matcher.group(1)));
          } else {
            LOG.info("Omitting non-matching line: {}", line);
          }
        }
      }
      return result.build();
    } catch (IOException ex) {
      Unchecked.rethrow(ex);
      return null; // unreachable
    }
  }
}
