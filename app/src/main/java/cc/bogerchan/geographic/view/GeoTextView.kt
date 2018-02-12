package cc.bogerchan.geographic.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView
import cc.bogerchan.geographic.R

/**
 * Created by Boger Chan on 2018/1/28.
 */
class GeoTextView(ctx: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
        TextView(ctx, attrs, defStyleAttr, defStyleRes) {

    constructor(ctx: Context) : this(ctx, null, 0, 0)
    constructor(ctx: Context, attrs: AttributeSet?) : this(ctx, attrs, 0, 0)
    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(ctx, attrs, defStyleAttr, 0)


    companion object {

        private val TYPEFACE_CACHE_MAP = hashMapOf<String, Typeface>()

        fun parseTypefaceFromAssets(ctx: Context, path: String): Typeface {
            if (TYPEFACE_CACHE_MAP.containsKey(path)) {
                return TYPEFACE_CACHE_MAP[path]!!
            }
            val typeface = Typeface.createFromAsset(ctx.applicationContext.assets, path)
            TYPEFACE_CACHE_MAP[path] = typeface
            return typeface
        }
    }

    init {
        attrs?.let { ctx.obtainStyledAttributes(it, R.styleable.GeoTextView) }?.apply {
            getString(R.styleable.GeoTextView_typefaceFromAssets)?.let { typeface = parseTypefaceFromAssets(ctx, it) }
        }
    }
}