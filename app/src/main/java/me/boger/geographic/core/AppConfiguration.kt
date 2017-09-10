package me.boger.geographic.core

import android.content.Context
import android.graphics.Typeface
import android.util.DisplayMetrics
import android.util.TypedValue
import me.boger.geographic.util.Timber

/**
 * Created by BogerChan on 2017/6/26.
 */

object AppConfiguration {
    var screenWidth: Int = 0
    var screenHeight: Int = 0
    var primaryTypeface: Typeface = Typeface.DEFAULT
    var iconFont: Typeface = Typeface.DEFAULT
    private var displayMetrics: DisplayMetrics? = null

    fun init(context: Context) {
        val metrics = context.applicationContext.resources.displayMetrics
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        displayMetrics = metrics
        try {
            primaryTypeface = Typeface.createFromAsset(context.applicationContext.assets, "font/HYRunYuan-35W.ttf")
            iconFont = Typeface.createFromAsset(context.applicationContext.assets, "font/iconfont.ttf")
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun dp2px(dp: Int): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), displayMetrics)
}