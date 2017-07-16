package cc.blunet.mtg.db;

import static cc.blunet.common.util.Paths2.fileName;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.zip.ZipInputStream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;

import cc.blunet.common.Unchecked;
import cc.blunet.mtg.core.Deck.Card;
import cc.blunet.mtg.core.MagicSet;
import cc.blunet.mtg.core.MagicSetType;

/**
 * Simple Db Singleton from json files.
 *
 * @author claude.nobs@blunet.cc
 */
public final class Db {
  private Db() {}

  private static SetMultimap<Card, MagicSet> cards = null;
  private static Set<MagicSet> sets = null;

  public static Optional<Card> readCard(String id) {
    return cards().keySet().stream().filter(c -> c.id().equals(id)).findFirst();
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
    List<MtgSet> sets = readJsonValue(path("AllSetsArray-x.json.zip"), new TypeReference<List<MtgSet>>() {});

    return sets.stream() //
        .filter(s -> !s.name.startsWith("p") && !s.name.equals("FRF_UGIN")) //
        .map(s -> new MagicSet(s.type, s.code, s.name, s.releaseDate, s.cards)) //
        .collect(toImmutableSet());
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private static final class MtgSet {
    private final MagicSetType type;
    private final String name;
    private final String code;
    private final LocalDate releaseDate;
    private final Set<Card> cards;

    @JsonCreator
    public MtgSet(@JsonProperty("type") String type, @JsonProperty("code") String code, @JsonProperty("name") String name,
        @JsonProperty("releaseDate") String releaseDate, @JsonProperty("cards") List<Map<String, Object>> cards) {
      this.type = type(type);
      this.code = code;
      this.name = name;
      this.releaseDate = LocalDate.parse(releaseDate);
      this.cards = cards.stream().map(c -> new Card((String) c.get("name"))).collect(toImmutableSet());
    }

    private static MagicSetType type(String type) {
      if (type.equals("core") || type.equals("un")) {
        return MagicSetType.CORE;
      } else if (type.equals("expansion")) {
        return MagicSetType.EXPANSION;
      } else if (type.equals("reprint") || type.equals("from the vault")) {
        return MagicSetType.REPRINT;
      } else if (ImmutableSet.of("premium deck", "duel deck", "box", "commander", //
          "planechase", "archenemy", "conspiracy").contains(type)) {
        return MagicSetType.DECK;
      }
      // "starter", ,"promo", "vanguard", "masters", "masterpiece"
      return MagicSetType.OTHER;
    }
  }

  private static Path path(String fileName) {
    try {
      return Paths.get(Db.class.getResource("/" + fileName).toURI());
    } catch (URISyntaxException ex) {
      throw Unchecked.<RuntimeException>cast(ex);
    }
  }

  // - read json TODO extract to separate class

  public static <T> T readJsonValue(Path file, Class<T> type) {
    return readValue(file, new ObjectMapper(), type);
  }

  public static <T> T readJsonValue(Path file, TypeReference<T> type) {
    return readValue(file, new ObjectMapper(), type);
  }

  // read from zipped or plaintext files
  @SuppressWarnings({"rawtypes", "unchecked"})
  private static <T, R> R readValue(Path file, ObjectMapper mapper, T type) {
    try {
      Reader reader = reader(file);

      if (type instanceof TypeReference) {
        return mapper.readValue(reader, (TypeReference) type);
      } else if (type instanceof JavaType) {
        return mapper.readValue(reader, (JavaType) type);
      }
      return (R) mapper.readValue(reader, (Class) type);
    } catch (IOException ex) {
      Unchecked.rethrow(ex);
      return null; // unreachable
    }
  }

  private static Reader reader(Path file) throws IOException, MalformedURLException {
    if (fileName(file).endsWith(".zip")) {
      ZipInputStream zip = new ZipInputStream(new FileInputStream(file.toFile()));
      zip.getNextEntry();
      return new InputStreamReader(zip, StandardCharsets.UTF_8);
    }
    return java.nio.file.Files.newBufferedReader(file, StandardCharsets.UTF_8);
  }
}
