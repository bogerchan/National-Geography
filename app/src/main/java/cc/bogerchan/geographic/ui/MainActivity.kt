package cc.bogerchan.geographic.ui

import android.animation.*
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.support.design.widget.AppBarLayout
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import cc.bogerchan.geographic.BuildConfig
import cc.bogerchan.geographic.R
import cc.bogerchan.geographic.util.CommonUtil
import cc.bogerchan.geographic.view.GeoTextView
import cc.bogerchan.geographic.viewmodel.FavoriteCardFlowViewModel
import cc.bogerchan.geographic.viewmodel.MainUIViewModel
import util.bindView
import java.util.*

/**
 * Created by Boger Chan on 2018/1/28.
 */
class MainActivity : AppCompatActivity() {

    private class DoubleTapDetector {

        private var mLastRecord: Long = 0L

        fun detect(): Boolean {
            val curTime = SystemClock.elapsedRealtime()
            return if (curTime - mLastRecord < 2000L) {
                mLastRecord = curTime
                true
            } else {
                mLastRecord = curTime
                false
            }
        }
    }

    private val gtvBtnMenu by bindView<TextView>(R.id.gtv_activity_main_menu)
    private val svMenu by bindView<ScrollView>(R.id.sv_activity_main_overlay_menu)
    private val tlTabs by bindView<TabLayout>(R.id.tl_activity_main_tabs)
    private val vpContent by bindView<ViewPager>(R.id.vp_activity_main_content)
    private val llLoading by bindView<View>(R.id.ll_activity_main_loading)
    private val ablTitle by bindView<AppBarLayout>(R.id.abl_activity_main_title)
    private val llContent by bindView<LinearLayout>(R.id.ll_activity_main_content)
    private val mMainUIViewModel by lazy { ViewModelProviders.of(this).get(MainUIViewModel::class.java) }
    private val mFavoriteCardFlowViewModel by lazy { ViewModelProviders.of(this).get(FavoriteCardFlowViewModel::class.java) }
    private var mPendingTitleVisibleAnimator: Animator? = null
    private var mPendingMenuAnimator: Animator? = null
    private val mIconMenu by lazy { getString(R.string.ic_menu) }
    private val mIconCross by lazy { getString(R.string.ic_cross) }
    private val mIconReturn by lazy { getString(R.string.ic_return) }
    private val mQuitDoubleTapDetector by lazy { DoubleTapDetector() }
    private var mPendingLoadingAnimator: Animator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = Color.TRANSPARENT
        }
        initViews()
        bindViewModels()
        mFavoriteCardFlowViewModel.fetchCardDataList()
    }

    override fun onBackPressed() {
        when {
            mMainUIViewModel.menuState.value == MainUIViewModel.MenuState.SHOW_RETURN -> {
                mMainUIViewModel.menuState.value = MainUIViewModel.MenuState.BACK_FROM_RETURN
            }
            mMainUIViewModel.menuState.value == MainUIViewModel.MenuState.SHOW_MENU -> {
                mMainUIViewModel.menuState.value = MainUIViewModel.MenuState.CLOSE_MENU
            }
            !mQuitDoubleTapDetector.detect() -> {
                Snackbar.make(vpContent, R.string.tip_quit_confirm, Snackbar.LENGTH_SHORT).show()
            }
            else -> {
                startActivity(Intent(Intent.ACTION_MAIN).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    addCategory(Intent.CATEGORY_HOME)
                })
            }
        }
    }

    private fun initViews() {
        initMenuItems()
        initViewPagerContent()
        initTabBar()
        gtvBtnMenu.setOnClickListener {
            when (gtvBtnMenu.text) {
                mIconMenu -> mMainUIViewModel.menuState.value = MainUIViewModel.MenuState.SHOW_MENU
                mIconCross -> mMainUIViewModel.menuState.value = MainUIViewModel.MenuState.CLOSE_MENU
                mIconReturn -> mMainUIViewModel.menuState.value = MainUIViewModel.MenuState.BACK_FROM_RETURN
            }
        }
    }

    private fun bindViewModels() {
        mMainUIViewModel.menuState.observe(this, Observer { menuState ->
            when (menuState) {
                MainUIViewModel.MenuState.SHOW_MENU -> openMenu()
                MainUIViewModel.MenuState.CLOSE_MENU -> closeMenu()
                MainUIViewModel.MenuState.SHOW_RETURN -> showReturn()
                MainUIViewModel.MenuState.BACK_FROM_RETURN -> backFromReturn()
            }
        })
        mMainUIViewModel.uiAction.observe(this, Observer { state ->
            when (state) {
                MainUIViewModel.UIAction.LOADING -> {
                    mPendingLoadingAnimator?.cancel()
                    mPendingLoadingAnimator = ObjectAnimator.ofFloat(llLoading, "alpha", llLoading.alpha, 1f).apply {
                        duration = 200
                        addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationStart(animation: Animator?) {
                                llLoading.visibility = View.VISIBLE
                            }
                        })
                        start()
                    }
                }
                MainUIViewModel.UIAction.FINISH_LOADING -> {
                    mPendingLoadingAnimator?.cancel()
                    mPendingLoadingAnimator = ObjectAnimator.ofFloat(llLoading, "alpha", llLoading.alpha, 0f).apply {
                        duration = 200
                        addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                llLoading.visibility = View.INVISIBLE
                            }
                        })
                        start()
                    }
                }
                MainUIViewModel.UIAction.GO_TO_NG_SHOW_PAGE -> {
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.fl_activity_main_full_content, NGCardShowFragment())
                            .addToBackStack(null).commit()
                    mMainUIViewModel.menuState.value = MainUIViewModel.MenuState.SHOW_RETURN
                    mMainUIViewModel.uiAction.value = MainUIViewModel.UIAction.NORMAL
                }
                else -> {
                }
            }
        })
        mMainUIViewModel.isTitleShowing.observe(this, Observer {
            mPendingTitleVisibleAnimator?.cancel()
            mPendingTitleVisibleAnimator = when (it) {
                false -> {
                    ObjectAnimator.ofFloat(ablTitle, "alpha", ablTitle.alpha, 0F).apply {
                        duration = 200
                        addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                ablTitle.visibility = View.INVISIBLE
                            }
                        })
                        start()
                    }
                }
                else -> {
                    ObjectAnimator.ofFloat(ablTitle, "alpha", ablTitle.alpha, 1F).apply {
                        duration = 200
                        addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationStart(animation: Animator?) {
                                ablTitle.visibility = View.VISIBLE
                            }
                        })
                        start()
                    }
                }
            }
        })
    }

    private fun initTabBar() {
        tlTabs.setupWithViewPager(vpContent)
        // Change font
        val textFontPath = getString(R.string.app_text_font)
        CommonUtil.walkViewTree(tlTabs, {
            if (it is TextView) {
                it.typeface = GeoTextView.parseTypefaceFromAssets(this, textFontPath)
            }
        })
    }

    private fun initViewPagerContent() {
        val vpContentData = arrayListOf(Pair<String, Fragment>(getString(R.string.tab_national_geography), NGCardFlowFragment()),
//                Pair<String, Fragment>(getString(R.string.tab_bing_wallpaper), Fragment()),
                Pair<String, Fragment>(getString(R.string.tab_favorite_gallery), FavoriteCardFlowFragment()))
        vpContent.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int) = vpContentData[position].second

            override fun getCount() = vpContentData.size

            override fun getPageTitle(position: Int) = vpContentData[position].first

        }
    }

    private fun initMenuItems() {
        findViewById<LinearLayout>(R.id.llc_activity_main_overlay_menu).apply {
            addView(createOverlayMenuItem(
                    iconLeft = getString(R.string.ic_update),
                    name = getString(R.string.menu_update_app),
                    iconRight = getString(R.string.ic_arrow_right),
                    value = "v${BuildConfig.VERSION_NAME}",
                    listener = {
                        //TODO
                    }))
            addView(createOverlayMenuItem(
                    iconLeft = getString(R.string.ic_info),
                    name = getString(R.string.menu_duty),
                    iconRight = getString(R.string.ic_arrow_right),
                    value = "",
                    listener = {
                        AlertDialog.Builder(context)
                                .setTitle(R.string.menu_duty)
                                .setMessage(CommonUtil.fromHtml(getString(R.string.text_duty)))
                                .setPositiveButton(R.string.text_confirm, null)
                                .show()
                    }))
            addView(createOverlayMenuItem(
                    iconLeft = getString(R.string.ic_code),
                    name = getString(R.string.menu_license),
                    iconRight = getString(R.string.ic_arrow_right),
                    value = "",
                    listener = {
                        AlertDialog.Builder(context)
                                .setTitle(R.string.menu_license)
                                .setMessage(CommonUtil.fromHtml(getString(R.string.text_license)))
                                .setPositiveButton(R.string.text_confirm, null)
                                .show()
                    }))
            addView(createOverlayMenuItem(
                    iconLeft = getString(R.string.ic_robot),
                    name = getString(R.string.menu_author),
                    iconRight = getString(R.string.ic_arrow_right),
                    value = "Boger Chan",
                    listener = {
                        CommonUtil.mailTo(context, "bogerchan@hotmail.com", String.format(
                                Locale.getDefault(),
                                getString(R.string.text_mail_subject),
                                getString(R.string.app_name),
                                BuildConfig.VERSION_NAME))
                    }))
        }
    }

    @SuppressLint("InflateParams")
    private fun createOverlayMenuItem(
            iconLeft: String,
            name: String,
            value: String,
            iconRight: String? = null,
            listener: (View) -> Unit,
            enabled: Boolean = true): View {

        return layoutInflater.inflate(R.layout.item_overlay_menu, null).apply {
            setOnClickListener(listener)
            findViewById<TextView>(R.id.gtv_item_overlay_menu_left).text = iconLeft
            findViewById<TextView>(R.id.gtv_item_overlay_menu_right).apply {
                if (TextUtils.isEmpty(iconRight)) {
                    visibility = View.INVISIBLE
                } else {
                    text = iconRight
                }
            }
            findViewById<TextView>(R.id.gtv_item_overlay_menu_name).text = name
            findViewById<TextView>(R.id.gtv_item_overlay_menu_value).text = value
            isEnabled = enabled
        }
    }


    private fun showReturn() {
        mPendingMenuAnimator?.cancel()
        mPendingMenuAnimator = AnimatorSet().apply {
            duration = 300
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    mPendingMenuAnimator = animation
                }

                override fun onAnimationEnd(animation: Animator?) {
                    svMenu.visibility = View.INVISIBLE
                    llContent.visibility = View.INVISIBLE
                    mPendingMenuAnimator = null
                }
            })
            play(ObjectAnimator.ofFloat(gtvBtnMenu, "rotation", gtvBtnMenu.rotation, 360F).apply {
                interpolator = LinearInterpolator()
            }).with(ObjectAnimator.ofFloat(gtvBtnMenu, "alpha", gtvBtnMenu.alpha, 0.3F, 1F).apply {
                interpolator = LinearInterpolator()
                addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                    override fun onAnimationUpdate(animation: ValueAnimator?) {
                        animation ?: return
                        if (animation.animatedFraction > 0.5F) {
                            gtvBtnMenu.text = mIconReturn
                            removeUpdateListener(this)
                        }
                    }

                })
            }).with(ObjectAnimator.ofFloat(llContent, "alpha", llContent.alpha, 0F).apply {
                interpolator = LinearInterpolator()
            }).with(ObjectAnimator.ofFloat(svMenu, "alpha", svMenu.alpha, 0F).apply {
                interpolator = LinearInterpolator()
            }).with(ObjectAnimator.ofFloat(svMenu, "rotationX", svMenu.rotationX, 10F).apply {
                interpolator = OvershootInterpolator()
            })
            start()
        }
    }

    private fun backFromReturn() {
        mPendingMenuAnimator?.cancel()
        mPendingMenuAnimator = AnimatorSet().apply {
            duration = 300
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    mPendingMenuAnimator = animation
                    llContent.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator?) {
                    svMenu.visibility = View.INVISIBLE
                    mPendingMenuAnimator = null
                }
            })
            play(ObjectAnimator.ofFloat(gtvBtnMenu, "rotation", gtvBtnMenu.rotation, 0F).apply {
                interpolator = LinearInterpolator()
            }).with(ObjectAnimator.ofFloat(gtvBtnMenu, "alpha", gtvBtnMenu.alpha, 0.3F, 1F).apply {
                interpolator = LinearInterpolator()
                addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                    override fun onAnimationUpdate(animation: ValueAnimator?) {
                        animation ?: return
                        if (animation.animatedFraction > 0.5F) {
                            gtvBtnMenu.text = mIconMenu
                            removeUpdateListener(this)
                        }
                    }

                })
            }).with(ObjectAnimator.ofFloat(llContent, "alpha", llContent.alpha, 1F).apply {
                interpolator = LinearInterpolator()
            }).with(ObjectAnimator.ofFloat(svMenu, "alpha", svMenu.alpha, 0F).apply {
                interpolator = LinearInterpolator()
            }).with(ObjectAnimator.ofFloat(svMenu, "rotationX", svMenu.rotationX, 10F).apply {
                interpolator = OvershootInterpolator()
            })
            start()
        }
        supportFragmentManager.popBackStackImmediate()
    }


    private fun openMenu() {
        mPendingMenuAnimator?.cancel()
        mPendingMenuAnimator = AnimatorSet().apply {
            duration = 300
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    mPendingMenuAnimator = animation
                    svMenu.visibility = View.VISIBLE
                    svMenu.pivotX = 0F
                    svMenu.pivotY = 0F
                }

                override fun onAnimationEnd(animation: Animator?) {
                    mPendingMenuAnimator = null
                }
            })
            play(ObjectAnimator.ofFloat(gtvBtnMenu, "rotation", gtvBtnMenu.rotation, 360F).apply {
                interpolator = LinearInterpolator()
            }).with(ObjectAnimator.ofFloat(gtvBtnMenu, "alpha", gtvBtnMenu.alpha, 0.3F, 1F).apply {
                interpolator = LinearInterpolator()
                addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                    override fun onAnimationUpdate(animation: ValueAnimator?) {
                        animation ?: return
                        if (animation.animatedFraction > 0.5F) {
                            gtvBtnMenu.text = mIconCross
                            removeUpdateListener(this)
                        }
                    }

                })
            }).with(ObjectAnimator.ofFloat(svMenu, "alpha", svMenu.alpha, 1F).apply {
                interpolator = LinearInterpolator()
            }).with(ObjectAnimator.ofFloat(svMenu, "rotationX", svMenu.rotationX, 0F).apply {
                interpolator = OvershootInterpolator()
            })
            start()
        }
    }

    private fun closeMenu() {
        mPendingMenuAnimator?.cancel()
        mPendingMenuAnimator = AnimatorSet().apply {
            duration = 300
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    mPendingMenuAnimator = animation
                    svMenu.pivotX = 0F
                    svMenu.pivotY = 0F
                }

                override fun onAnimationEnd(animation: Animator?) {
                    svMenu.visibility = View.INVISIBLE
                    mPendingMenuAnimator = null
                }
            })
            play(ObjectAnimator.ofFloat(gtvBtnMenu, "rotation", gtvBtnMenu.rotation, 0F).apply {
                interpolator = LinearInterpolator()
            }).with(ObjectAnimator.ofFloat(gtvBtnMenu, "alpha", gtvBtnMenu.alpha, 0.3F, 1F).apply {
                interpolator = LinearInterpolator()
                addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                    override fun onAnimationUpdate(animation: ValueAnimator?) {
                        animation ?: return
                        if (animation.animatedFraction > 0.5F) {
                            gtvBtnMenu.text = mIconMenu
                            removeUpdateListener(this)
                        }
                    }

                })
            }).with(ObjectAnimator.ofFloat(svMenu, "alpha", svMenu.alpha, 0F).apply {
                interpolator = LinearInterpolator()
            }).with(ObjectAnimator.ofFloat(svMenu, "rotationX", svMenu.rotationX, 10F).apply {
                interpolator = OvershootInterpolator()
            })
            start()
        }
    }
}