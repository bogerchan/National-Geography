package me.boger.geographic.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import android.text.Html
import java.util.*

/**
 * Created by BogerChan on 2017/7/12.
 */
object SdkSupporter {
    fun fromHtml(source: String) =
            (if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) Html.fromHtml(source)
            else Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY))!!

    fun updateConfiguration(res: Resources, locale: Locale) {
        val conf = res.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocale(locale)
            val localList = LocaleList(locale)
            LocaleList.setDefault(localList)
            conf.locales = localList
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(locale)
        } else {
            conf.locale = locale
            res.updateConfiguration(conf, res.displayMetrics)
        }
    }

    fun createConfigurationContext(ctx: Context, conf: Configuration)
            = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        ctx.createConfigurationContext(conf)
    } else {
        ctx
    }
}