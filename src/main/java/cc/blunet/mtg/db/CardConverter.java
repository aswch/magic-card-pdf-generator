package cc.blunet.mtg.db;

import static cc.blunet.common.io.data.JacksonUtils.stream;
import static java.util.stream.Collectors.joining;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.util.StdConverter;

import cc.blunet.mtg.core.Deck.Card;
import cc.blunet.mtg.core.Deck.DoubleFacedCard;

class CardConverter extends StdConverter<JsonNode, Card> {
  @Override
  public Card convert(JsonNode card) {
    String layout = card.path("layout").asText();
    if ("double-faced".equals(layout)) {
      ArrayNode names = (ArrayNode) card.get("names");
      return new DoubleFacedCard(names.get(0).asText(), names.get(1).asText());
    }
    if ("split".equals(layout)) {
      ArrayNode names = (ArrayNode) card.get("names");
      return new Card(stream(names).map(JsonNode::asText).collect(joining(" / ")));
    }
    return new Card(card.get("name").asText());
  }
}
