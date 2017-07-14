package cc.blunet.magic.deck2pdf;

import static java.util.stream.Collectors.toList;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

public class SevenZipExtractor implements IArchiveExtractCallback, Closeable {

  public static void main(String[] args) throws Exception {
    Path source = Paths.get("/", "Users", "bernstein", "XLHQ-Sets-Torrent", "test");
    Path target = source;

    for (Path file : java.nio.file.Files//
        .find(source, 1, (path, bfa) -> path.toFile().getName().endsWith(".zip"))//
        .collect(toList())) {
      extract(file, target);
    }
  }

  public static void extract(Path file, Path extractPath) throws SevenZipException, IOException {
    final String code = file.getFileName().toString().substring(0, 3);
    UnaryOperator<Path> fileMapper = path -> {
      if (path.endsWith("xlhq.jpg")) {
        throw new RuntimeException("Wrong name: " + path);
      }
      String filePath = path.getFileName().toString().replace("xlhq.jpg", code + ".jpg");
      return extractPath.resolve(filePath);
    };

    try (//
        RandomAccessFile randomAccessFile = new RandomAccessFile(file.toFile(), "r");
        IInArchive inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile)); //
        SevenZipExtractor sze = new SevenZipExtractor(inArchive, fileMapper); //
    ) {
      inArchive.extract(null, false, sze);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private static Logger LOG = LoggerFactory.getLogger(SevenZipExtractor.class);

  private static int getStreamCounter = 0;
  private static int writeCounter = 0;

  private final IInArchive inArchive;
  private final UnaryOperator<Path> fileMapper;

  private int currentIndex = -1;
  private FileOutputStream fos = null;

  public SevenZipExtractor(IInArchive inArchive, UnaryOperator<Path> fileMapper) {
    this.inArchive = inArchive;
    this.fileMapper = fileMapper;
  }

  @Override
  public ISequentialOutStream getStream(final int index, ExtractAskMode extractAskMode) throws SevenZipException {
    LOG.debug("running into getStream() : {}", getStreamCounter++);

    return data -> {
      LOG.debug("running into write({}) : {}", index, writeCounter++);
      try {
        if (index != currentIndex) {
          Path filePath = Paths.get(inArchive.getStringProperty(index, PropID.PATH));
          File path = fileMapper.apply(filePath).toFile();

          if (!path.getParentFile().exists()) {
            path.getParentFile().mkdirs();
          }
          if (!path.exists()) {
            path.createNewFile();
          }
          fosClose();
          fos = new FileOutputStream(path, true);
        }
        fos.write(data);
        return data.length;
      } catch (IOException ex) {
        try {
          throw new SevenZipException("Coundn't write " + index, ex);
        } finally {
          fosClose();
        }
      }
    };
  }

  private void fosClose() {
    try {
      if (fos != null) {
        fos.flush();
        fos.close();
        fos = null;
      }
    } catch (IOException ex) {
      LOG.debug("Could not close FileOutputStream" + ex);
    }
  }

  @Override
  public void close() throws IOException {
    fosClose();
  }

  @Override
  public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException {}

  @Override
  public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {}

  @Override
  public void setCompleted(long completeValue) throws SevenZipException {}

  @Override
  public void setTotal(long total) throws SevenZipException {}
}
