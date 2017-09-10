package me.boger.geographic.biz.splash

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.ContentFrameLayout
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import com.facebook.drawee.view.SimpleDraweeView
import me.boger.geographic.R
import me.boger.geographic.biz.common.MainActivity
import me.boger.geographic.core.NGActivity

class SplashActivity : NGActivity() {

    private val cflLogo by lazy { findViewById(R.id.cfl_splash_logo) as ContentFrameLayout }
    private val sdvSplash by lazy { findViewById(R.id.sdv_splash) as SimpleDraweeView }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        init()
    }

    private fun init() {
        val aniScale = ScaleAnimation(1f, 1.1f, 1f, 1.1f, ScaleAnimation.RELATIVE_TO_PARENT, 0.5f, ScaleAnimation.RELATIVE_TO_PARENT, 0.5f)
        aniScale.interpolator = LinearInterpolator()
        aniScale.duration = 3000
        aniScale.fillAfter = true
        aniScale.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                handleJump()
            }

            override fun onAnimationStart(animation: Animation?) {
            }

        })
        val aniAlpha = AlphaAnimation(0f, 1f)
        aniAlpha.interpolator = LinearInterpolator()
        aniAlpha.duration = 500
        aniAlpha.fillAfter = true
        aniAlpha.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
            }

            override fun onAnimationStart(p0: Animation?) {
                cflLogo.visibility = View.VISIBLE
            }

        })
        cflLogo.startAnimation(aniAlpha)
        sdvSplash.startAnimation(aniScale)
    }

    private fun handleJump() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        overridePendingTransition(R.anim.scale_in, R.anim.scale_out)
    }
}
