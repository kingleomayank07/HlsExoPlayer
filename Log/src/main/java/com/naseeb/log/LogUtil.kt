package com.naseeb.log

object LogUtil {
    fun debugLog(tag: String?, message: String?) {
        AppLoggerImpl.getInstance()
            .log(LogLevel.DEBUG, tag, message)
    }

    fun warnLog(tag: String?, message: String?) {
        AppLoggerImpl.getInstance()
            .log(LogLevel.WARN, tag, message)
    }

    fun errorLog(tag: String?, message: String?) {
        AppLoggerImpl.getInstance()
            .log(LogLevel.ERROR, tag, message)
    }
}