package me.boger.geographic.biz.detailpage

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import me.boger.geographic.R
import me.boger.geographic.biz.common.ContentType
import me.boger.geographic.core.AppConfiguration
import me.boger.geographic.core.NGFragment
import me.boger.geographic.core.NGUtil
import me.boger.geographic.view.SealedIconFont
import me.boger.geographic.view.SealedTextView
import java.io.Serializable
import java.util.*

/**
 * Created by BogerChan on 2017/6/30.
 */
class DetailPageFragment : NGFragment(), IDetailPageUI {

    companion object {
        val TAG = "DetailPageFragment"

        val KEY_FRAGMENT_NGDETAIL_INTERNAL_DATA = "key_fragment_ngdetail_internal_data"
    }

    private val mPresenter: IDetailPagePresenter by lazy { DetailPagePresenterImpl() }

    private val llcIntroAndMenu by lazy {
        view!!.findViewById(R.id.llc_fragment_ng_detail_intro_and_menu)
    }

    private val llcMenuShare by lazy {
        view!!.findViewById(R.id.llc_fragment_ng_detail_menu_share)
    }

    private val llcMenuSave by lazy {
        view!!.findViewById(R.id.llc_fragment_ng_detail_menu_save)
    }

    private val llcMenuFav by lazy {
        view!!.findViewById(R.id.llc_fragment_ng_detail_menu_fav)
    }

    private val llcMenu by lazy {
        view!!.findViewById(R.id.llc_fragment_ng_detail_menu)
    }

    private val llcLoading by lazy {
        view!!.findViewById(R.id.llc_fragment_ng_detail_loading)
    }

    private val tvTitle by lazy {
        view!!.findViewById(R.id.tv_fragment_ng_detail_title) as SealedTextView
    }

    private val tvPageIdx by lazy {
        view!!.findViewById(R.id.tv_fragment_ng_detail_page_idx) as SealedTextView
    }

    private val tvBody by lazy {
        view!!.findViewById(R.id.tv_fragment_ng_detail_body) as SealedTextView
    }

    private val vpContent by lazy {
        view!!.findViewById(R.id.vp_fragment_ng_detail) as ViewPager
    }

    private val tvMenuButton by lazy {
        view!!.findViewById(R.id.icon_fragment_ng_detail_menu) as SealedIconFont
    }

    private val tvMenuFavIcon by lazy {
        view!!.findViewById(R.id.icon_fragment_ng_detail_menu_fav) as SealedIconFont
    }

    private val vMenuDivider by lazy {
        view!!.findViewById(R.id.v_fragment_ng_detail_divider_menu)
    }

    private val ablTitleBar by lazy {
        activity!!.findViewById(R.id.abl_activity_main_ng_title)
    }

    private val mMenuDividerList by lazy {
        arrayOf(
                view!!.findViewById(R.id.v_fragment_ng_detail_divider_1),
                view!!.findViewById(R.id.v_fragment_ng_detail_divider_2),
                view!!.findViewById(R.id.v_fragment_ng_detail_divider_3)
        )
    }

    private var mPendingMenuAnimator: Animator? = null

    private var mPendingOverlayAnimator: Animator? = null

    private class InternalData(var id: String? = null,
                               var offlineData: DetailPageData? = null) : Serializable

    private lateinit var mInternalData: InternalData

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater!!.inflate(R.layout.fragment_ng_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        restoreDataIfNeed(savedInstanceState)
        mPresenter.restoreDataIfNeed(savedInstanceState)
        initView()
        mPresenter.init(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter.destroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        setOverlayMenuShown(true)
    }

    private fun restoreDataIfNeed(savedInstanceState: Bundle?) {
        if (savedInstanceState == null
                || !savedInstanceState.containsKey(KEY_FRAGMENT_NGDETAIL_INTERNAL_DATA)) {
            return
        }
        mInternalData = savedInstanceState.getSerializable(KEY_FRAGMENT_NGDETAIL_INTERNAL_DATA) as InternalData
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mPresenter.onSaveInstanceState(outState)
        if (outState == null) {
            return
        }
        outState.putSerializable(KEY_FRAGMENT_NGDETAIL_INTERNAL_DATA, mInternalData)
    }

    fun initData(id: String?, offlineData: DetailPageData?) {
        mInternalData = InternalData(id, offlineData)
    }

    fun initView() {
        val adapter = DetailPageAdapter()
        adapter.setOnItemClickListener(object : DetailPageAdapter.OnItemClickListener {

            private var show: Boolean = true

            override fun onItemClick(v: View, position: Int) {
                show = !show
                setOverlayMenuShown(show)
            }
        })
        vpContent.adapter = adapter
        vpContent.pageMargin = AppConfiguration.dp2px(10).toInt()
        vpContent.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {

            override fun onPageSelected(position: Int) {
                updateContent(adapter.data, position)
            }

        })
        llcIntroAndMenu.viewTreeObserver
                .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        setBottomMenuExpanded(false)
                        llcIntroAndMenu.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }

                })
        tvMenuButton.setOnClickListener(object : View.OnClickListener {

            private var expand: Boolean = false

            override fun onClick(p0: View?) {
                expand = !expand
                setBottomMenuExpanded(expand)
            }

        })
        llcMenuShare.setOnClickListener {
            mPresenter.shareNGDetailImage(
                    (vpContent.adapter as DetailPageAdapter).data[vpContent.currentItem].url)
        }
        llcMenuSave.setOnClickListener {
            mPresenter.saveNGDetailImage(
                    (vpContent.adapter as DetailPageAdapter).data[vpContent.currentItem].url)
        }
        llcMenuFav.setOnClickListener {
            mPresenter.setNGDetailItemFavoriteState(
                    (vpContent.adapter as DetailPageAdapter).data[vpContent.currentItem])
        }
    }

    override var contentType = ContentType.UNSET
        get() {
            return field
        }
        set(value) {
            when (value) {
                ContentType.LOADING -> {
                    llcLoading.visibility = View.VISIBLE
                    llcIntroAndMenu.visibility = View.INVISIBLE
                    vpContent.visibility = View.INVISIBLE
                    tvMenuButton.visibility = View.INVISIBLE
                }
                ContentType.CONTENT -> {
                    llcLoading.visibility = View.INVISIBLE
                    llcIntroAndMenu.visibility = View.VISIBLE
                    vpContent.visibility = View.VISIBLE
                    tvMenuButton.visibility = View.VISIBLE
                }
                ContentType.ERROR -> {
                    Snackbar.make(view, R.string.tip_load_error, Snackbar.LENGTH_SHORT).show()
                    fragmentManager.beginTransaction().remove(this).commit()
                }
                else -> {
                }
            }
            field = value
        }

    override fun refreshData(data: List<DetailPagePictureData>) {
        if (data.isEmpty()) {
            return
        }
        val adapter = vpContent.adapter as DetailPageAdapter
        adapter.data = data
        adapter.notifyDataSetChanged()
        vpContent.currentItem = 0
        updateContent(data, 0)
    }

    private fun updateContent(dataList: List<DetailPagePictureData>, idx: Int) {
        val data = dataList[idx]
        val localeData = data.locale()
        tvTitle.text = localeData.title
        tvPageIdx.text = String.format(Locale.US, "%2d/%2d", idx + 1, dataList.size)
        tvBody.text = String.format(Locale.US, getResourceString(R.string.template_detail_text_body), localeData.content, localeData.author)
        setFavoriteButtonState(data.favorite)
    }

    private fun setOverlayMenuShown(show: Boolean) {
        mPendingOverlayAnimator?.cancel()
//        val titleBar = mMainUIController.getTitleBar()
        val range = if (show) arrayOf(ablTitleBar.alpha, 1f) else arrayOf(ablTitleBar.alpha, 0f)
        val ani = ValueAnimator.ofFloat(*range.toFloatArray())
        ani.duration = 300
        ani.interpolator = LinearInterpolator()
        ani.addUpdateListener {
            val value = it.animatedValue as Float
            ablTitleBar.alpha = value
            if (isVisible) {
                llcIntroAndMenu.alpha = value
                tvMenuButton.alpha = value
            }
        }
        ani.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                mPendingOverlayAnimator = ani
                if (show) {
                    ablTitleBar.visibility = View.VISIBLE
                    if (isVisible) {
                        llcIntroAndMenu.visibility = View.VISIBLE
                        tvMenuButton.visibility = View.VISIBLE
                    }
                }
            }

            override fun onAnimationEnd(animation: Animator?) {
                mPendingOverlayAnimator = null
                if (!show) {
                    ablTitleBar.visibility = View.INVISIBLE
                    if (isVisible) {
                        llcIntroAndMenu.visibility = View.INVISIBLE
                        tvMenuButton.visibility = View.INVISIBLE
                    }
                }
            }
        })
        ani.start()
    }

    private fun setBottomMenuExpanded(expand: Boolean) {
        mPendingMenuAnimator?.cancel()
        val iconText = if (expand) "\ue649" else "\ue6e5"
        val range = if (expand) arrayOf(llcIntroAndMenu.translationY, 0f) else
            arrayOf(llcIntroAndMenu.translationY, (llcIntroAndMenu.height - vMenuDivider.top).toFloat())
        val ani = ValueAnimator.ofFloat(*range.toFloatArray())
        ani.duration = 500
        ani.interpolator = OvershootInterpolator()
        ani.addUpdateListener {
            val value = it.animatedValue as Float
            llcIntroAndMenu.translationY = value
            val fraction = it.animatedFraction
            tvMenuButton.rotation = fraction
            if (fraction > .5f) {
                tvMenuButton.alpha = (fraction - 0.5f) * 2
                if (iconText != tvMenuButton.text) {
                    tvMenuButton.text = iconText
                }
            } else {
                tvMenuButton.alpha = 1 - fraction * 2
            }
        }
        ani.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                mPendingMenuAnimator = ani
                if (!isVisible) {
                    return
                }
                if (expand) {
                    llcMenuShare.visibility = View.VISIBLE
                    llcMenuSave.visibility = View.VISIBLE
                    llcMenuFav.visibility = View.VISIBLE
                    mMenuDividerList.forEach {
                        it.visibility = View.VISIBLE
                    }
                    tvBody.maxLines = Int.MAX_VALUE
                } else {
                    tvBody.maxLines = 4
                }
            }

            override fun onAnimationEnd(animation: Animator?) {
                mPendingMenuAnimator = null
                if (!isVisible) {
                    return
                }
                if (!expand) {
                    llcMenuShare.visibility = View.INVISIBLE
                    llcMenuSave.visibility = View.INVISIBLE
                    llcMenuFav.visibility = View.INVISIBLE
                    mMenuDividerList.forEach {
                        it.visibility = View.INVISIBLE
                    }
                }
            }
        })
        ani.start()
    }

    override fun showTipMessage(msg: String) {
        if (NGUtil.isUIThread()) {
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
        } else {
            activity.runOnUiThread {
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun showTipMessage(msgId: Int) {
        Toast.makeText(activity, activity.getString(msgId), Toast.LENGTH_SHORT).show()
    }

    override fun getResourceString(id: Int): String = activity.getString(id)

    override fun getContentResolver(): ContentResolver = activity.contentResolver

    override fun sendBroadcast(intent: Intent) {
        activity.sendBroadcast(intent)
    }

    override fun setFavoriteButtonState(favorite: Boolean) {
        tvMenuFavIcon.text = if (favorite) "\ue677" else "\ue603"
    }

    override fun hasOfflineData(): Boolean = mInternalData.offlineData != null

    override fun getOfflineData(): DetailPageData = mInternalData.offlineData!!

    override fun getNGDetailDataId(): String = mInternalData.id!!
}