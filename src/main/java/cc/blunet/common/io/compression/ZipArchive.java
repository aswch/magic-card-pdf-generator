package cc.blunet.common.io.compression;

import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import cc.blunet.common.util.Files2;

public final class ZipArchive {

  private ZipArchive() {}

  private static final Logger LOG = LoggerFactory.getLogger(ZipArchive.class);

  public static void extract(Path path, UnaryOperator<Path> fileMapper) {
    LOG.info("Extracting Archive: {}", path);

    try (ZipInputStream zip = new ZipInputStream(new FileInputStream(path.toFile()))) {

      ZipEntry entry = zip.getNextEntry();
      while (entry != null) {
        if (!entry.isDirectory()) {
          Path oldPath = Paths.get(entry.getName());
          Path newPath = fileMapper.apply(oldPath);
          File file = newPath.toFile();

          if (!file.exists() || Files2.crc32(newPath) != entry.getCrc()) {

            if (!file.getParentFile().exists()) {
              file.getParentFile().mkdirs();
            }
            LOG.debug("Extracting {} to {}", oldPath, newPath);
            try (FileOutputStream fos = new FileOutputStream(file)) {
              ByteStreams.copy(zip, fos);
            }
          } else {
            LOG.debug("Skipping {} to {} because it exists.", oldPath, newPath);
          }
        }
        entry = zip.getNextEntry();
      }
      LOG.debug("Extraction successfully finished.");

    } catch (IOException ex) {
      LOG.error("Could not extract " + path + ".", ex);
    }
  }

  public static void compressAll(Path directory, UnaryOperator<Path> fileMapper) {
    checkState(directory.toFile().isDirectory());
    LOG.info("Creating Archive: {}.zip", directory);

    try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(directory + ".zip"));
        Stream<Path> directoryStream = Files.walk(directory)) {

      directoryStream.forEach(path -> {
        if (!path.toFile().isDirectory()) {
          try (FileInputStream is = new FileInputStream(path.toFile())) {
            addToZipFile(fileMapper.apply(directory.getParent().relativize(path)), is, zip);
          } catch (IOException e) {
            throw new ZipEntryException("Unable to process " + path, e);
          }
        }
      });
      LOG.debug("Compression successfully finished.");

    } catch (IOException | ZipEntryException ex) {
      LOG.error("Error while zipping.", ex);
    }
  }

  @SuppressWarnings("serial")
  private static class ZipEntryException extends RuntimeException {
    public ZipEntryException(String reason, Exception inner) {
      super(reason, inner);
    }
  }

  public static void compress(Path file) {
    checkState(!file.toFile().isDirectory());
    LOG.info("Creating Archive: {}.zip", file);

    try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file + ".zip"));
        FileInputStream is = new FileInputStream(file.toFile())) {

      addToZipFile(file.getFileName(), is, zip);
      LOG.debug("Compression successfully finished.");

    } catch (IOException ex) {
      LOG.error("Error creating archive.", ex);
    }
  }

  /**
   * Adds an extra file to the zip archive, copying in the created date.
   */
  private static void addToZipFile(Path file, FileInputStream is, ZipOutputStream zip) throws IOException {

    ZipEntry entry = new ZipEntry(file.toString());
    entry.setCreationTime(FileTime.fromMillis(file.toFile().lastModified()));

    zip.putNextEntry(entry);
    long bytesCount = ByteStreams.copy(is, zip);

    LOG.info("Created new entry of {} bytes for {}.", bytesCount, file);
  }

}
