package cc.blunet.common.util;

import static org.apache.commons.lang3.StringUtils.substring;

import java.nio.file.Path;

public class Paths2 {

  public static String fileName(Path file) {
    return file.getFileName().toString();
  }

  public static String stripFileSuffix(String fileName) {
    return substring(fileName, 0, -4);
  }
}
