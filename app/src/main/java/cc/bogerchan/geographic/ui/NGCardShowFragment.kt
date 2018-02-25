package cc.bogerchan.geographic.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.util.ArraySet
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import cc.bogerchan.geographic.R
import cc.bogerchan.geographic.dao.NGCardElementData
import cc.bogerchan.geographic.util.FetchStatus
import cc.bogerchan.geographic.util.Timber
import cc.bogerchan.geographic.util.dp2px
import cc.bogerchan.geographic.viewmodel.FavoriteCardFlowViewModel
import cc.bogerchan.geographic.viewmodel.MainUIViewModel
import cc.bogerchan.geographic.viewmodel.NGCardShowViewModel
import com.alexvasilkov.gestures.GestureController
import com.alexvasilkov.gestures.Settings
import com.alexvasilkov.gestures.views.GestureFrameLayout
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.postprocessors.IterativeBoxBlurPostProcessor
import com.facebook.imagepipeline.request.ImageRequestBuilder
import util.bindView

/**
 * Created by Boger Chan on 2018/1/28.
 */
class NGCardShowFragment : Fragment() {

    private class NGCardShowPagerAdapter(val viewPager: ViewPager, val cardDataElements: List<NGCardElementData>, val onClickListener: (View) -> Unit) : PagerAdapter() {

        private val mRecyclerPool = arrayListOf<GestureFrameLayout>()

        override fun isViewFromObject(view: View?, `object`: Any?) = view == `object`

        override fun getCount(): Int {
            return cardDataElements.size
        }

        override fun instantiateItem(container: ViewGroup?, position: Int): Any {
            if (mRecyclerPool.isNotEmpty()) {
                Timber.d("Use recycler SimpleDraweeView instance.")
                return mRecyclerPool.removeAt(0).apply {
                    (getChildAt(0) as SimpleDraweeView).apply {
                        setImageURI(cardDataElements[position].imgUrl)
                    }
                    container!!.addView(this, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                }
            }
            return GestureFrameLayout(container!!.context).apply {
                configureGestureView(this)
                addView(SimpleDraweeView(container.context).apply {
                    hierarchy.apply {
                        actualImageScaleType = ScalingUtils.ScaleType.FIT_CENTER
                        setPlaceholderImage(R.mipmap.placeholder_loading, ScalingUtils.ScaleType.FIT_CENTER)
                        setImageURI(cardDataElements[position].imgUrl)
                    }
                }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                container.addView(this, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            }
        }

        fun getCardElement(position: Int) = cardDataElements[position]

        override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
            container ?: return
            `object` ?: return
            val gfl = `object` as GestureFrameLayout
            container.removeView(gfl)
            mRecyclerPool.add((gfl.apply {
                (getChildAt(0) as SimpleDraweeView).apply {
                    setImageURI(null as String?)
                }
            }))
        }

        private fun configureGestureView(view: GestureFrameLayout) {
            view.controller.apply {
                settings.apply {
                    maxZoom = 2F
                    doubleTapZoom = 2F
                    isZoomEnabled = true
                    isDoubleTapEnabled = true
                    isRotationEnabled = false
                    isRestrictRotation = false
                    overzoomFactor = 2F
                    isFillViewport = false
                    fitMethod = Settings.Fit.INSIDE
                    gravity = Gravity.CENTER
                }
                setOnGesturesListener(object : GestureController.SimpleOnGestureListener() {
                    override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
                        onClickListener(view)
                        return false
                    }
                })
                enableScrollInViewPager(viewPager)
            }
        }
    }

    private val vpContent by bindView<ViewPager>(R.id.vp_fragment_ng_card_show)
    private val llBottom by bindView<LinearLayout>(R.id.ll_fragment_ng_card_show_bottom)
    private val tvTitle by bindView<TextView>(R.id.gtv_fragment_ng_card_show_intro_title)
    private val tvPage by bindView<TextView>(R.id.tv_fragment_ng_card_show_intro_page)
    private val tvContent by bindView<TextView>(R.id.tv_fragment_ng_card_show_intro_content)
    private val vMenuDividerTop by bindView<View>(R.id.v_fragment_ng_card_show_menu_divider_top)
    private val llShare by bindView<LinearLayout>(R.id.ll_fragment_ng_card_show_menu_share)
    private val llSave by bindView<LinearLayout>(R.id.ll_fragment_ng_card_show_menu_save)
    private val llFav by bindView<LinearLayout>(R.id.ll_fragment_ng_card_show_menu_fav)
    private val tvFavIcon by bindView<TextView>(R.id.gtv_fragment_ng_card_show_menu_fav_icon)
    private val tvMenuIndicator by bindView<TextView>(R.id.gtv_fragment_ng_card_show_menu_indicator)
    private val sdvBackgroundLayer by bindView<SimpleDraweeView>(R.id.sdv_fragment_ng_card_show_background_layer)

    private val mNGCardShowViewModel by lazy { ViewModelProviders.of(activity).get(NGCardShowViewModel::class.java) }
    private val mMainUIViewModel by lazy { ViewModelProviders.of(activity).get(MainUIViewModel::class.java) }
    private val mFavoriteCardFlowViewModel by lazy { ViewModelProviders.of(activity).get(FavoriteCardFlowViewModel::class.java) }

    private var mPendingMenuAnimator: Animator? = null
    private var mPendingMenuVisibleAnimator: Animator? = null
    private val mFrescoDraweeController by lazy { Fresco.newDraweeControllerBuilder() }
    private val mBlurProcessor by lazy { IterativeBoxBlurPostProcessor(6, 15) }
    private val mFavoriteItems by lazy { ArraySet<Int>() }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?) = inflater?.inflate(R.layout.fragment_ng_card_show, null)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViews()
        bindViewModels()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbindViewModels()
    }

    private fun initViews() {
        vpContent.pageMargin = context.dp2px(15).toInt()
        vpContent.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                val adapter = vpContent.adapter as NGCardShowPagerAdapter
                adapter.getCardElement(position).apply {
                    tvTitle.text = title
                    tvPage.text = getString(R.string.text_page_indicator, position + 1, adapter.count)
                    tvContent.text = getString(R.string.text_ng_card_show_content, content, author)
                    sdvBackgroundLayer.controller = mFrescoDraweeController.apply {
                        oldController = sdvBackgroundLayer.controller
                        imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(imgUrl)).apply {
                            postprocessor = mBlurProcessor
                        }.build()
                    }.build()
                    sdvBackgroundLayer.startAnimation(AlphaAnimation(0F, 1F).apply { duration = 200 })
                    tvFavIcon.text = if (mFavoriteItems.contains(id)) getString(R.string.ic_fav_solid) else getString(R.string.ic_fav)
                }
            }
        })
        val divider = vMenuDividerTop
        llBottom.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                llBottom.viewTreeObserver.removeOnGlobalLayoutListener(this)
                context ?: return
                llBottom.translationY = llBottom.measuredHeight - divider.top.toFloat()
                tvContent.maxLines = 4
            }

        })
        tvMenuIndicator.setOnClickListener {
            when (tvMenuIndicator.text) {
                getString(R.string.ic_more) -> {
                    mPendingMenuAnimator?.cancel()
                    mPendingMenuAnimator = ObjectAnimator.ofFloat(llBottom, "translationY", llBottom.translationY, 0F).apply {
                        duration = 300
                        interpolator = OvershootInterpolator()
                        start()
                    }
                    tvContent.maxLines = Int.MAX_VALUE
                    tvMenuIndicator.text = getString(R.string.ic_double_arrow_down)
                }
                getString(R.string.ic_double_arrow_down) -> {
                    mPendingMenuAnimator?.cancel()
                    mPendingMenuAnimator = ObjectAnimator.ofFloat(llBottom, "translationY", llBottom.translationY, llBottom.height - vMenuDividerTop.top.toFloat()).apply {
                        duration = 300
                        interpolator = OvershootInterpolator()
                        start()
                    }
                    tvContent.maxLines = 4
                    tvMenuIndicator.text = getString(R.string.ic_more)
                }
            }
        }
        llFav.setOnClickListener {
            val cardDataElements = mNGCardShowViewModel.cardData.value?.cardElements
            if (cardDataElements == null || cardDataElements.isEmpty()) {
                return@setOnClickListener
            }
            if (tvFavIcon.text == getString(R.string.ic_fav)) {
                mFavoriteCardFlowViewModel.addNGCardElementData(cardDataElements[vpContent.currentItem])
            } else {
                mFavoriteCardFlowViewModel.removeNGCardElementData(cardDataElements[vpContent.currentItem])
            }
        }
    }

    private fun bindViewModels() {
        mNGCardShowViewModel.cardData.observe(this, Observer { data ->
            val cardElements = data?.cardElements
            if (cardElements == null || cardElements.isEmpty()) {
                Snackbar.make(llBottom, R.string.tip_empty_data, Snackbar.LENGTH_SHORT).show()
                mMainUIViewModel.menuState.value = MainUIViewModel.MenuState.BACK_FROM_RETURN
                return@Observer
            }
            vpContent.adapter = NGCardShowPagerAdapter(vpContent, cardElements.toList(), {
                mMainUIViewModel.isTitleShowing.value = when (mMainUIViewModel.isTitleShowing.value) {
                    false -> true
                    else -> false
                }
                mNGCardShowViewModel.isMenuShowing.value = when (mNGCardShowViewModel.isMenuShowing.value) {
                    false -> true
                    else -> false
                }
            }).apply {
                getCardElement(0).apply {
                    tvTitle.text = title
                    tvPage.text = getString(R.string.text_page_indicator, 1, count)
                    tvContent.text = getString(R.string.text_ng_card_show_content, content, author)
                    sdvBackgroundLayer.controller = mFrescoDraweeController.apply {
                        oldController = sdvBackgroundLayer.controller
                        imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(imgUrl)).apply {
                            postprocessor = mBlurProcessor
                        }.build()
                        tvFavIcon.text = if (mFavoriteItems.contains(id)) getString(R.string.ic_fav_solid) else getString(R.string.ic_fav)
                    }.build()
                }
            }
        })
        mNGCardShowViewModel.isMenuShowing.observe(this, Observer {
            mPendingMenuVisibleAnimator?.cancel()
            mPendingMenuVisibleAnimator = when (it) {
                false -> {
                    AnimatorSet().apply {
                        play(ObjectAnimator.ofFloat(llBottom, "alpha", llBottom.alpha, 0F))
                                .with(ObjectAnimator.ofFloat(tvMenuIndicator, "alpha", llBottom.alpha, 0F))
                    }.apply {
                                duration = 200
                                addListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator?) {
                                        llBottom.visibility = View.INVISIBLE
                                        tvMenuIndicator.visibility = View.INVISIBLE
                                    }
                                })
                                start()
                            }
                }
                else -> {
                    AnimatorSet().apply {
                        play(ObjectAnimator.ofFloat(llBottom, "alpha", llBottom.alpha, 1F))
                                .with(ObjectAnimator.ofFloat(tvMenuIndicator, "alpha", llBottom.alpha, 1F))
                    }.apply {
                                duration = 200
                                addListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationStart(animation: Animator?) {
                                        llBottom.visibility = View.VISIBLE
                                        tvMenuIndicator.visibility = View.VISIBLE
                                    }
                                })
                                start()
                            }
                }
            }
        })
        mFavoriteCardFlowViewModel.cardDataList.observe(this, Observer {
            if (it?.first == null) {
                return@Observer
            }
            when (it.first) {
                FetchStatus.SUCCESS -> {
                    mFavoriteItems.clear()
                    if (it.second.isNotEmpty()) {
                        it.second[0].cardElements?.mapTo(mFavoriteItems, { it.id })
                    }
                    val cardElements = (vpContent.adapter as NGCardShowPagerAdapter).cardDataElements
                    if (cardElements.isEmpty()) {
                        Snackbar.make(llBottom, R.string.tip_empty_data, Snackbar.LENGTH_SHORT).show()
                        mMainUIViewModel.menuState.value = MainUIViewModel.MenuState.BACK_FROM_RETURN
                    } else {
                        cardElements[vpContent.currentItem].apply {
                            tvFavIcon.text = if (mFavoriteItems.contains(id)) getString(R.string.ic_fav_solid) else getString(R.string.ic_fav)
                        }
                    }
                }
                else -> {
                }
            }
        })
    }

    private fun unbindViewModels() {
        mNGCardShowViewModel.cardData.removeObservers(this)
        mNGCardShowViewModel.isMenuShowing.removeObservers(this)
        mFavoriteCardFlowViewModel.cardDataList.removeObservers(this)
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation {
        return AnimationUtils.loadAnimation(context, if (enter) android.R.anim.fade_in else android.R.anim.fade_out)
    }

    override fun onDestroy() {
        super.onDestroy()
        mMainUIViewModel.isTitleShowing.value = true
        mNGCardShowViewModel.isMenuShowing.value = true
    }
}