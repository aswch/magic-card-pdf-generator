package cc.blunet.mtg.core;

import static cc.blunet.common.util.Paths2.fileName;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.substring;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;

import cc.blunet.common.Unchecked;

/**
 * Creates {@link PrintedDeck}s from deck-files. Also supports basic markdown markup.
 *
 * @author claude.nobs@blunet.cc
 */
public class AdvDeckFactory {

  private static final Pattern basicMd = Pattern.compile("^[#*+-]+\\s"); // '#'-headings and '*'-,'+'-,'-'-lists
  private static final Pattern linkMd = Pattern.compile("\\[(.*)\\]\\(http://[-A-Za-z0-9_.@:/?&=!$'()*+,;~]+\\)");

  private final DeckFactory df;

  public AdvDeckFactory(DeckFactory df) {
    this.df = checkNotNull(df);
  }

  public Set<PrintedDeck> createFrom(Path path) {
    try {
      String fileName = substring(fileName(path), 0, -4);
      return splitDecks(readDeckFile(path)).stream()//
          .map(lines -> df.createFrom(lines, fileName)) //
          .collect(toImmutableSet());
    } catch (IOException ex) {
      Unchecked.rethrow(ex);
      return null; // unreachable
    }
  }

  private List<String> readDeckFile(Path path) throws IOException {
    return Files.readAllLines(path, Charsets.UTF_8).stream() //
        .map(line -> basicMd.matcher(line).replaceFirst("")) // remove basic markdown
        .map(line -> linkMd.matcher(line).replaceFirst("$1")) // remove markdown links
        .map(StringEscapeUtils::unescapeHtml4) // handle tappedout.net md files
        .map(String::trim) //
        .filter(line -> !line.startsWith("by ")) //
        .filter(line -> !line.isEmpty()) // remove empty lines
        .collect(toList());
  }

  private List<List<String>> splitDecks(List<String> lines) {
    ImmutableList.Builder<List<String>> result = ImmutableList.builder();
    ImmutableList.Builder<String> part = ImmutableList.builder();
    for (String line : lines) {
      if (line.matches("^[-_*]{3,}$")) {
        result.add(part.build());
        part = ImmutableList.builder();
      } else {
        part.add(line);
      }
    }
    result.add(part.build());
    return result.build();
  }
}
