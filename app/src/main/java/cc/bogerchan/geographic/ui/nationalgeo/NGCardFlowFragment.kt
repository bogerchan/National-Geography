package cc.bogerchan.geographic.ui.nationalgeo

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Rect
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import cc.bogerchan.geographic.R
import cc.bogerchan.geographic.adapter.NGCardFlowAdapter
import cc.bogerchan.geographic.util.FetchStatus
import cc.bogerchan.geographic.util.dp2px
import cc.bogerchan.geographic.viewmodel.MainUIViewModel
import cc.bogerchan.geographic.viewmodel.natoinalgeo.NGCardFlowViewModel
import cc.bogerchan.geographic.viewmodel.natoinalgeo.NGCardShowViewModel
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.bezierlayout.BezierLayout
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter
import jp.wasabeef.recyclerview.animators.LandingAnimator
import java.lang.ref.WeakReference

/**
 * Created by Boger Chan on 2018/1/28.
 */
class NGCardFlowFragment : Fragment() {

    private class ScaleAnimatorHelper(val scaleTo: Float) {
        private var mPendingAnimator: Animator? = null
        private var mPendingView: WeakReference<View>? = null

        fun startScale(target: View) {
            mPendingAnimator?.cancel()
            restoreScale()
            mPendingAnimator = AnimatorSet().apply {
                play(ObjectAnimator.ofFloat(target, "scaleX", target.scaleX, scaleTo))
                        .with(ObjectAnimator.ofFloat(target, "scaleY", target.scaleY, scaleTo))
                duration = 500
                interpolator = OvershootInterpolator()
                start()
            }
            mPendingView = WeakReference(target)
        }

        fun restoreScale() {
            val target = mPendingView?.get() ?: return
            mPendingAnimator?.cancel()
            mPendingAnimator = AnimatorSet().apply {
                play(ObjectAnimator.ofFloat(target, "scaleX", target.scaleX, 1F))
                        .with(ObjectAnimator.ofFloat(target, "scaleY", target.scaleY, 1F))
                duration = 500
                interpolator = OvershootInterpolator()
                start()
            }
        }
    }

    private lateinit var llError: LinearLayout
    private lateinit var llLoading: LinearLayout
    private lateinit var trlContent: TwinklingRefreshLayout
    private lateinit var rvContent: RecyclerView
    private val mNGCardFlowViewModel by lazy { ViewModelProviders.of(activity).get(NGCardFlowViewModel::class.java) }
    private val mNGCardShowViewModel by lazy { ViewModelProviders.of(activity).get(NGCardShowViewModel::class.java) }
    private val mMainUIViewHolder by lazy { ViewModelProviders.of(activity).get(MainUIViewModel::class.java) }
    private val mCardDataAdapter by lazy { NGCardFlowAdapter() }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?) = inflater?.inflate(R.layout.fragment_ng_card_flow, null)?.apply {
        // bind view, butter-knife is not suitable for this scene.
        llError = findViewById(R.id.ll_fragment_ng_card_flow_error)
        llLoading = findViewById(R.id.ll_fragment_ng_card_flow_loading)
        trlContent = findViewById(R.id.trl_fragment_ng_card_flow)
        rvContent = findViewById(R.id.rv_fragment_ng_card_flow)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViews()
        bindViewModels()
        if (savedInstanceState == null) {
            mNGCardFlowViewModel.uiState.value = NGCardFlowViewModel.UIState.LOADING
            mNGCardFlowViewModel.startFetch(NGCardFlowViewModel.FetchType.FROM_FIRST)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbindViewModels()
    }

    private fun initViews() {
        rvContent.adapter = SlideInBottomAnimationAdapter(mCardDataAdapter)
        rvContent.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        rvContent.itemAnimator = LandingAnimator()
        rvContent.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
                super.getItemOffsets(outRect, view, parent, state)
                val dp5 = context.dp2px(5).toInt()
                outRect?.top = dp5 * 3
                outRect?.bottom = dp5 * 3
                outRect?.left = dp5 * 3
                outRect?.right = dp5 * 3
            }
        })
        rvContent.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {

            val aniHelper = ScaleAnimatorHelper(1.03F)
            val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                    e ?: return false
                    rvContent.findChildViewUnder(e.x, e.y)?.let {
                        performCardItemClicked(it, rvContent.getChildAdapterPosition(it))
                    }
                    return false
                }
            })

            override fun onInterceptTouchEvent(rv: RecyclerView?, e: MotionEvent?): Boolean {
                rv ?: return false
                gestureDetector.onTouchEvent(e)
                when (e?.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        rv.findChildViewUnder(e.x, e.y)?.let {
                            aniHelper.startScale(it)
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        aniHelper.restoreScale()
                    }
                }
                return false
            }
        })
        val bezierHeaderView = BezierLayout(activity)
        bezierHeaderView.setWaveColor(ResourcesCompat.getColor(resources, R.color.colorPrimaryDark20, activity.theme))
        bezierHeaderView.setRippleColor(ResourcesCompat.getColor(resources, R.color.colorPrimaryDark20, activity.theme))
        trlContent.setHeaderView(bezierHeaderView)
        val ballPulseView = BallPulseView(activity)
        ballPulseView.setNormalColor(ResourcesCompat.getColor(resources, R.color.colorAccent20, activity.theme))
        ballPulseView.setAnimatingColor(ResourcesCompat.getColor(resources, R.color.colorAccent20, activity.theme))
        trlContent.setBottomView(ballPulseView)
        trlContent.setOnRefreshListener(object : RefreshListenerAdapter() {

            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
                mNGCardFlowViewModel.uiState.value = NGCardFlowViewModel.UIState.REFRESH
                mNGCardFlowViewModel.startFetch(NGCardFlowViewModel.FetchType.FROM_FIRST)
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                mNGCardFlowViewModel.uiState.value = NGCardFlowViewModel.UIState.LOAD_MORE
                mNGCardFlowViewModel.startFetch(NGCardFlowViewModel.FetchType.FOR_NEXT)
            }
        })
        llError.setOnClickListener { mNGCardFlowViewModel.startFetch(NGCardFlowViewModel.FetchType.FOR_NEXT) }
    }

    private fun performCardItemClicked(target: View, position: Int) {
        mMainUIViewHolder.uiAction.value = MainUIViewModel.UIAction.LOADING
        mNGCardFlowViewModel.startQueryCardDataElements(mCardDataAdapter.cardData[position])
    }

    private fun bindViewModels() {
        mNGCardFlowViewModel.uiState.observe(this, Observer { uiState ->
            when (uiState) {
                NGCardFlowViewModel.UIState.LOADING -> {
                    llError.visibility = View.INVISIBLE
                    llLoading.visibility = View.VISIBLE
                    trlContent.visibility = View.INVISIBLE
                }
                NGCardFlowViewModel.UIState.ERROR -> {
                    llError.visibility = View.VISIBLE
                    llLoading.visibility = View.INVISIBLE
                    trlContent.visibility = View.INVISIBLE
                }
                NGCardFlowViewModel.UIState.REFRESH -> {
//                    llError.visibility = View.INVISIBLE
//                    llLoading.visibility = View.INVISIBLE
//                    trlContent.visibility = View.VISIBLE
//                    trlContent.startRefresh()
                }
                NGCardFlowViewModel.UIState.LOAD_MORE -> {
//                    llError.visibility = View.INVISIBLE
//                    llLoading.visibility = View.INVISIBLE
//                    trlContent.visibility = View.VISIBLE
//                    trlContent.startLoadMore()
                }
                NGCardFlowViewModel.UIState.FINISH_LOADING -> {
                    llError.visibility = View.INVISIBLE
                    llLoading.visibility = View.INVISIBLE
                    trlContent.visibility = View.VISIBLE
                }
                NGCardFlowViewModel.UIState.FINISH_REFRESH -> {
                    llError.visibility = View.INVISIBLE
                    llLoading.visibility = View.INVISIBLE
                    trlContent.visibility = View.VISIBLE
                    trlContent.finishRefreshing()
                }
                NGCardFlowViewModel.UIState.FINISH_LOAD_MORE -> {
                    llError.visibility = View.INVISIBLE
                    llLoading.visibility = View.INVISIBLE
                    trlContent.visibility = View.VISIBLE
                    trlContent.finishLoadmore()
                }
            }
        })
        mNGCardFlowViewModel.cardDataList.observe(this, Observer { resp ->
            when (resp?.first) {
                FetchStatus.SUCCESS -> {
                    mNGCardFlowViewModel.uiState.value = when (mNGCardFlowViewModel.uiState.value) {
                        NGCardFlowViewModel.UIState.LOADING -> NGCardFlowViewModel.UIState.FINISH_LOADING
                        NGCardFlowViewModel.UIState.REFRESH -> NGCardFlowViewModel.UIState.FINISH_REFRESH
                        NGCardFlowViewModel.UIState.LOAD_MORE -> NGCardFlowViewModel.UIState.FINISH_LOAD_MORE
                        else -> NGCardFlowViewModel.UIState.FINISH_LOADING
                    }
                    mCardDataAdapter.cardData = resp.second
                }
                FetchStatus.ERROR -> {
                    mNGCardFlowViewModel.uiState.value = NGCardFlowViewModel.UIState.ERROR
                }
                else -> {
                }
            }
        })
        mNGCardFlowViewModel.cardDataWithElements.observe(this, Observer {
            if (it == null) {
                return@Observer
            }
            if (it.first == FetchStatus.ERROR) {
                Snackbar.make(rvContent, R.string.tip_load_error, Snackbar.LENGTH_SHORT).show()
                return@Observer
            }
            mMainUIViewHolder.uiAction.value = MainUIViewModel.UIAction.FINISH_LOADING
            mMainUIViewHolder.uiAction.value = MainUIViewModel.UIAction.GO_TO_NG_SHOW_PAGE
            mNGCardShowViewModel.cardData.value = it.second
            mNGCardFlowViewModel.resetCardDataElements()
        })
    }

    private fun unbindViewModels() {
        mNGCardFlowViewModel.uiState.removeObservers(this)
        mNGCardFlowViewModel.cardDataList.removeObservers(this)
        mNGCardFlowViewModel.cardDataWithElements.removeObservers(this)
    }

}