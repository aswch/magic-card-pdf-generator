package cc.blunet.mtg.db;

import static cc.blunet.common.util.Paths2.fileName;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Multimaps.flatteningToMultimap;
import static org.apache.commons.lang3.StringUtils.substring;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;

import cc.blunet.common.Unchecked;
import cc.blunet.mtg.core.MagicSet;
import cc.blunet.mtg.core.MagicSetType;

/**
 * Simple Db Singleton from json files.
 *
 * @author claude.nobs@blunet.cc
 */
public final class Db {
  private Db() {}

  private static Multimap<String, MagicSet> cards = null;
  private static Set<MagicSet> sets = null;

  public static Multimap<String, MagicSet> cards() {
    if (cards == null) {
      cards = loadCards();
    }
    return cards;
  }

  @SuppressWarnings("unchecked")
  private static Multimap<String, MagicSet> loadCards() {
    Map<String, Map<String, List<String>>> cards = readJsonValue(path("AllCards-x.json"), Map.class);

    return ImmutableSetMultimap.<String, MagicSet>copyOf(//
        cards.keySet().stream() //
            .collect(flatteningToMultimap(//
                k -> k, //
                k -> cards.get(k).get("printings").stream() //
                    .filter(c -> !c.startsWith("p") && !c.equals("FRF_UGIN")) //
                    .map(c -> m.containsKey(c) ? m.get(c) : c) //
                    .map(MagicSet::valueOf), //
                HashMultimap::create)));
  }

  private static final Map<String, String> m = ImmutableMap.<String, String>builder() //
      .put("DD3_DVD", "DD3") //
      .put("DD3_EVG", "DD3") //
      .put("DD3_GVL", "DD3") //
      .put("DD3_JVC", "DD3") //
      .put("EXP", "ZEN") //
      .put("MPS", "KLD") //
      .put("MPS_AKH", "AKH") //
      .build();

  public static Set<MagicSet> sets() {
    if (sets == null) {
      sets = loadSets();
    }
    return sets;
  }

  private static Set<MagicSet> loadSets() {
    List<MtgSet> sets = readJsonValue(path("SetList.json"), new TypeReference<List<MtgSet>>() {});

    return sets.stream() //
        .map(s -> new MagicSet(MagicSetType.CORE, s.code, s.name, s.releaseDate)) //
        .collect(toImmutableSet());
  }

  private static final class MtgSet {
    private final String name;
    private final String code;
    private final LocalDate releaseDate;

    @JsonCreator
    public MtgSet(@JsonProperty("name") String name, @JsonProperty("code") String code,
        @JsonProperty("releaseDate") String releaseDate) {
      this.name = name;
      this.code = code;
      this.releaseDate = LocalDate.parse(releaseDate);
    }
  }

  private static Path path(String fileName) {
    try {
      return Paths.get(Db.class.getResource("/" + fileName).toURI());
    } catch (URISyntaxException ex) {
      throw Unchecked.<RuntimeException>cast(ex);
    }
  }

  // TODO extract :

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
      URL url = new URL("jar:" + file + "!/" + substring(fileName(file), 0, -4));
      return new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);
    }
    return java.nio.file.Files.newBufferedReader(file, StandardCharsets.UTF_8);
  }

//try {
//  ObjectMapper mapper = new ObjectMapper(new SmileFactory());
//  mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
//  byte[] smile = mapper.writeValueAsBytes(cards);
//  Files.write(smile, Paths.get(Db.class.getResource("/AllCards-x.smile").toURI()).toFile());
//} catch (Exception ex) {
//  throw new RuntimeException(ex);
//}
}
