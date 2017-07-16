package cc.blunet.common.io.compression;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.UnaryOperator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

public final class ZipArchive {

  private ZipArchive() {}

  private static final Logger LOG = LoggerFactory.getLogger(ZipArchive.class);

  public static void extract(Path file, UnaryOperator<Path> fileMapper) {
    LOG.info("Extracting Archive: {}", file);

    try (ZipInputStream zip = new ZipInputStream(new FileInputStream(file.toFile()))) {

      ZipEntry entry = zip.getNextEntry();
      while (entry != null) {
        if (!entry.isDirectory()) {
          Path filePath = Paths.get(entry.getName());
          File path = fileMapper.apply(filePath).toFile();

          LOG.info("Extracting {} to {}", filePath, path);

          if (!path.getParentFile().exists()) {
            path.getParentFile().mkdirs();
          }
          try (FileOutputStream fos = new FileOutputStream(path)) {
            ByteStreams.copy(zip, fos);
          }
        }
        entry = zip.getNextEntry();
      }
      LOG.info("Extraction successfully finished.");

    } catch (IOException ex) {
      LOG.error("Could not extract " + file + ".", ex);
    }
  }
}
