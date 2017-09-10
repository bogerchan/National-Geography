package me.boger.geographic.biz.selectpage

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import me.boger.geographic.core.AppConfiguration

/**
 * Created by BogerChan on 2017/6/30.
 */
class SelectPageItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
        super.getItemOffsets(outRect, view, parent, state)
        val dp5 = AppConfiguration.dp2px(5).toInt()
        outRect?.top = dp5 * 3
        outRect?.bottom = dp5 *3
        outRect?.left = dp5 * 3
        outRect?.right = dp5 * 3
    }
}