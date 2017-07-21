package cc.blunet.mtg.db;

import static cc.blunet.common.io.data.JacksonUtils.stream;
import static java.util.stream.Collectors.joining;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.util.StdConverter;

import cc.blunet.mtg.core.Deck.AftermathCard;
import cc.blunet.mtg.core.Deck.Card;
import cc.blunet.mtg.core.Deck.DoubleFacedCard;
import cc.blunet.mtg.core.Deck.FlipCard;
import cc.blunet.mtg.core.Deck.SimpleCard;
import cc.blunet.mtg.core.Deck.SplitCard;

class CardConverter extends StdConverter<JsonNode, Card> {
  public static final Logger LOG = LoggerFactory.getLogger(CardConverter.class);

  @Override
  public Card convert(JsonNode card) {
    String layout = card.path("layout").asText();
    ArrayNode names = (ArrayNode) card.get("names");
    if ("double-faced".equals(layout)) {
      check(names.size() == 2, card);
      return new DoubleFacedCard(//
          new SimpleCard(names.get(0).asText()), //
          new SimpleCard(names.get(1).asText()));
    }
    if ("flip".equals(layout)) {
      check(names.size() == 2, card);
      return new FlipCard(//
          new SimpleCard(names.get(0).asText()), //
          new SimpleCard(names.get(1).asText()));
    }
    if ("split".equals(layout)) {
      check(names.size() == 2, card);
      return new SplitCard(//
          new SimpleCard(names.get(0).asText()), //
          new SimpleCard(names.get(1).asText()));
    }
    if ("aftermath".equals(layout)) {
      check(names.size() == 2, card);
      return new AftermathCard(//
          new SimpleCard(names.get(0).asText()), //
          new SimpleCard(names.get(1).asText()));
    }
    // TODO handle meld cards...
    check(card.get("names") == null, card);
    return new SimpleCard(card.get("name").asText());
  }

  private void check(boolean condition, JsonNode card) {
    if (!condition && card.get("names") != null) {
      LOG.warn("layout: " + card.get("layout").asText() //
          + " ['" + stream(card.get("names")) //
              .map(JsonNode::asText) //
              .collect(joining("','"))
          + "']");
    }

  }
}
