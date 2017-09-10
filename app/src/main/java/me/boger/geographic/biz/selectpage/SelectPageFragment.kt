package me.boger.geographic.biz.selectpage

import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.bezierlayout.BezierLayout
import jp.wasabeef.recyclerview.adapters.AnimationAdapter
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter
import jp.wasabeef.recyclerview.animators.LandingAnimator
import me.boger.geographic.R
import me.boger.geographic.biz.common.ContentType
import me.boger.geographic.core.NGConstants
import me.boger.geographic.core.NGFragment
import me.boger.geographic.view.SealedTextView

/**
 * Created by BogerChan on 2017/6/27.
 */
class SelectPageFragment(
        var albumSelectedListener: (SelectPageAlbumData) -> Unit = {}) : NGFragment(), ISelectPageUI {

    companion object {
        val TAG = "SelectPageFragment"
    }

    private val mPresenter: ISelectPagePresenter by lazy {
        SelectPagePresenterImpl()
    }

    private val rvContent: RecyclerView by lazy {
        view!!.findViewById(R.id.rv_fragment_select_date) as RecyclerView
    }

    private val trlContent by lazy {
        view!!.findViewById(R.id.trl_select_date) as TwinklingRefreshLayout
    }

    private val tvLoading by lazy {
        view!!.findViewById(R.id.tv_fragment_select_date_loading) as SealedTextView
    }

    private val llLoading by lazy {
        view!!.findViewById(R.id.ll_fragment_select_date_loading)
    }

    private val llError by lazy {
        view!!.findViewById(R.id.ll_fragment_select_date_error)
    }

    private val tvError by lazy {
        view!!.findViewById(R.id.tv_fragment_select_date_error) as SealedTextView
    }

    private val tvErrorIcon by lazy {
        view!!.findViewById(R.id.icon_fragment_select_date_error_icon) as SealedTextView
    }

    override var contentType = ContentType.UNSET
        get() {
            return field
        }
        set(value) {
            when (value) {
                ContentType.LOADING -> {
                    llLoading.visibility = View.VISIBLE
                    llError.visibility = View.INVISIBLE
                    trlContent.visibility = View.INVISIBLE
                }
                ContentType.CONTENT -> {
                    llLoading.visibility = View.INVISIBLE
                    llError.visibility = View.INVISIBLE
                    trlContent.visibility = View.VISIBLE
                }
                ContentType.ERROR -> {
                    llLoading.visibility = View.INVISIBLE
                    llError.visibility = View.VISIBLE
                    trlContent.visibility = View.INVISIBLE
                }
                else -> {
                }
            }
            field = value
        }

    override fun setOnRetryClickListener(listener: (view: View) -> Unit) {
        llError.setOnClickListener(listener)
    }

    override fun setEnableLoadMore(canLoadMore: Boolean) {
        trlContent.setEnableLoadmore(canLoadMore)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater!!.inflate(R.layout.fragment_select_date, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
        mPresenter.restoreDataIfNeed(savedInstanceState)
        mPresenter.init(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter.destroy()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mPresenter.onSaveInstanceState(outState)
    }

    private fun init() {
        initViews()
    }

    private fun initViews() {
        rvContent.adapter = SlideInBottomAnimationAdapter(SelectPageAdapter(albumSelectedListener))
        rvContent.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        rvContent.itemAnimator = LandingAnimator()
        rvContent.addItemDecoration(SelectPageItemDecoration())
        val bezierHeaderView = BezierLayout(activity)
        bezierHeaderView.setWaveColor(ResourcesCompat.getColor(resources, R.color.color_gray_50, activity.theme))
        bezierHeaderView.setRippleColor(ResourcesCompat.getColor(resources, R.color.color_gray_dark, activity.theme))
        trlContent.setHeaderView(bezierHeaderView)
        val ballPulseView = BallPulseView(activity)
        ballPulseView.setNormalColor(ResourcesCompat.getColor(resources, R.color.ng_yellow_50, activity.theme))
        ballPulseView.setAnimatingColor(ResourcesCompat.getColor(resources, R.color.ng_yellow_50, activity.theme))
        trlContent.setBottomView(ballPulseView)
    }

    override fun getContentView(): View {
        return trlContent
    }

    override fun refreshFavoriteData(favoriteData: SelectPageAlbumData) {
        val adapter = (rvContent.adapter as AnimationAdapter).wrappedAdapter as SelectPageAdapter
        if (adapter.listData.isNotEmpty()) {
            adapter.listData[0] = favoriteData
        }
        adapter.notifyDataSetChanged()
    }

    override fun refreshCardData(data: List<SelectPageAlbumData>, append: Boolean) {
        val adapter = (rvContent.adapter as AnimationAdapter).wrappedAdapter as SelectPageAdapter
        if (append) adapter.listData.addAll(data)
        else adapter.listData = data.toMutableList()
        adapter.notifyDataSetChanged()
    }

    override fun finishLoadMore() {
        trlContent.finishLoadmore()
    }

    override fun finishRefreshing() {
        trlContent.finishRefreshing()
    }

    override fun setOnRefreshListener(
            onRefresh: (ISelectPageUI) -> Unit,
            onLoadMore: (ISelectPageUI) -> Unit) {
        trlContent.setOnRefreshListener(object : RefreshListenerAdapter() {
            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
                onRefresh(this@SelectPageFragment)
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                onLoadMore(this@SelectPageFragment)
            }
        })
    }

    override fun getBroadcastReceiverAction(): Array<String>?
            = arrayOf(NGConstants.ACTION_FAVORITE_DATA_CHANGED)

    override fun onLocalBroadcastReceive(action: String, data: Bundle?) {
        if (action == NGConstants.ACTION_FAVORITE_DATA_CHANGED) {
            mPresenter.notifyFavoriteNGDetailDataChanged()
        }
    }
}