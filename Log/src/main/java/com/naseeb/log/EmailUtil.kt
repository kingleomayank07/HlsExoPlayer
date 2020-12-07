package com.naseeb.log

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Patterns

class EmailUtil {
    fun sendEmail(context: Context, emails: Array<String?>?, subject: String?, emailBody: String?, attachmentUri: Uri?, emailIntentTitle: String?): Boolean {
        val emailIntent = Intent("android.intent.action.SEND")
        emailIntent.type = "text/plain"
        if (emails != null) {
            emailIntent.putExtra("android.intent.extra.EMAIL", emails)
        }
        emailIntent.putExtra("android.intent.extra.SUBJECT", subject)
        emailIntent.putExtra("android.intent.extra.TEXT", emailBody)
        if (attachmentUri != null) {
            emailIntent.putExtra("android.intent.extra.STREAM", attachmentUri)
        }

        //Intent chooser = Intent.createChooser(emailIntent, emailIntentTitle);
        val chooser = Intent.createChooser(emailIntent, "Share File")
        val resInfoList = context.packageManager.queryIntentActivities(chooser,
                PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(packageName, attachmentUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (context !is Activity) {
            chooser.flags = 268435456
        }
        return if (emailIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooser)
            true
        } else {
            LogUtil.debugLog("email", "no email app installed")
            false
        }
    }

    fun sendEmail(context: Context, emails: Array<String?>?, subjectStringRes: Int, emailBodyStringRes: Int, attachmentUri: Uri?, emailIntentTitleStringRes: Int, appName: String): Boolean {
        return this.sendEmail(context, emails, context.getString(subjectStringRes, *arrayOf<Any>(appName)), context.getString(emailBodyStringRes, *arrayOf<Any>(appName)), attachmentUri, context.getString(emailIntentTitleStringRes))
    }

    fun canSendEmail(context: Context): Boolean {
        val emailIntent = Intent("android.intent.action.SEND")
        emailIntent.type = "text/plain"
        return emailIntent.resolveActivity(context.packageManager) != null
    }

    companion object {
        @Synchronized
        fun isValidEmail(target: CharSequence?): Boolean {
            return if (target == null) false else Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }
}