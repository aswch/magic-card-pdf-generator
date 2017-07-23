package cc.blunet.mtg.db;

import static cc.blunet.common.io.serialization.JacksonUtils.stream;
import static cc.blunet.common.util.Collections2.set;
import static com.google.common.collect.ImmutableMultiset.toImmutableMultiset;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.StdConverter;
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
    if (type.equals("core")) {
      return MagicSetType.CORE;
    } else if (type.equals("expansion")) {
      return MagicSetType.EXPANSION;
    } else if (set("reprint", "from the vault", "masters", "masterpiece").contains(type)) {
      return MagicSetType.REPRINT;
    } else if (set("duel deck", "premium deck", "box").contains(type)) {
      return MagicSetType.DECK; // regular magic decks
    } else if (set("archenemy", "commander", "planechase").contains(type)) {
      return MagicSetType.DECK; // special format decks
    }
    // "conspiracy" // multiplayer booster draft
    // "vanguard" // special vanguard-format cards only
    // "un" : sets (UNH, UGL) of non-legal cards
    // "starter" : ?
    // "promo" : ?
    return MagicSetType.OTHER;
  }
}
