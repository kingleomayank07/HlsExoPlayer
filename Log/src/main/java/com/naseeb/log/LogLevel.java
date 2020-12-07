package com.naseeb.log;

/**
 * Level class used for logging
 * <ul>
 * <li>{@link #SENSITIVE}</li>
 * <li>{@link #INFO}</li>
 * <li>{@link #DEBUG}</li>
 * <li>{@link #WARN}</li>
 * <li>{@link #ERROR}</li>
 * </ul>
 */
public enum LogLevel {
  /**
   * Level to log sensitive info. Such logs won't be written to file and not sent to
   * server either
   */
  SENSITIVE,
  /**
   * Information level
   */
  INFO,
  /**
   * Debug level
   */
  DEBUG,
  /**
   * Warning level
   */
  WARN,
  /**
   * Error level
   */
  ERROR
}
