package cc.blunet.common.io.compression;

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

  public static void extract(Path file, UnaryOperator<Path> fileMapper) throws IOException {
    try (//
        RandomAccessFile randomAccessFile = new RandomAccessFile(file.toFile(), "r");
        IInArchive inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile)); //
        SevenZipExtractor sze = new SevenZipExtractor(inArchive, fileMapper); //
    ) {
      LOG.info("Extracting {}", file);
      inArchive.extract(null, false, sze);
    } catch (Exception ex) {
      LOG.error("Could not extract " + file + ".", ex);
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
