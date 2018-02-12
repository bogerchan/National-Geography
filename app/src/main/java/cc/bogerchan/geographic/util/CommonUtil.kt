package cc.bogerchan.geographic.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.app.Fragment
import android.text.Html
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by Boger Chan on 2018/1/28.
 */
object CommonUtil {

    private val gson by lazy {
        GsonBuilder()
                .addSerializationExclusionStrategy(object : ExclusionStrategy {
                    override fun shouldSkipClass(clazz: Class<*>?) = false

                    override fun shouldSkipField(f: FieldAttributes)
                            = f.getAnnotation(Transient::class.java) != null
                            || f.name.endsWith("\$delegate")
                })
                .create()
    }

    fun fromHtml(source: String) =
            (if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) Html.fromHtml(source)
            else Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY))!!

    fun mailTo(context: Context, address: String, subject: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$address"))
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun walkViewTree(v: View?, closure: (View?) -> Unit) {
        when (v) {
            is ViewGroup -> 0.rangeTo(v.childCount).forEach { walkViewTree(v.getChildAt(it), closure) }
            else -> closure(v)
        }
    }

    fun obtainRetrofit(baseUrl: String) = Retrofit.Builder()
            .client(OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.MINUTES)
                    .connectTimeout(60, TimeUnit.MINUTES)
                    .build())
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    fun Fragment.dp2px(dp: Int): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.applicationContext.resources.displayMetrics)
    }
}

fun <E> MutableList<E>.removeIfCompat(filter: (e: E) -> Boolean): Boolean {
    var removed = false
    val each = iterator()
    while (each.hasNext()) {
        if (filter(each.next())) {
            each.remove()
            removed = true
        }
    }
    return removed
}