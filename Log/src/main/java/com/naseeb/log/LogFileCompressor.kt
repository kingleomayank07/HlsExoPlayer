package com.naseeb.log

import android.content.Context
import android.os.Environment
import com.naseeb.log.LogSingletonProvider.Companion.getLogger
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Compresses log file
 */
class LogFileCompressor {
    /**
     * Get log file name
     *
     * @param c [Context]
     * @return [File] output file name (compressed file)
     */
    protected fun getOutFile(c: Context): File {
        log("Entering getOutFile() with Context $c")
        val outFilePath = (Environment.getExternalStorageDirectory().absolutePath
                + File.separator
                + OUT_FILE_DIR
                + File.separator
                + OUT_FILENAME)
        val outFile = File(outFilePath)
        if (!outFile.exists()) {
            outFile.parentFile.mkdirs()
        }
        log("Exiting getOutFile(), returning = $outFile")
        return outFile
    }

    /**
     * Compress log files
     *
     * @param files [List] of [File]
     * @param c [Context]
     * @return [File] compressed out file (like tar)
     * @throws IOException
     */
    /* final protected File compressLogFile(List<File> files, Context c) throws
      IOException {
    log("Entering compressLogFile() with files = " + files + ", context = " + c);
    CompressionUtil util = new CompressionUtil();
    File outFile = getOutFile(c);
    //util.compressFiles(files, outFile);
    util.zip(files,outFile);
    log("Exiting compressLogFile(), returning outfile = " + outFile);
    return outFile;
  }*/
    @Throws(IOException::class)
    protected fun compressLogFile(files: List<String>, c: Context): File {
        log("Entering compressLogFile() with files = $files, context = $c")
        val util = CompressionUtil()
        val outFile = getOutFile(c)
        //util.compressFiles(files, outFile);
        util.zip(files, outFile.absolutePath)
        log("Exiting compressLogFile(), returning outfile = $outFile")
        return outFile
    }

    /**
     * Get all files in the logs directory ([AppLogger.LOG_DIR])
     *
     * @param c [Context]
     * @return [List] of [File]
     */
    protected fun getAllFilesInLogsDirectory(c: Context): List<File> {
        log("Entering getAllFilesInLogsDirectory() with Context $c")
        val logsDir = c.applicationContext.getDir(AppLogger.LOG_DIR, Context.MODE_PRIVATE)
        if (null != logsDir && logsDir.exists() && logsDir.isDirectory) {
            val files = logsDir.listFiles()
            if (null != files) {
                val filesList = Arrays.asList(*files)
                log("exiting getAllFilesInLogsDirectory() with filesList = $filesList")
                return filesList
            }
        }
        log("exiting getAllFilesInLogsDirectory() with filesList = null")
        return ArrayList()
    }

    /**
     * Get only eligible log files for compression
     *
     * @param c [Context]
     * @return [List] of [File]
     */
    protected fun getLogFilesEligibleForCompression(c: Context): List<File> {
        log("entering getAllFilesInLogsDirectory() with Context = $c")
        val allFilesInLogsDirectory = getAllFilesInLogsDirectory(c)
        val eligibleFiles: MutableList<File> = ArrayList()
        for (file in allFilesInLogsDirectory) {
            log("in loop file = $file")
            if (isLogFileEligibleForCompression(c, file)) {
                log("in loop file = $file is eligible")
                eligibleFiles.add(file)
            } else {
                log("in loop file = $file is not eligible")
            }
        }
        log("exiting getAllFilesInLogsDirectory() with eligibleFiles = $eligibleFiles")
        return eligibleFiles
    }

    protected fun getLogFilesPathListEligibleForCompression(c: Context): List<String> {
        log("entering getAllFilesInLogsDirectory() with Context = $c")
        val allFilesInLogsDirectory = getAllFilesInLogsDirectory(c)
        val eligibleFiles: MutableList<String> = ArrayList()
        for (file in allFilesInLogsDirectory) {
            log("in loop file = $file")
            if (isLogFileEligibleForCompression(c, file)) {
                log("in loop file = $file is eligible")
                eligibleFiles.add(file.absolutePath)
            } else {
                log("in loop file = $file is not eligible")
            }
        }
        log("exiting getAllFilesInLogsDirectory() with eligibleFiles = $eligibleFiles")
        return eligibleFiles
    }

    /**
     * Do compression
     *
     * @param c [Context]
     * @return [File] compressed out file (in tar.gz)
     * @throws IOException
     */
    @Throws(IOException::class)
    fun compress(c: Context): File {
        log("entering compress() with Context = $c")
        val compressedFile = compressLogFile(getLogFilesPathListEligibleForCompression(c), c)
        log("exiting compress(), returning compressedFile = $compressedFile")
        return compressedFile
    }

    /**
     * Setting output file name
     */
    fun setOutputFileName(outputFileName: String) {
        log("entering setOutputFileName() with outputFileName:$outputFileName")
        OUT_FILENAME = outputFileName
        log("exiting setOutputFileName()")
    }

    /**
     * Check if file is eligible for compression
     *
     * @param c [Context]
     * @param file [File]
     * @return boolean true if file is eligible for compression
     * false otherwise
     */
    protected fun isLogFileEligibleForCompression(c: Context, file: File?): Boolean {
        log("entering isLogFileEligibleForCompression() with Context = $c, file  = $file")
        if (null == file) {
            log("file is null, returning isEligible = false")
            return false
        }
        val fileName = file.name
        if (null == fileName || fileName.isEmpty()) {
            log("fileName is null or empty, returning isEligible = false")
            return false
        }
        val isEligible = !file.name.contains("lck")
        log("exiting isLogFileEligibleForCompression(), returning isEligible = $isEligible")
        return isEligible
    }

    companion object {
        var OUT_FILENAME = "Sirona-tapp-android-logs.zip"
        const val OUT_FILE_DIR = "Sirona-tapp-logs"
        private val TAG = LogFileCompressor::class.java.canonicalName
        private val logger = getLogger()

        /**
         * helper method for logging
         */
        private fun log(message: String) {
            logger.log(LogLevel.DEBUG, TAG, message)
        }
    }
}