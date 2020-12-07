package com.naseeb.log

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.naseeb.log.LogSingletonProvider.Companion.getLogger
import java.io.IOException

class SendLogsByEmail {
    /**
     * Compress and send logs via email
     *
     * @param context [Context] if Activity is
     * passed then email compose activity
     * is launched in same task
     */
    fun compressAndSendLogsViaEmail(context: Context, emailsTo: Array<String?>?) {
        val compressor = LogFileCompressor()
        try {
            val compressedFile = compressor.compress(context.applicationContext)
            log("compressedFile $compressedFile")
            val uri: Uri
            uri = if (Build.VERSION.SDK_INT >= 24) {
                FileProvider.getUriForFile(context, AppLoggerImpl.getApplicationId() + ".provider",
                        compressedFile)
            } else {
                Uri.fromFile(compressedFile)
            }
            val canEmailBeSent = EmailUtil().sendEmail(context.applicationContext, emailsTo,
                    "Sirona-tapp Android | Logs", "Logs of Sirona-tapp Android app attached.", uri, "Send "
                    + "logs via "
                    + "email")
            if (!canEmailBeSent) {
                /*Toast.makeText(context, context.resources
                        .getString(R.string.send_log_by_email_toast_no_email_app_installed), Toast.LENGTH_SHORT)
                        .show()*/
            }
        } catch (e: IOException) {
            logger.log(LogLevel.ERROR, TAG, "IOException $e")
            /*Toast.makeText(context, context.resources
                    .getString(R.string.send_log_by_email_toast_there_was_error_compressing_logs),
                    Toast.LENGTH_SHORT).show()*/
            e.printStackTrace()
        }
    }

    companion object {
        private val logger = getLogger()
        private val TAG = SendLogsByEmail::class.java.canonicalName

        /**
         * helper method for logging
         */
        private fun log(message: String) {
            logger.log(LogLevel.DEBUG, TAG, message)
        }
    }
}