package cc.blunet.mtg.core;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

import cc.blunet.common.BaseEntity;

public class Deck extends BaseEntity<String> {

  private final Multiset<Card> mainboard;
  private final Multiset<Card> sideboard;

  public Deck(String name, Multiset<Card> mainboard, Multiset<Card> sideboard) {
    super(name);
    this.mainboard = ImmutableMultiset.copyOf(checkNotNull(mainboard));
    this.sideboard = ImmutableMultiset.copyOf(checkNotNull(sideboard));
    checkArgument(!mainboard.isEmpty());
  }

  public String name() {
    return id();
  }

  public Multiset<Card> mainboard() {
    return mainboard;
  }

  public Multiset<Card> sideboard() {
    return sideboard;
  }

  public Multiset<Card> cards() {
    return ImmutableMultiset.<Card>builder().addAll(mainboard).addAll(sideboard).build();
  }
}
