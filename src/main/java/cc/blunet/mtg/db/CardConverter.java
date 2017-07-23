package cc.blunet.mtg.db;

import static cc.blunet.common.io.serialization.JacksonUtils.stream;
import static cc.blunet.common.util.Logging.toStringSupplier;
import static java.util.stream.Collectors.joining;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import cc.blunet.mtg.core.Card;
import cc.blunet.mtg.core.Card.AftermathCard;
import cc.blunet.mtg.core.Card.DoubleFacedCard;
import cc.blunet.mtg.core.Card.FlipCard;
import cc.blunet.mtg.core.Card.SimpleCard;
import cc.blunet.mtg.core.Card.SplitCard;
import cc.blunet.mtg.core.Card.TokenCard;
import cc.blunet.mtg.core.Card.TwoPartCard;

class CardConverter extends StdConverter<JsonNode, Card> {
  public static final Logger LOG = LoggerFactory.getLogger(CardConverter.class);

  private static final Set<String> whoWhatWhenWhereWhy = ImmutableSet.of("Who", "What", "When", "Where", "Why");
  private static final Map<Integer, String> bigFurryMonster = ImmutableMap.of(9780, "Left", 9844, "Right");

  @Override
  public Card convert(JsonNode card) {
    String layout = card.path("layout").asText();
    String name = card.get("name").asText();
    ArrayNode names = (ArrayNode) card.get("names");
    if ("double-faced".equals(layout)) {
      check(names.size() == 2, layout, names);
      return new DoubleFacedCard(//
          new SimpleCard(names.get(0).asText()), //
          new SimpleCard(names.get(1).asText()));
    }
    if ("flip".equals(layout)) {
      check(names.size() == 2, layout, names);
      return new FlipCard(//
          new SimpleCard(names.get(0).asText()), //
          new SimpleCard(names.get(1).asText()));
    }
    if ("split".equals(layout)) {
      if (whoWhatWhenWhereWhy.contains(name)) {
        return new SplitCard(stream(names).map(JsonNode::asText).map(SimpleCard::new).toArray(SimpleCard[]::new));
      }
      check(names.size() == 2, layout, names);
      return new SplitCard(//
          new SimpleCard(names.get(0).asText()), //
          new SimpleCard(names.get(1).asText()));
    }
    if ("aftermath".equals(layout)) {
      check(names.size() == 2, layout, names);
      return new AftermathCard(//
          new SimpleCard(names.get(0).asText()), //
          new SimpleCard(names.get(1).asText()));
    }
    if ("meld".equals(layout)) {
      check(names.size() == 3, layout, names);
      return new SimpleCard(name);
    }
    if ("token".equals(layout)) {
      // FIXME link tokens to cards, get db of tokens...
      // see https://github.com/mtgjson/mtgjson4
      return new TokenCard(name);
    }
    Integer multiverseId = card.path("multiverseid").asInt();
    if (bigFurryMonster.keySet().contains(multiverseId)) {
      // see https://github.com/mtgjson/mtgjson/issues/356
      return new TwoPartCard(name, bigFurryMonster.get(multiverseId));
    }
    check(card.get("names") == null, layout, names);
    return new SimpleCard(name);
  }

  private void check(boolean condition, String layout, JsonNode names) {
    if (!condition && names != null) {
      LOG.warn("layout: {} {}", layout, toStringSupplier(() -> //
      "['" + stream(names) //
          .map(JsonNode::asText) //
          .collect(joining("','")) + "']"));
    }
  }
}
