package cc.blunet.common.util;

import static org.apache.commons.lang3.StringUtils.substring;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Paths2 {

  public static String fileName(Path file) {
    return file.getFileName().toString();
  }

  public static String stripFileSuffix(String fileName) {
    return substring(fileName, 0, -4);
  }

  public static Path of(Class<?> clazz, String name) throws URISyntaxException {
    return Paths.get(clazz.getResource(name).toURI());
  }
}
