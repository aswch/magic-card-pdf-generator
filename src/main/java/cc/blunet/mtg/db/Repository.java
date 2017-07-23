package cc.blunet.mtg.db;

import static cc.blunet.common.io.serialization.JacksonUtils.readJsonValue;
import static cc.blunet.common.util.Collections2.set;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Ordering.natural;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;

import cc.blunet.common.Unchecked;
import cc.blunet.mtg.core.Card;
import cc.blunet.mtg.core.Card.MultiCard;
import cc.blunet.mtg.core.MagicSet;

/**
 * Simple Repository reading data from json file.
 *
 * @author claude.nobs@blunet.cc
 */
public final class Repository {

  private static final Logger LOG = LoggerFactory.getLogger(Repository.class);

  private SetMultimap<Card, MagicSet> cards = null;
  private Set<MagicSet> sets = null;

  private final Collection<Predicate<MagicSet>> filters;

  public Repository(Collection<Predicate<MagicSet>> filters) {
    this.filters = checkNotNull(filters);
  }

  public Optional<Card> readCard(String name) {
    return cards().keySet().stream()//
        .filter(c -> c.id().equals(name) //
            || (c instanceof MultiCard //
                && ((MultiCard) c).cards().stream().anyMatch(cc -> cc.name().equals(name)))) //
        .findFirst();
  }

  public Multimap<Card, MagicSet> cards() {
    if (cards == null) {
      cards = loadCards();
    }
    return cards;
  }

  private SetMultimap<Card, MagicSet> loadCards() {
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

  public SortedSet<MagicSet> sets(Card card) {
    return ImmutableSortedSet.copyOf(Ordering.natural().onResultOf(MagicSet::releasedOn), cards().get(card));
  }

  public Optional<MagicSet> readSet(String id) {
    return sets().stream().filter(s -> s.id().equals(id)).findFirst();
  }

  public Set<MagicSet> sets() {
    if (sets == null) {
      sets = loadSets();
    }
    return sets;
  }

  private Set<MagicSet> loadSets() {
    List<MagicSet> sets = readJsonValue(dataSource(), objectMapper(), new TypeReference<List<MagicSet>>() {});
    Predicate<MagicSet> predicate = filters.stream().reduce(Predicate::and).orElse(t -> false);
    if (LOG.isInfoEnabled()) {
      sets.stream() //
          .sorted(natural().onResultOf(MagicSet::releasedOn)) //
          .forEach(s -> LOG.info("{} [{}] {}{}", s.releasedOn(), s.id(), s.name(), //
              predicate.test(s) ? "" : " (filtered)"));
    }
    return sets //
        .stream() //
        .filter(predicate) //
        .collect(toImmutableSet());
  }

  private static ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule("cc.blunet.mtg.db", //
        new Version(3, 10, 0, null, "com.mtgjson", "AllSetsArray-x"));
    module.addDeserializer(MagicSet.class, new StdDelegatingDeserializer<>(new MagicSetConverter()));
    mapper.registerModule(module);
    return mapper;
  }

  // - Config

  public static Path dataSource() {
    try {
      return Paths.get(Repository.class.getResource("/AllSetsArray-x.json.zip").toURI());
    } catch (URISyntaxException ex) {
      Unchecked.rethrow(ex);
      return null; // unreachable
    }
  }

  public static Collection<Predicate<MagicSet>> defaultFilters() {
    return ImmutableList.of(//
        s -> !set("CED", "CEI").contains(s.id()), // no images (not tournament legal)
        s -> !set("MED", "ME2", "ME3", "ME4", "VMA", "TPR").contains(s.id()), // no images (online only)
        s -> !s.id().equals("MGB"), // no images (all cards also in visions)
        s -> s.releasedOn().isBefore(LocalDate.of(2017, 5, 1)) && !s.id().equals("DDS"), // no images yet
        s -> !s.id().startsWith("p"), // no images/order (promos)
        s -> !s.id().equals("MPS_AKH") // looks like shit
    );
  }
}
