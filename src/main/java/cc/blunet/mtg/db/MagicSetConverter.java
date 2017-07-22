package cc.blunet.mtg.db;

import static cc.blunet.common.io.serialization.JacksonUtils.stream;
import static com.google.common.collect.ImmutableMultiset.toImmutableMultiset;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;

import cc.blunet.mtg.core.Card;
import cc.blunet.mtg.core.Card.SimpleCard;
import cc.blunet.mtg.core.MagicSet;
import cc.blunet.mtg.core.MagicSetType;

class MagicSetConverter extends StdConverter<JsonNode, MagicSet> {
  private final CardConverter cardConverter = new CardConverter();

  @Override
  public MagicSet convert(JsonNode value) {
    String type = value.get("type").asText();
    String code = value.get("code").asText();
    String name = value.get("name").asText();
    String releaseDate = value.get("releaseDate").asText();
    Set<String> twinCardNames = new HashSet<>();
    Multiset<Card> cards = stream(value.path("cards")) //
        .map(cardConverter::convert) //
        .filter(c -> c instanceof SimpleCard || twinCardNames.add(c.name())) //
        .collect(toImmutableMultiset());
    return new MagicSet(type(type), code, name, LocalDate.parse(releaseDate), cards);
  }

  private MagicSetType type(String type) {
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
