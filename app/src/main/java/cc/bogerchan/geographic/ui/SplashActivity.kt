package cc.bogerchan.geographic.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.Toast
import cc.bogerchan.geographic.R
import cc.bogerchan.geographic.util.CommonUtil
import com.facebook.drawee.view.SimpleDraweeView
import util.bindView

/**
 * Created by Boger Chan on 2018/1/28.
 */
class SplashActivity : AppCompatActivity() {

    companion object {
        private val REQUEST_CODE_FILE_PERMISSIONS = 1
    }

    private val flLogo by bindView<FrameLayout>(R.id.fl_splash_logo)
    private val sdvSplash by bindView<SimpleDraweeView>(R.id.sdv_splash)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        init()
    }

    private fun init() {
        val aniScale = ScaleAnimation(1f, 1.1f, 1f, 1.1f, ScaleAnimation.RELATIVE_TO_PARENT, 0.5f, ScaleAnimation.RELATIVE_TO_PARENT, 0.5f)
        aniScale.interpolator = LinearInterpolator()
        aniScale.duration = 2000
        aniScale.fillAfter = true
        aniScale.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                checkPermissions()
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
                flLogo.visibility = View.VISIBLE
            }

        })
        flLogo.startAnimation(aniAlpha)
        sdvSplash.startAnimation(aniScale)
    }

    private fun checkPermissions() {
        if (!CommonUtil.hasPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) ||
                !CommonUtil.hasPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder(this)
                        .setTitle(R.string.title_file_permission_dialog)
                        .setMessage(R.string.content_file_permission_dialog)
                        .setPositiveButton(R.string.text_file_permission_dialog_positive_button, { _, _ ->
                            requestFilePermissions()
                        }).setNegativeButton(R.string.text_file_permission_dialog_negative_button, { _, _ ->
                    handleJump()
                }).show()
            } else {
                requestFilePermissions()
            }
        } else {
            handleJump()
        }
    }

    private fun requestFilePermissions() {
        ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_FILE_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_FILE_PERMISSIONS -> {
                if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                    Toast.makeText(this, R.string.tip_request_file_permissions_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
        handleJump()
    }

    private fun handleJump() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        overridePendingTransition(R.anim.scale_in, R.anim.scale_out)
    }
}
