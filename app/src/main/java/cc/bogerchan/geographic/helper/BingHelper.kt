package cc.bogerchan.geographic.helper

import android.content.Context
import cc.bogerchan.geographic.GApplication
import cc.bogerchan.geographic.util.Timber
import org.jsoup.Jsoup

/**
 * Created by hb.chen on 2018/3/10.
 */

object BingHelper {

    private val mSp by lazy { GApplication.app.getSharedPreferences("_NG_BING", Context.MODE_PRIVATE) }
    var lastContentMD5: String = ""
        private set
    private val KEY_LAST_CONTENT_MD5 = "key_last_content_md5"

    fun addDailyPostCss(raw: String): String {
        return try {
            val doc = Jsoup.parse(raw)
            // 移除不必要信息
//            doc.select("#hplaDL")?.forEach { it.remove() }
//            doc.select("span.hplaDM")?.forEach { it.remove() }
//            doc.select("div.hplaCopy")?.forEach { it.remove() }
//            doc.select("#hpBingAppQR")?.forEach { it.remove() }
            val head = doc.select("head").first()
//            head.append("""<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no">""")
//            head.append("""<meta name="mobileoptimized" content="0">""")
            head.append("""<style type="text/css">#hpla { background-color: transparent; color: #eeeef0 } .hplatBlue { color: #fdd000; border-color: #fdd000 } #hpla a { color: #fdd000; } .hplaCard div { background-color: #201c1a }</style>""")
            doc.toString()
        } catch (e: Exception) {
            Timber.e(e)
            raw
        }
    }

    fun loadLastContentMD5() {
        lastContentMD5 = mSp.getString(KEY_LAST_CONTENT_MD5, "")
    }

    fun saveLastContentMD5(md5: String) {
        mSp.edit().putString(KEY_LAST_CONTENT_MD5, md5).apply()
        lastContentMD5 = md5
    }
}