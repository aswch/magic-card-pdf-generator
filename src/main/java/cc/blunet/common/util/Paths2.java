package cc.blunet.common.util;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Paths2 {
  private Paths2() {}

  public static String fileName(Path file) {
    return file.getFileName().toString();
  }

  public static String stripFileSuffix(String fileName) {
    return fileName.contains(".") //
        ? fileName.substring(0, fileName.lastIndexOf('.'))
        : fileName;
  }

  public static Path of(Class<?> clazz) throws URISyntaxException {
    return of(clazz, ".");
  }

  public static Path of(Class<?> clazz, String name) throws URISyntaxException {
    return Paths.get(clazz.getResource(name).toURI());
  }
}
