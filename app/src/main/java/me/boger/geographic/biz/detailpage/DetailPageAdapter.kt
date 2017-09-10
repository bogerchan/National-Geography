package me.boger.geographic.biz.detailpage

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import me.boger.geographic.R

/**
 * Created by BogerChan on 2017/7/1.
 */
class DetailPageAdapter(var data: List<DetailPagePictureData> = emptyList()) : PagerAdapter() {

    interface OnItemClickListener {
        fun onItemClick(v: View, position: Int)
    }

    private val mIdleViewList by lazy { arrayListOf<SimpleDraweeView>() }
    private val mViewMap by lazy { linkedMapOf<Int, SimpleDraweeView>() }

    private var mOnItemClickListener: OnItemClickListener? = null

    override fun isViewFromObject(view: View?, `object`: Any?): Boolean = view == `object`

    override fun getCount(): Int = data.size

    override fun instantiateItem(container: ViewGroup?, position: Int): Any {
        val v = if (mIdleViewList.size == 0) SimpleDraweeView(container!!.context).reset()
        else mIdleViewList.removeAt(0)
        mViewMap[position] = v
        v.setImageURI(data[position].url)
        container!!.addView(v,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        v.setOnClickListener {
            mOnItemClickListener?.onItemClick(v, position)
        }
        return v
    }

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
        val v = mViewMap.remove(position)
        mIdleViewList.add(v!!.reset())
        container!!.removeView(v)
    }

    fun SimpleDraweeView.reset(): SimpleDraweeView {
        val h = this.hierarchy
        h.actualImageScaleType = ScalingUtils.ScaleType.FIT_CENTER
        h.setPlaceholderImage(R.mipmap.placeholder_loading, ScalingUtils.ScaleType.FIT_CENTER)
        controller = null
        return this
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mOnItemClickListener = listener
    }
}