package cc.blunet.mtg.core;

import static com.google.common.collect.ImmutableMultiset.toImmutableMultiset;
import static com.google.common.collect.ImmutableSortedSet.toImmutableSortedSet;
import static org.apache.commons.lang3.StringUtils.substring;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.common.base.Charsets;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;

import cc.blunet.common.Unchecked;
import cc.blunet.mtg.core.Deck.Card;
import cc.blunet.mtg.core.PrintedDeck.PrintedCard;

public class DeckFactory {

  SortedSet<MagicSet> sets = MagicSet.values().stream() //
      .filter(s -> s.type() == MagicSetType.DECK) //
      .collect(toImmutableSortedSet(Ordering.natural().onResultOf(MagicSet::releasedOn)));

  public static Deck createFrom(Path path) {
    String name = substring(path.getFileName().toString(), 0, -4);
    Multiset<Card> cards = readDeck(path);
    return new Deck(name, cards);
  }

  private static final Pattern cardLine = Pattern.compile("^\\s*(\\d+)x?\\s+([^\\[]+)(\\s+\\[(\\w{3})\\]\\s*)?$");

  private static Multiset<Card> readDeck(Path file) {
    try {
      List<String> lines = Files.readLines(file.toFile(), Charsets.UTF_8);
      return lines.stream() //
          .flatMap(line -> {
            Matcher matcher = cardLine.matcher(line);
            if (matcher.find()) {
              int count = Integer.parseInt(matcher.group(1));
              String name = matcher.group(2).trim();
              MagicSet set = MagicSet.valueOf(matcher.group(4));
              Card card = set == null ? new Card(name) : new PrintedCard(name, set);
              return Collections.nCopies(count, card).stream();
            }
            return Stream.<Card>empty();
          })//
          .collect(toImmutableMultiset());
    } catch (IOException ex) {
      Unchecked.rethrow(ex);
      return null; // unreachable
    }
  }
}
