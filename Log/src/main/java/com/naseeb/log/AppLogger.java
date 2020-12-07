package com.naseeb.log;

/**
 * Logging interface.
 * <br/>
 * use this to log logs.
 */
public interface AppLogger {

  /**
   * File name for logs
   */
  String LOG_FILENAME = "app.%g.%u.log";
  /**
   * Directory where logs are kept
   */
  String LOG_DIR = "logs";
  /**
   * File size limit per log file in rotation
   * {@code 1 MB}
   */
  int LOG_FILE_SIZE_LIMIT_IN_BYTES = 1024 * 1024;
  /**
   * Number of files for rotation
   */
  int NUMBER_OF_FILES_FOR_ROTATION = 5;

  /**
   * @param logLevel {@link LogLevel} provide log level
   * @param tag {@link String} tag to log
   * @param message {@link String} message to log
   * @throws LogNotInitializedException
   */
  void log(LogLevel logLevel, String tag, String message) throws LogNotInitializedException;

  /**
   * Exception class to be thrown when config is not initialize
   */
  final class LogNotInitializedException extends RuntimeException {
    public LogNotInitializedException() {
      super("Initialize logConfig file first before calling log method");
    }
  }
}
