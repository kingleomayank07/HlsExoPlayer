package com.naseeb.log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final public class CompressionUtil {

  private final static String TAG = CompressionUtil.class.getCanonicalName();
  private final static AppLogger logger = LogSingletonProvider.Companion.getLogger();

  /*  */

  /**
   * Compress (tar.gz) the input files to the output file
   *
   * @param files {@link Collection} of {@link File} The files to compress
   * //@param output {@link File} The resulting output file (should end in .tar.gz)
   * and places it in the same directory
   * @throws IOException
   *//*
  final public void compressFiles(Collection<File> files, File output) throws
      IOException {
    logger.log(LogLevel.DEBUG, TAG, "entering compressFiles()");
    // Create the output stream for the output file
    FileOutputStream fos = new FileOutputStream(output);
    // Wrap the output file stream in streams that will tar and gzip everything
    TarArchiveOutputStream taos =
        new TarArchiveOutputStream(new GZIPOutputStream(new BufferedOutputStream(fos)));
    // TAR has an 8 gig file limit by default, this gets around that
    taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR); // to get past the 8 gig limit
    // TAR originally didn't support long file names, so enable the support for it
    taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

    // Get to putting all the files in the compressed output file
    for (File f : files) {
      logger.log(LogLevel.DEBUG, TAG, "looping through files - current file = " + files);
      addFilesToCompression(taos, f, ".");
    }

    // Close everything up
    taos.close();
    fos.close();
    logger.log(LogLevel.DEBUG, TAG, "exiting compressFiles()");
  }
*/
  public void zip(List<String> files, String zipFile) {
    //String[] _files = files;
    //String _zipFile = zipFile;
    int BUFFER = 1024 * 2;

    try {
      BufferedInputStream origin = null;
      FileOutputStream dest = new FileOutputStream(zipFile);

      ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

      byte data[] = new byte[BUFFER];

      for (int i = 0; i < files.size(); i++) {
        logger.log(LogLevel.DEBUG, "add:", files.get(i));
        logger.log(LogLevel.DEBUG, "Compress", "Adding: " + files.get(i));
        FileInputStream fi = new FileInputStream(files.get(i));
        origin = new BufferedInputStream(fi, BUFFER);
        ZipEntry entry = new ZipEntry(files.get(i).substring(files.get(i).lastIndexOf("/") + 1));
        out.putNextEntry(entry);
        int count;
        while ((count = origin.read(data, 0, BUFFER)) != -1) {
          out.write(data, 0, count);
        }
        origin.close();
      }

      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /* *//**
   * Does the work of compression and going recursive for nested directories
   * <br />
   *
   * @param taos {@link TarArchiveOutputStream} The archive
   * @param file {@link File} The file to add to the archive
   * @param dir {@link String} The directory that should serve as the parent directory in the
   * archive
   * @throws IOException
   *//*
  final private void addFilesToCompression(TarArchiveOutputStream taos, File file, String dir)
      throws
      IOException {
    logger.log(LogLevel.DEBUG, TAG, "entering addFilesToCompression()");
    // Create an entry for the file
    TarArchiveEntry tarArchiveEntry=new TarArchiveEntry(file, dir + File.separator + file.getName
        ());
    tarArchiveEntry.setSize(file.length());

    taos.putArchiveEntry(tarArchiveEntry);
    if (file.isFile()) {
      logger.log(LogLevel.DEBUG, TAG, "file is file");
      // Add the file to the archive
      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
      IOUtils.copy(bis, taos);
      taos.closeArchiveEntry();
      bis.close();
    } else if (file.isDirectory()) {
      logger.log(LogLevel.DEBUG, TAG, "file is dir");
      // close the archive entry
      taos.closeArchiveEntry();
      // go through all the files in the directory and using recursion, add them to the
      // archive
      for (File childFile : file.listFiles()) {
        addFilesToCompression(taos, childFile, file.getName());
      }
    }
    logger.log(LogLevel.DEBUG, TAG, "exiting addFilesToCompression()");
  }*/
}
