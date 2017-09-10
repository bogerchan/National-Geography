package me.boger.geographic.view

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import me.boger.geographic.core.LocalizationWorker

/**
 * Created by BogerChan on 2017/7/11.
 */
class SealedTextView(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int)
    : TextView(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context) : this(context, null, 0, 0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0, 0)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    init {
        typeface = LocalizationWorker.curTypeface
    }
}