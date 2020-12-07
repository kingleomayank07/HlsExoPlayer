package com.naseeb.log;

import android.content.Context;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import pl.brightinventions.slf4android.FileLogHandlerConfiguration;
import pl.brightinventions.slf4android.LoggerConfiguration;

/**
 * Implements logger
 */
public class AppLoggerImpl implements AppLogger {

    private final static AtomicBoolean isInitialized = new AtomicBoolean(false);
    /**
     * instance of logger
     */
    private static AppLogger sAppLogger = new AppLoggerImpl();
    private static boolean mIsDebugBuild;
    private static String mApplicationId;
    private static String mApplicationFlavor;

    private AppLoggerImpl() {
    }

    /**
     * getter for instance
     */
    public static AppLogger getInstance() {
        return sAppLogger;
    }

    /**
     * initialize logger
     *
     * @param c {@link Context}
     */
    public static void init(Context c, boolean isDebugBuild, String applicationId, String applicationFlavor) {
        /** get file handler */
        FileLogHandlerConfiguration fileHandler = LoggerConfiguration.fileLogHandler(c);
        /** set number of files for rotation */
        fileHandler.setRotateFilesCountLimit(NUMBER_OF_FILES_FOR_ROTATION);
        /** get log file directory path */
        String logsFileDirPath = c.getDir(LOG_DIR, Context.MODE_PRIVATE).getAbsolutePath();
        /** create log file path */
        String logFileWithPath = logsFileDirPath + File.separator + LOG_FILENAME;
        /** set file path on file handler */
        fileHandler.setFullFilePathPattern(logFileWithPath);
        /** set log file size limit on file handler */
        fileHandler.setLogFileSizeLimitInBytes(LOG_FILE_SIZE_LIMIT_IN_BYTES);
        /** add file handler to log configuration */
        LoggerConfiguration.configuration().addHandlerToRootLogger(fileHandler);
        mIsDebugBuild = isDebugBuild;
        mApplicationId = applicationId;
        mApplicationFlavor = applicationFlavor;
        isInitialized.set(true);
    }

    public static String getApplicationId() {
        return mApplicationId;
    }

    public static String getApplicationFlavor() {
        return mApplicationFlavor;
    }

    private Logger getLogger(String tag) {
        return LoggerFactory.getLogger(tag);
    }

    @Override
    synchronized public void log(LogLevel logLevel, String tag, String message)
            throws LogNotInitializedException {
        /** if app is not debuggable then check if
         * level is allowed to be logged */
        if (!mIsDebugBuild) {
            /** if level is not loggable
             * then return without logging */
            if (!Loggable.isLoggable(logLevel)) {
                return;
            }
        }
        /**
         * If Logger is not initialized
         * in {@link Application} class then throw
         * {@link LogNotInitializedException}
         */
        if (!isInitialized.get()) {
            throw new LogNotInitializedException();
        }
        Logger logger = getLogger(tag);
        switch (logLevel) {
            case DEBUG:
                logger.debug(message);
                break;
            case ERROR:
                logger.error(message);
                break;
            case WARN:
                logger.warn(message);
                break;
            case INFO:
                logger.info(message);
                break;
            case SENSITIVE:
                Log.d(tag, message);
                break;
        }
    }
}
