package cc.blunet.common.io.serialization;

import static cc.blunet.common.util.Paths2.fileName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipInputStream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.mrbean.MrBeanModule;

import cc.blunet.common.Unchecked;

public final class JacksonUtils {
  private JacksonUtils() {}

  public static Stream<JsonNode> stream(JsonNode node) {
    return StreamSupport.stream(node.spliterator(), false);
  }

  public static <T> T readJsonValue(Path file, ObjectMapper mapper, Class<T> type) {
    return readValue(file, mapper, type);
  }

  public static <T> T readJsonValue(Path file, ObjectMapper mapper, TypeReference<T> type) {
    return readValue(file, mapper, type);
  }

  // use JsonNode as type to get a DOM.
  // read from zipped or plaintext files
  @SuppressWarnings({"rawtypes", "unchecked"})
  private static <T, R> R readValue(Path file, ObjectMapper mapper, T type) {

    try (InputStream is = Channels.newInputStream(Files.newByteChannel(file))) {
      Reader reader = reader(file, is);

      // support abstract type materialization
      mapper.registerModule(new MrBeanModule());

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

  private static Reader reader(Path file, InputStream is) throws IOException {
    if (fileName(file).endsWith(".zip")) {
      ZipInputStream zip = new ZipInputStream(is);
      zip.getNextEntry();
      return new InputStreamReader(zip, StandardCharsets.UTF_8);
    }
    return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
  }
}
