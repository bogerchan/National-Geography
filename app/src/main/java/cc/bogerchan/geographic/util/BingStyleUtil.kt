package cc.bogerchan.geographic.util

import org.jsoup.Jsoup

/**
 * Created by hb.chen on 2018/3/10.
 */

object BingStyleUtil {

    fun addBingCss(raw: String): String {
        return try {
            val doc = Jsoup.parse(raw)
            // 移除不必要信息
            doc.select("#hplaDL")?.forEach { it.remove() }
            doc.select("span.hplaDM")?.forEach { it.remove() }
            doc.select("div.hplaCopy")?.forEach { it.remove() }
            doc.select("#hpBingAppQR")?.forEach { it.remove() }
            val head = doc.select("head").first()
            head.append("""<meta name="viewport" content="width=device-width, minimum-scale=0.85, initial-scale=0.85, maximum-scale=0.85, user-scalable=no">""")
            head.append("""<meta name="mobileoptimized" content="0">""")
            doc.toString()
        } catch (e: Exception) {
            Timber.e(e)
            raw
        }
    }
}