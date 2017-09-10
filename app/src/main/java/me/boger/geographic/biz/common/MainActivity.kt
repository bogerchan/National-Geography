package me.boger.geographic.biz.common

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutCompat
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ScrollView
import android.widget.TextView
import com.tencent.bugly.beta.Beta
import me.boger.geographic.BuildConfig
import me.boger.geographic.R
import me.boger.geographic.biz.detailpage.DetailPageFragment
import me.boger.geographic.biz.selectpage.SelectPageAlbumData
import me.boger.geographic.biz.selectpage.SelectPageFragment
import me.boger.geographic.core.LocalizationWorker
import me.boger.geographic.core.NGActivity
import me.boger.geographic.core.NGRumtime
import me.boger.geographic.util.SdkSupporter
import java.util.*

class MainActivity : NGActivity() {

    private enum class MenuState {
        OPEN, CLOSE, BACK
    }

    private val mPresenter by lazy {
        MainActivityPresenter()
    }

    private val tvNGTitle by lazy {
        findViewById(R.id.tv_activity_main_ng_title) as TextView
    }

    private val tvNGMenu by lazy {
        findViewById(R.id.icon_activity_main_ng_menu) as TextView
    }

    private val ablTitle by lazy {
        findViewById(R.id.abl_activity_main_ng_title) as AppBarLayout
    }

    private val llcOverlayMenu by lazy {
        findViewById(R.id.llc_activity_main_overlay_menu) as LinearLayoutCompat
    }

    private val svOverlayMenu by lazy {
        findViewById(R.id.sv_activity_main_overlay_menu) as ScrollView
    }

    private var mPendingMenuAnimator: Animator? = null

    private var mMenuState = MenuState.CLOSE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        mPresenter.init(this)
    }

    fun showSelectDateContent(listener: (SelectPageAlbumData) -> Unit) {
        fragmentManager.beginTransaction()
                .add(R.id.cfl_activity_main_ng_content, SelectPageFragment(listener), SelectPageFragment.TAG)
                .commit()
    }

    fun showNGDetailContent(data: SelectPageAlbumData) {
        val offlineData =
                if (data.id == "unset") NGRumtime.favoriteNGDataSupplier.getNGDetailData() else null
        if (offlineData != null && offlineData.picture.isEmpty()) {
            Snackbar.make(
                    findViewById(R.id.cfl_activity_main_ng_content_full),
                    getString(R.string.tip_empty_favorite),
                    Snackbar.LENGTH_SHORT)
                    .show()
            return
        }
        val df = DetailPageFragment()
        df.initData(data.id, offlineData)
        fragmentManager.beginTransaction()
                .add(R.id.cfl_activity_main_ng_content_full, df, DetailPageFragment.TAG)
                .addToBackStack(null)
                .setCustomAnimations(R.anim.scale_in, R.anim.scale_out)
                .commit()
        ablTitle.setExpanded(true)
        setMenuState(MenuState.BACK)
    }

    override fun onBackPressed() {
        if (fragmentManager.popBackStackImmediate()
                || mMenuState == MenuState.OPEN) {
            setMenuState(MenuState.CLOSE)
        } else {
            super.onBackPressed()
        }
    }

    private fun initView() {
        ablTitle.postDelayed({
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }, 800)
        tvNGMenu.text = "\ue665"
        tvNGMenu.setOnClickListener(object : View.OnClickListener {

            override fun onClick(p0: View?) {
                if ("\ue6d8" == tvNGMenu.text) {
                    onBackPressed()
                } else if (mMenuState == MenuState.OPEN) {
                    setMenuState(MenuState.CLOSE)
                } else {
                    setMenuState(MenuState.OPEN)
                }
            }
        })
        llcOverlayMenu.addView(createOverlayMenuItem(
                iconLeft = "\ue600",
                name = getString(R.string.menu_language_settings),
                iconRight = "\ue615",
                value = getString(R.string.text_language),
                listener = {
                    AlertDialog.Builder(this)
                            .setTitle(R.string.text_language_setting)
                            .setSingleChoiceItems(
                                    R.array.language_selection,
                                    LocalizationWorker.curType.ordinal,
                                    { dialog, i ->
                                        dialog.dismiss()
                                        LocalizationWorker
                                                .startWork(this@MainActivity, LocalizationWorker.Type.values()[i])
                                    })
                            .setNegativeButton(R.string.text_cancel, null)
                            .show()
                }))
//        llcOverlayMenu.addView(createOverlayMenuItem(
//                iconLeft = "\ue6a2",
//                name = getString(R.string.menu_clean_cache),
//                iconRight = "\ue615",
//                value = "",
//                listener = {
//                    Snackbar.make(it, getString(R.string.tip_unsupport), Snackbar.LENGTH_SHORT).show()
//                }))
//        llcOverlayMenu.addView(createOverlayMenuItem(
//                iconLeft = "\ue609",
//                name = getString(R.string.menu_download_offline),
//                iconRight = "\ue615",
//                value = "",
//                listener = {
//                    Snackbar.make(it, getString(R.string.tip_unsupport), Snackbar.LENGTH_SHORT).show()
//                }))
//        llcOverlayMenu.addView(createOverlayMenuItem(
//                iconLeft = "\ue65b",
//                name = getString(R.string.menu_live_paper),
//                iconRight = "\ue615",
//                value = "",
//                listener = {
//                    Snackbar.make(it, getString(R.string.tip_unsupport), Snackbar.LENGTH_SHORT).show()
//                }, enabled = false))
        llcOverlayMenu.addView(createOverlayMenuItem(
                iconLeft = "\ue60a",
                name = getString(R.string.menu_update_app),
                iconRight = "\ue615",
                value = "v${BuildConfig.VERSION_NAME}",
                listener = {
                    checkUpdate()
                }))
        llcOverlayMenu.addView(createOverlayMenuItem(
                iconLeft = "\ue62a",
                name = getString(R.string.menu_duty),
                iconRight = "\ue615",
                value = "",
                listener = {
                    AlertDialog.Builder(this)
                            .setTitle(R.string.menu_duty)
                            .setMessage(SdkSupporter.fromHtml(getString(R.string.text_duty)))
                            .setPositiveButton(R.string.text_confirm, null)
                            .show()
                }))
        llcOverlayMenu.addView(createOverlayMenuItem(
                iconLeft = "\ue637",
                name = getString(R.string.menu_license),
                iconRight = "\ue615",
                value = "",
                listener = {
                    AlertDialog.Builder(this)
                            .setTitle(R.string.menu_license)
                            .setMessage(SdkSupporter.fromHtml(getString(R.string.text_license)))
                            .setPositiveButton(R.string.text_confirm, null)
                            .show()
                }))
        llcOverlayMenu.addView(createOverlayMenuItem(
                iconLeft = "\ued05",
                name = getString(R.string.menu_author),
                iconRight = "\ue615",
                value = "BogerChan",
                listener = {
                    mailToAuthor()
                }))
    }

    private fun createOverlayMenuItem(
            iconLeft: String,
            name: String,
            value: String,
            iconRight: String? = null,
            listener: (View) -> Unit,
            enabled: Boolean = true): View {

        val v = layoutInflater.inflate(R.layout.item_overlay_menu, null)
        v.setOnClickListener(listener)
        val tvIconLeft = v.findViewById(R.id.icon_item_overlay_menu_left) as TextView
        tvIconLeft.text = iconLeft
        val tvIconRight = v.findViewById(R.id.icon_item_overlay_menu_right) as TextView
        if (TextUtils.isEmpty(iconRight)) {
            tvIconRight.visibility = View.INVISIBLE
        } else {
            tvIconRight.text = iconRight
        }
        val tvName = v.findViewById(R.id.tv_item_overlay_menu_name) as TextView
        tvName.text = name
        val tvValue = v.findViewById(R.id.tv_item_overlay_menu_value) as TextView
        tvValue.text = value
        v.isEnabled = enabled
        return v
    }

    private fun setMenuState(state: MenuState) {
        mPendingMenuAnimator?.cancel()
        val range = when (state) {
            MenuState.CLOSE -> arrayOf(tvNGMenu.rotation, 0f)
            else -> arrayOf(tvNGMenu.rotation, 360f)
        }
        val menuIconText = when (state) {
            MenuState.OPEN -> "\ue736"
            MenuState.CLOSE -> "\ue665"
            MenuState.BACK -> "\ue6d8"
        }
        val animSet = AnimatorSet()
        animSet.duration = 300
        animSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                mPendingMenuAnimator = animation
                if (MenuState.OPEN == state) {
                    svOverlayMenu.visibility = View.VISIBLE
                }
                svOverlayMenu.pivotX = 0f
                svOverlayMenu.pivotY = 0f
            }

            override fun onAnimationEnd(animation: Animator?) {
                mPendingMenuAnimator = null
                if (state != MenuState.OPEN) {
                    svOverlayMenu.visibility = View.INVISIBLE
                }
                svOverlayMenu.pivotX = svOverlayMenu.measuredWidth / 2f
                svOverlayMenu.pivotY = svOverlayMenu.measuredHeight / 2f
                mMenuState = state
            }
        })
        val aniMenuButton = ValueAnimator.ofFloat(*range.toFloatArray())
        aniMenuButton.interpolator = LinearInterpolator()
        aniMenuButton.addUpdateListener {
            val fraction = it.animatedFraction
            if (fraction > .5f) {
                tvNGMenu.alpha = (fraction - 0.5f) * 2
                if (menuIconText != tvNGMenu.text) {
                    tvNGMenu.text = menuIconText
                }
            } else {
                tvNGMenu.alpha = 1 - fraction * 2
            }
            tvNGMenu.rotation = it.animatedValue as Float
        }
        val aniBuilder = animSet.play(aniMenuButton)
        if (state != MenuState.BACK) {
            val aniMenuContent = ValueAnimator.ofFloat(*range.toFloatArray())
            aniMenuContent.interpolator = LinearInterpolator()
            aniMenuContent.addUpdateListener {
                svOverlayMenu.alpha = it.animatedValue as Float / 360
            }
            val aniRotation = ValueAnimator.ofFloat(*range.toFloatArray())
            aniRotation.interpolator = OvershootInterpolator()
            aniRotation.addUpdateListener {
                svOverlayMenu.rotationX = (1 - it.animatedValue as Float / 360) * 10
            }
            aniBuilder.with(aniMenuContent).with(aniRotation)
        }

        animSet.start()
    }

    private fun checkUpdate() {
//        val intent = Intent(Intent.ACTION_VIEW)
//        intent.data = Uri.parse("market://details?id=geographic.boger.me.nationalgeographic")
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        startActivity(Intent.createChooser(intent, getString(R.string.menu_update_app)))
        Beta.checkUpgrade()
    }

    private fun mailToAuthor() {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:bogerchan@hotmail.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT,
                String.format(
                        Locale.getDefault(),
                        getString(R.string.text_mail_subject),
                        getString(R.string.app_name),
                        BuildConfig.VERSION_NAME))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}
