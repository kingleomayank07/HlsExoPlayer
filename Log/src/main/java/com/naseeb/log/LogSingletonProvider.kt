package com.naseeb.log

class LogSingletonProvider {

    companion object {
        /**
         * get the [AppLogger] singleton
         *
         * @return implementation of [AppLogger]
         */
        fun getLogger(): AppLogger {
            return AppLoggerImpl.getInstance()
        }
    }
}