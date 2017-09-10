package me.boger.geographic.core

import android.os.Looper
import java.security.MessageDigest
import java.util.*

/**
 * Created by BogerChan on 2017/7/10.
 */
object NGUtil {
    fun toMD5(str: String): String {
        val md5 = MessageDigest.getInstance("MD5")
        md5.update(str.toByteArray())
        return md5.digest()
                .map { String.format(Locale.US, "%2x", it.toInt().and(0xff)) }
                .joinToString("")
    }

    fun isUIThread() = Thread.currentThread() == Looper.getMainLooper().thread
}