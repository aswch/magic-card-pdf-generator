package cc.blunet.mtg.core;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.io.Files;

import cc.blunet.common.Unchecked;

public final class Deck {
  private final String name;
  private final Multiset<Card> cards;

  private Deck(String name, Multiset<Card> cards) {
    this.name = checkNotNull(name);
    this.cards = ImmutableMultiset.copyOf(checkNotNull(cards));
  }

  public String name() {
    return name;
  }

  public Multiset<Card> cards() {
    return cards;
  }

  // - types

  public static class Card {
    private final String name;

    public Card(String name) {
      this.name = name;
    }

    public String name() {
      return name;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof Card)) {
        return false;
      }
      return equalTo((Card) obj);
    }

    public boolean equalTo(Card other) {
      return name.equals(other);
    }
  }

  // - factory

  private static final Deck EMPTY = new Deck("empty", ImmutableMultiset.of());

  public static Deck empty() {
    return EMPTY;
  }

  public static Deck of(String name, Multiset<Card> cards) {
    checkArgument(!cards.isEmpty());
    return new Deck(name, cards);
  }

  public static Deck createFrom(Path path) {
    String name = StringUtils.substring(path.toFile().getName(), 0, -4);
    Multiset<Card> cards = readDeck(path).map(Card::new) //
        .collect(ImmutableMultiset.toImmutableMultiset());
    return new Deck(name, cards);
  }

  private static final Pattern cardLine = Pattern.compile("^\\s*(\\d+)x?\\s+(.+)$");

  private static Stream<String> readDeck(Path file) {
    try {
      List<String> lines = Files.readLines(file.toFile(), Charsets.UTF_8);
      return lines.stream() //
          .flatMap(line -> {
            Matcher matcher = cardLine.matcher(line);
            if (matcher.find()) {
              int count = Integer.valueOf(matcher.group(1));
              String card = matcher.group(2).trim();
              return Collections.nCopies(count, card).stream();
            }
            return Stream.<String>empty();
          });
    } catch (IOException ex) {
      Unchecked.rethrow(ex);
      return null; // unreachable
    }
  }
}
