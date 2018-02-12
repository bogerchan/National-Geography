package cc.bogerchan.geographic.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import cc.bogerchan.geographic.R
import com.facebook.drawee.view.SimpleDraweeView
import util.bindView

/**
 * Created by Boger Chan on 2018/1/28.
 */
class SplashActivity : AppCompatActivity() {

    private val cflLogo by bindView<FrameLayout>(R.id.cfl_splash_logo)
    private val sdvSplash by bindView<SimpleDraweeView>(R.id.sdv_splash)

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
