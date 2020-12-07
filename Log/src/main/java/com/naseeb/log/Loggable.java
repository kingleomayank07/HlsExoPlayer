package com.naseeb.log;



import com.naseeb.log.flavors.FlavorHelper;

import java.util.Arrays;
import java.util.List;

public class Loggable {

  static final List<LogLevel> ALLOWED_LEVELS_DEV = Arrays.asList(LogLevel.INFO, LogLevel.DEBUG,
      LogLevel.WARN, LogLevel.ERROR, LogLevel.SENSITIVE);

  static final List<LogLevel> ALLOWED_LEVELS_QA = Arrays.asList(LogLevel.INFO, LogLevel.DEBUG,
      LogLevel.WARN, LogLevel.ERROR, LogLevel.SENSITIVE);

  static final List<LogLevel> ALLOWED_LEVELS_STAGE = Arrays.asList(LogLevel.WARN, LogLevel.ERROR);

  static final List<LogLevel> ALLOWED_LEVELS_PROD = Arrays.asList(LogLevel.ERROR);

  static public final synchronized boolean isLoggable(LogLevel logLevel) {
    if (FlavorHelper.isDev()) {
      return ALLOWED_LEVELS_DEV.contains(logLevel);
    } else if (FlavorHelper.isProd()) {
      return ALLOWED_LEVELS_PROD.contains(logLevel);
    }
    return false;
  }
}
