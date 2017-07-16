package cc.blunet.mtg.core;

import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDate;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import cc.blunet.common.BaseEntity;

public final class MagicSet extends BaseEntity<String> {

  public static MagicSet valueOf(String code) {
    try {
      return values().stream() //
          .filter(s -> s.id().equals(code)) //
          .findAny().get();
    } catch (NullPointerException ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  public static Set<MagicSet> values() {
    return VALUES;
  }

  private static final Set<MagicSet> VALUES = ImmutableSet.of(//
      // CORE
      core("LEA", "Limited Edition Alpha", date(1993, 8, 5)), //
      core("LEB", "Limited Edition Beta", date(1993, 10, 1)), //
      core("2ED", "Unlimited Edition", date(1993, 12, 1)), //
      core("3ED", "Revised Edition", date(1994, 4, 1)), //
      core("4ED", "Fourth Edition", date(1995, 4, 1)), //
      core("5ED", "Fifth Edition", date(1997, 3, 24)), //
      core("6ED", "Classic Sixth Edition", date(1999, 4, 28)), //
      core("7ED", "Seventh Edition", date(2001, 4, 11)), //
      core("8ED", "Eighth Edition", date(2003, 7, 28)), //
      core("9ED", "Ninth Edition", date(2005, 7, 29)), //
      core("10E", "Tenth Edition", date(2007, 7, 13)), //
      core("M10", "Magic 2010", date(2009, 7, 17)), //
      core("M11", "Magic 2011", date(2010, 7, 16)), //
      core("M12", "Magic 2012", date(2011, 7, 15)), //
      core("M13", "Magic 2013", date(2012, 7, 13)), //
      core("M14", "Magic 2014", date(2013, 7, 19)), //
      core("M15", "Magic 2015", date(2014, 7, 18)), //
      core("ORI", "Magic Origins", date(2015, 7, 17)), //

      // EXPANSION
      exp("ARN", "Arabian Nights", date(1993, 12, 1)), //
      exp("ATQ", "Antiquities", date(1994, 3, 1)), //
      exp("LEG", "Legends", date(1994, 6, 1)), //
      exp("DRK", "The Dark", date(1994, 8, 1)), //
      exp("FEM", "Fallen Empires", date(1994, 11, 1)), //
      exp("ICE", "Ice Age", date(1995, 6, 1)), //
      exp("HML", "Homelands", date(1995, 10, 1)), //
      exp("ALL", "Alliances", date(1996, 6, 10)), //
      // Mirage Block
      exp("MIR", "Mirage", date(1996, 10, 7)), //
      exp("VIS", "Visions", date(1997, 2, 3)), //
      exp("WTH", "Weatherlight", date(1997, 6, 9)), //
      // Rath Cycle or Tempest Block
      exp("TMP", "Tempest", date(1997, 10, 13)), //
      exp("STH", "Stronghold", date(1998, 3, 2)), //
      exp("EXO", "Exodus", date(1998, 6, 15)), //
      // Artifacts Cycle or Urza Block
      exp("USG", "Urza's Saga", date(1998, 10, 12)), //
      exp("ULG", "Urza's Legacy", date(1999, 2, 15)), //
      exp("UDS", "Urza's Destiny", date(1999, 6, 7)), //
      // Masquerade Cycle or Masques Block
      exp("MMQ", "Mercadian Masques", date(1999, 10, 4)), //
      exp("NMS", "Nemesis", date(2000, 2, 14)), //
      exp("PCY", "Prophecy", date(2000, 6, 5)), //
      // Invasion Block
      exp("INV", "Invasion", date(2000, 10, 2)), //
      exp("PLS", "Planeshift", date(2001, 2, 5)), //
      exp("APC", "Apocalypse", date(2001, 6, 4)), //
      // Odyssey Block
      exp("ODY", "Odyssey", date(2001, 10, 1)), //
      exp("TOR", "Torment", date(2002, 2, 4)), //
      exp("JUD", "Judgment", date(2002, 5, 27)), //
      // Onslaught Block
      exp("ONS", "Onslaught", date(2002, 10, 7)), //
      exp("LGN", "Legions", date(2003, 2, 3)), //
      exp("SCG", "Scourge", date(2003, 5, 26)), //
      // Mirrodin Block
      exp("MRD", "Mirrodin", date(2003, 10, 3)), //
      exp("DST", "Darksteel", date(2004, 2, 6)), //
      exp("5DN", "Fifth Dawn", date(2004, 6, 4)), //
      // Kamigawa Block
      exp("CHK", "Champions of Kamigawa", date(2004, 10, 1)), //
      exp("BOK", "Betrayers of Kamigawa", date(2005, 2, 4)), //
      exp("SOK", "Saviors of Kamigawa", date(2005, 6, 3)), //
      // Ravnica Block
      exp("RAV", "Ravnica: City of Guilds", date(2005, 10, 7)), //
      exp("GPT", "Guildpact", date(2006, 2, 3)), //
      exp("DIS", "Dissension", date(2006, 5, 5)), //
      // Coldsnap
      exp("CSP", "Coldsnap", date(2006, 7, 21)), //
      // Time Spiral Block
      exp("TSP", "Time Spiral", date(2006, 10, 6)), //
      exp("TSB", "Time Spiral Bonus", date(2006, 10, 6)), //
      exp("PLC", "Planar Chaos", date(2007, 2, 2)), //
      exp("FUT", "Future Sight", date(2007, 5, 4)), //
      // Lorwyn Block
      exp("LRW", "Lorwyn", date(2007, 10, 12)), //
      exp("MOR", "Morningtide", date(2008, 2, 1)), //
      exp("SHM", "Shadowmoor Block", date(2008, 5, 2)), //
      exp("EVE", "Eventide", date(2008, 7, 25)), //
      // Alara Block
      exp("ALA", "Shards of Alara", date(2008, 10, 3)), //
      exp("CON", "Conflux", date(2009, 2, 6)), //
      exp("ARB", "Alara Reborn", date(2009, 4, 30)), //
      // Zendikar Block
      exp("ZEN", "Zendikar", date(2009, 10, 2)), //
      exp("WWK", "Worldwake", date(2010, 2, 5)), //
      exp("ROE", "Rise of the Eldrazi", date(2010, 4, 23)), //
      // Scars of Mirrodin Block
      exp("SOM", "Scars of Mirrodin", date(2010, 10, 1)), //
      exp("MBS", "Mirrodin Besieged", date(2011, 2, 4)), //
      exp("NPH", "New Phyrexia", date(2011, 5, 13)), //
      // Innistrad Block
      exp("ISD", "Innistrad", date(2011, 9, 30)), //
      exp("DKA", "Dark Ascension", date(2012, 2, 3)), //
      exp("AVR", "Avacyn Restored", date(2012, 5, 4)), //
      // Return to Ravnica Block
      exp("RTR", "Return to Ravnica", date(2012, 10, 5)), //
      exp("GTC", "Gatecrash", date(2013, 2, 1)), //
      exp("DGM", "Dragon's Maze", date(2013, 5, 3)), //
      // Theros Block
      exp("THS", "Theros", date(2013, 9, 27)), //
      exp("BNG", "Born of the Gods", date(2014, 2, 7)), //
      exp("JOU", "Journey into Nyx", date(2014, 5, 2)), //
      // Khans of Tarkir Block
      exp("KTK", "Khans of Tarkir", date(2014, 9, 26)), //
      exp("FRF", "Fate Reforged", date(2015, 1, 23)), //
      exp("DTK", "Dragons of Tarkir", date(2015, 3, 27)), //
      // Battle for Zendikar Block
      exp("BFZ", "Battle for Zendikar", date(2015, 10, 2)), //
      exp("OGW", "Oath of the Gatewatch", date(2016, 1, 22)), //
      // Shadows over Innistrad Block
      exp("SOI", "Shadows over Innistrad", date(2016, 4, 8)), //
      exp("EMN", "Eldritch Moon", date(2016, 7, 22)), //
      // Kaladesh Block
      exp("KLD", "Kaladesh", date(2016, 9, 30)), //
      exp("AER", "Aether Revolt", date(2017, 1, 20)), //
      // Amonkhet Block
      exp("AKH", "Amonkhet", date(2017, 4, 28)), //
      exp("HOU", "Hour of Devastation", date(2017, 7, 14)), //

      // REPRINT
      reprint("CHR", "Chronicles", date(1995, 7, 1)), //
      // Masters Series
      reprint("MMA", "Modern Masters", date(2013, 6, 7)), //
      reprint("MM2", "Modern Masters 2015 Edition", date(2015, 5, 22)), //
      reprint("EMA", "Eternal Masters", date(2016, 6, 10)), //
      reprint("MM3", "Modern Masters 2017 Edition", date(2017, 3, 17)), //
      // From the Vault
      reprint("DRB", "From the Vault: Dragons", date(2008, 8, 29)), //
      reprint("V09", "From the Vault: Exiled", date(2009, 8, 28)), //
      reprint("V10", "From the Vault: Relics", date(2010, 8, 27)), //
      reprint("V11", "From the Vault: Legends", date(2011, 8, 26)), //
      reprint("V12", "From the Vault: Realms", date(2012, 8, 31)), //
      reprint("V13", "From the Vault: Twenty", date(2013, 8, 23)), //
      reprint("V14", "From the Vault: Annihilation", date(2014, 8, 22)), //
      reprint("V15", "From the Vault: Angels", date(2015, 8, 21)), //
      reprint("V16", "From the Vault: Lore", date(2016, 8, 19)), //

      // DECK
      // Rivals Quick Start Set No specific symbol none July 1, 1996 Four pre-constructed decks
      deck("ATH", "Anthologies", date(1998, 11, 1)), //
      deck("BRB", "Battle Royale Box Set", date(1999, 11, 12)), //
      deck("BTD", "Beatdown Box Set", date(2000, 12, 1)), //
      deck("DKM", "Deckmasters: Garfield vs. Finkel", date(2001, 9, 17)), //
      deck("DPA", "Duels of the Planeswalkers (decks)", date(2010, 6, 4)), //
      // Duel Decks
      deck("EVG", "Duel Decks: Elves vs. Goblins", date(2007, 11, 16)), //
      deck("DD2", "Duel Decks: Jace vs. Chandra", date(2008, 11, 7)), //
      deck("DDC", "Duel Decks: Divine vs. Demonic", date(2009, 4, 10)), //
      deck("DDD", "Duel Decks: Garruk vs. Liliana", date(2009, 10, 30)), //
      deck("DDE", "Duel Decks: Phyrexia vs. the Coalition", date(2010, 3, 19)), //
      deck("DDF", "Duel Decks: Elspeth vs. Tezzeret", date(2010, 9, 3)), //
      deck("DDG", "Duel Decks: Knights vs. Dragons", date(2011, 4, 1)), //
      deck("DDH", "Duel Decks: Ajani vs. Nicol Bolas", date(2011, 9, 2)), //
      deck("DDI", "Duel Decks: Venser vs. Koth", date(2012, 3, 30)), //
      deck("DDJ", "Duel Decks: Izzet vs. Golgari", date(2012, 9, 7)), //
      deck("DDK", "Duel Decks: Sorin vs. Tibalt", date(2013, 3, 15)), //
      deck("DDL", "Duel Decks: Heroes vs. Monsters", date(2013, 9, 6)), //
      deck("DDM", "Duel Decks: Jace vs. Vraska", date(2014, 3, 14)), //
      deck("DDN", "Duel Decks: Speed vs. Cunning", date(2014, 9, 5)), //
      deck("DD3", "Duel Decks Anthology", date(2014, 12, 5)), //
      deck("DDO", "Duel Decks: Elspeth vs. Kiora", date(2015, 2, 27)), //
      deck("DDP", "Duel Decks: Zendikar vs. Eldrazi", date(2015, 8, 28)), //
      deck("DDQ", "Duel Decks: Blessed vs. Cursed", date(2016, 2, 26)), //
      deck("DDR", "Duel Decks: Nissa vs. Ob Nixilis", date(2016, 9, 2)), //
      deck("DDS", "Duel Decks: Mind vs. Might", date(2017, 3, 31)), //
      deck("DDT", "Duel Decks: Merfolk vs. Goblins", date(2017, 11, 10)), //
      // Premium Deck Series
      deck("H09", "Premium Deck Series: Slivers", date(2009, 11, 20)), //
      deck("PD2", "Premium Deck Series: Fire and Lightning", date(2010, 11, 19)), //
      deck("PD3", "Premium Deck Series: Graveborn", date(2011, 11, 18)), //
      // Event Decks
      deck("MD1", "Modern Event Deck 2014", date(2014, 5, 30)), //

      // MULTIPLAYER
      // Planechase
      deck("HOP", "Planechase", date(2009, 9, 4)), //
      deck("PC2", "Planechase 2012 Edition", date(2012, 6, 1)), //
      deck("PCA", "Planechase Anthology", date(2016, 11, 25)), //
      // Archenemy
      deck("ARC", "Archenemy", date(2010, 6, 18)), //
      deck("E01", "Archenemy: Nicol Bolas", date(2017, 6, 16)), //
      // Commander
      deck("CMD", "Commander", date(2011, 6, 17)), //
      deck("CM1", "Commander's Arsenal", date(2012, 11, 2)), //
      deck("C13", "Commander 2013 Edition", date(2013, 11, 1)), //
      deck("C14", "Commander 2014", date(2014, 11, 7)), //
      deck("C15", "Commander 2015", date(2015, 11, 13)), //
      deck("C16", "Commander 2016", date(2016, 11, 11)), //
      deck("CMA", "Commander Anthology", date(2017, 6, 9)), //
      deck("C17", "Commander 2017", date(2017, 8, 25)), //
      // Conspiracy
      deck("CNS", "Conspiracy", date(2014, 6, 6)), //
      deck("CN2", "Conspiracy: Take the Crown", date(2016, 8, 26)), //
      // Other sets
      deck("E02", "Explorers of Ixalan", date(2017, 11, 24)), //

      // OTHER
      // Portal
      other("POR", "Portal", date(1997, 6, 1)), // 20 90 57 55 0 0
      other("PO2", "Portal Second Age", date(1998, 6, 1)), // 15 70 45 35 0 0
      other("PTK", "Portal Three Kingdoms", date(1999, 5, 1)), // 15 55 55 55 0 0
      // Starter
      other("S99", "Starter 1999", date(1999, 7, 1)), // 20 63 55 35 0 0
      other("S00", "Starter 2000", date(2000, 7, 1)), // 10 39 6 2 0 0
      // Non-Legal
      other("CED", "Collector's Edition", date(1993, 12, 1)), // 76 75 95 117 0 0
      other("CEI", "International Collector's Edition", date(1993, 12, 1)), // 76 75 95 117 0 0
      other("UGL", "Unglued", date(1998, 8, 17)), // 5 33 22 28 0, 0
      other("UNH", "Unhinged", date(2004, 11, 20)), // 5 55 40 40 0 1
      // Online-Only
      other("MED", "Masters Edition", date(2007, 9, 10)), // 15 60 60 60 0 0
      other("ME2", "Masters Edition II", date(2008, 9, 22)), // 5 80 80 80 0 0
      other("ME3", "Masters Edition III", date(2009, 9, 7)), // 15 75 70 70 0 0
      other("ME4", "Masters Edition IV", date(2011, 1, 10)), // 12 80 72 105 0 0
      other("VMA", "Vintage Masters", date(2014, 6, 16)), // 0 101 80 105 30 9
      other("TPR", "Tempest Remastered", date(2015, 5, 6)), // 20 101 80 53 15 0
  );

  private static LocalDate date(int year, int month, int dayOfMonth) {
    return LocalDate.of(year, month, dayOfMonth);
  }

  private static MagicSet core(String id, String name, LocalDate releasedOn) {
    return new MagicSet(MagicSetType.CORE, id, name, releasedOn);
  }

  private static MagicSet exp(String id, String name, LocalDate releasedOn) {
    return new MagicSet(MagicSetType.EXPANSION, id, name, releasedOn);
  }

  private static MagicSet reprint(String id, String name, LocalDate releasedOn) {
    return new MagicSet(MagicSetType.REPRINT, id, name, releasedOn);
  }

  private static MagicSet deck(String id, String name, LocalDate releasedOn) {
    return new MagicSet(MagicSetType.DECK, id, name, releasedOn);
  }

  private static MagicSet other(String id, String name, LocalDate releasedOn) {
    return new MagicSet(MagicSetType.OTHER, id, name, releasedOn);
  }

  private final MagicSetType type;
  private final String name;
  private final LocalDate releasedOn;

  private MagicSet(MagicSetType type, String id, String name, LocalDate releasedOn) {
    super(id);
    this.type = checkNotNull(type);
    this.name = checkNotNull(name);
    this.releasedOn = checkNotNull(releasedOn);
  }

  public MagicSetType type() {
    return type;
  }

  public String name() {
    return name;
  }

  public LocalDate releasedOn() {
    return releasedOn;
  }
}
