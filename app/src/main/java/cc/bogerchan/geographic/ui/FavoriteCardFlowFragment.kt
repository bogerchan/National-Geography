package cc.bogerchan.geographic.ui

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import cc.bogerchan.geographic.R
import cc.bogerchan.geographic.adapter.FavoriteCardFlowAdapter
import cc.bogerchan.geographic.util.dp2px
import cc.bogerchan.geographic.util.FetchStatus
import cc.bogerchan.geographic.viewmodel.FavoriteCardFlowViewModel
import cc.bogerchan.geographic.viewmodel.MainUIViewModel
import cc.bogerchan.geographic.viewmodel.NGCardShowViewModel
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
class FavoriteCardFlowFragment : Fragment() {

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
    private val mFavoriteCardViewModel by lazy { ViewModelProviders.of(activity).get(FavoriteCardFlowViewModel::class.java) }
    private val mMainUIViewHolder by lazy { ViewModelProviders.of(activity).get(MainUIViewModel::class.java) }
    private val mNGCardShowViewModel by lazy { ViewModelProviders.of(activity).get(NGCardShowViewModel::class.java) }
    private val mCardDataAdapter by lazy { FavoriteCardFlowAdapter() }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater?.inflate(R.layout.fragment_favorite_card_flow, null)?.apply {
        llError = findViewById(R.id.ll_fragment_favorite_card_flow_error)
        llLoading = findViewById(R.id.ll_fragment_favorite_card_flow_loading)
        trlContent = findViewById(R.id.trl_fragment_favorite_card_flow)
        rvContent = findViewById(R.id.rv_fragment_favorite_card_flow)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViews()
        bindViewModels()
        mFavoriteCardViewModel.uiState.value = FavoriteCardFlowViewModel.UIState.LOADING
        if (mFavoriteCardViewModel.cardDataList.value == null) {
            mFavoriteCardViewModel.fetchCardDataList()
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
                        performCardItemClicked(rvContent.getChildAdapterPosition(it))
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
        trlContent.setEnableLoadmore(false)
        trlContent.setOnRefreshListener(object : RefreshListenerAdapter() {

            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
                mFavoriteCardViewModel.uiState.value = FavoriteCardFlowViewModel.UIState.REFRESH
                mFavoriteCardViewModel.fetchCardDataList()
            }
        })
        llError.setOnClickListener { mFavoriteCardViewModel.fetchCardDataList() }
    }

    private fun performCardItemClicked(position: Int) {
        mMainUIViewHolder.uiAction.value = MainUIViewModel.UIAction.GO_TO_NG_SHOW_PAGE
        mNGCardShowViewModel.cardData.value = mCardDataAdapter.cardData[position]
    }

    private fun bindViewModels() {
        mFavoriteCardViewModel.uiState.observe(this, Observer { uiState ->
            when (uiState) {
                FavoriteCardFlowViewModel.UIState.LOADING -> {
                    llError.visibility = View.INVISIBLE
                    llLoading.visibility = View.VISIBLE
                    trlContent.visibility = View.INVISIBLE
                }
                FavoriteCardFlowViewModel.UIState.FINISH_LOADING -> {
                    llError.visibility = View.INVISIBLE
                    llLoading.visibility = View.INVISIBLE
                    trlContent.visibility = View.VISIBLE
                }
                FavoriteCardFlowViewModel.UIState.ERROR -> {
                    llError.visibility = View.VISIBLE
                    llLoading.visibility = View.INVISIBLE
                    trlContent.visibility = View.INVISIBLE
                }
                FavoriteCardFlowViewModel.UIState.REFRESH -> {
//                    llError.visibility = View.INVISIBLE
//                    llLoading.visibility = View.INVISIBLE
//                    trlContent.visibility = View.VISIBLE
//                    trlContent.startRefresh()
                }
                FavoriteCardFlowViewModel.UIState.FINISH_REFRESH -> {
                    llError.visibility = View.INVISIBLE
                    llLoading.visibility = View.INVISIBLE
                    trlContent.visibility = View.VISIBLE
                    trlContent.finishRefreshing()
                }
            }
        })
        mFavoriteCardViewModel.cardDataList.observe(this, Observer { resp ->
            when (resp?.first) {
                FetchStatus.SUCCESS -> {
                    when (mFavoriteCardViewModel.uiState.value) {
                        FavoriteCardFlowViewModel.UIState.LOADING -> mFavoriteCardViewModel.uiState.value = FavoriteCardFlowViewModel.UIState.FINISH_LOADING
                        FavoriteCardFlowViewModel.UIState.REFRESH -> mFavoriteCardViewModel.uiState.value = FavoriteCardFlowViewModel.UIState.FINISH_REFRESH
                        else -> {
                        }
                    }
                    mCardDataAdapter.cardData = resp.second
                }
                FetchStatus.ERROR -> {
                    mFavoriteCardViewModel.uiState.value = FavoriteCardFlowViewModel.UIState.ERROR
                }
                else -> {
                }
            }
        })
        mFavoriteCardViewModel.ngCardElementUpdate.observe(this, Observer { resp ->
            when (resp?.first) {
                FetchStatus.SUCCESS -> {
                    mCardDataAdapter.cardData = resp.second
                }
                FetchStatus.ERROR -> {
                    mFavoriteCardViewModel.uiState.value = FavoriteCardFlowViewModel.UIState.ERROR
                }
                else -> {
                }
            }
        })
    }

    private fun unbindViewModels() {
        mFavoriteCardViewModel.uiState.removeObservers(this)
        mFavoriteCardViewModel.cardDataList.removeObservers(this)
        mFavoriteCardViewModel.ngCardElementUpdate.removeObservers(this)
    }
}