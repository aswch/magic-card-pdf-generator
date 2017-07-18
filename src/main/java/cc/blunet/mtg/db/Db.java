package cc.blunet.mtg.db;

import static cc.blunet.common.io.data.JacksonUtils.readJsonValue;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;

import cc.blunet.common.Unchecked;
import cc.blunet.mtg.core.Deck.Card;
import cc.blunet.mtg.core.Deck.DoubleFacedCard;
import cc.blunet.mtg.core.MagicSet;

/**
 * Simple Db Singleton from json files.
 *
 * @author claude.nobs@blunet.cc
 */
public final class Db {
  private Db() {}

  private static SetMultimap<Card, MagicSet> cards = null;
  private static Set<MagicSet> sets = null;

  public static Optional<Card> readCard(String name) {
    return cards().keySet().stream()//
        .filter(c -> c.name().equals(name) //
            || (c instanceof DoubleFacedCard && ((DoubleFacedCard) c).backsideName().equals(name))) //
        .findFirst();
  }

  public static Multimap<Card, MagicSet> cards() {
    if (cards == null) {
      cards = loadCards();
    }
    return cards;
  }

  private static SetMultimap<Card, MagicSet> loadCards() {
    ImmutableSetMultimap.Builder<Card, MagicSet> result = ImmutableSetMultimap.builder();
    for (MagicSet set : sets()) {
      for (Card card : set.cards()) {
        result.put(card, set);
      }
    }
    return result //
        .orderKeysBy(Ordering.natural().onResultOf(Card::name)) //
        .orderValuesBy(Ordering.natural().onResultOf(MagicSet::releasedOn)) //
        .build();
  }

  public static SortedSet<MagicSet> sets(Card card) {
    return ImmutableSortedSet.copyOf(Ordering.natural().onResultOf(MagicSet::releasedOn), cards().get(card));
  }

  public static Optional<MagicSet> readSet(String id) {
    return sets().stream().filter(s -> s.id().equals(id)).findFirst();
  }

  public static Set<MagicSet> sets() {
    if (sets == null) {
      sets = loadSets();
    }
    return sets;
  }

  private static Set<MagicSet> loadSets() {
    List<MagicSet> sets = readJsonValue(dataSource(), objectMapper(), new TypeReference<List<MagicSet>>() {});

    return sets.stream() //
        .filter(s -> !s.id().startsWith("p") && !s.id().equals("FRF_UGIN")) //
        .collect(toImmutableSet());
  }

  private static ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule("cc.blunet.mtg.db", new Version(3, 9, 3, null, "com.mtgjson", "AllSetsArray-x"));
    module.addDeserializer(MagicSet.class, new StdDelegatingDeserializer<>(new MagicSetConverter()));
    mapper.registerModule(module);
    return mapper;
  }

  private static Path dataSource() {
    try {
      return Paths.get(Db.class.getResource("/AllSetsArray-x.json.zip").toURI());
    } catch (URISyntaxException ex) {
      Unchecked.rethrow(ex);
      return null; // unreachable
    }
  }
}
