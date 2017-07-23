package cc.blunet.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;

public final class Files2 {
  private Files2() {}

  public static long crc32(Path path) throws IOException {
    CRC32 crc = new CRC32();
    crc.update(Files.readAllBytes(path));
    return crc.getValue();
  }
}
