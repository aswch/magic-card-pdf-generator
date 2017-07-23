package cc.blunet.mtg.tools;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;

import cc.blunet.common.io.compression.ZipArchive;
import cc.blunet.common.util.Paths2;

/**
 * Updates the json Db from mtgjson.org.
 *
 * @author claude.nobs@blunet.cc
 */
public class JsonDbUpdaterApp {

  // single card from mtgjson 3.10.0 onwards
  private static final Set<String> tokenCards = ImmutableSet.of(//
      "Brisela, Voice of Nightmares", // meld card
      "Chittering Host", // meld card
      "Hanweir, the Writhing Township" // meld card
  );

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws IOException, URISyntaxException {
    // download data
    URL url = new URL("https://mtgjson.com/json/AllSetsArray-x.json");
    String data = CharStreams.toString(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));

    // create object graph
    List<Map<String, Object>> json = new ObjectMapper().readValue(data, List.class);

    json.forEach(set -> {
      // discard unnecessary information
      set.remove("translations");
      ((List<Map<String, Object>>) set.get("cards")).forEach(card -> {
        // discard unnecessary information
        card.remove("foreignNames");
        String name = (String) card.get("name");
        if (tokenCards.contains(name)) {
          card.put("layout", "token");
        }
      });
    });
    // TODO add challenge decks (DC1,CD2,CD3)

    // write
    Path out = Paths2.of(JsonDbUpdaterApp.class, "/").resolve("AllSetsArray-x.json");

    // unformatted zipped
    ObjectMapper mapper = new ObjectMapper();
    Files.write(out, mapper.writeValueAsBytes(json));
    ZipArchive.compress(out);

    // formatted plain
    mapper.setDefaultPrettyPrinter(new MyPrettyPrinter()) //
        .enable(SerializationFeature.INDENT_OUTPUT);
    Files.write(out, mapper.writeValueAsBytes(json));
  }

  @SuppressWarnings("serial")
  private static final class MyPrettyPrinter extends DefaultPrettyPrinter {
    public MyPrettyPrinter() {}

    public MyPrettyPrinter(MyPrettyPrinter myPrettyPrinter) {
      super(myPrettyPrinter);
      indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
    }

    @Override
    public DefaultPrettyPrinter createInstance() {
      return new MyPrettyPrinter(this);
    }

    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator jg) throws IOException {
      jg.writeRaw(": ");
    }
  }
}
