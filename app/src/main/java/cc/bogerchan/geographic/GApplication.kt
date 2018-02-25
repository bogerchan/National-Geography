package cc.bogerchan.geographic

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Environment
import android.text.TextUtils
import cc.bogerchan.geographic.util.Timber
import com.facebook.drawee.backends.pipeline.Fresco
import com.flurry.android.FlurryAgent
import java.io.File

/**
 * Created by Boger Chan on 2018/1/28.
 */
class GApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var app: Context

        val imageDir by lazy {

            return@lazy File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "GeoApp").apply {
                if (!exists()) {
                    mkdirs()
                }
            }
        }

    }

    override fun onCreate() {
        super.onCreate()
        app = applicationContext
        initLog()
        Fresco.initialize(this)
        initFlurry()
    }

    private fun initFlurry() {
        val apiKeyForFlurry = getString(R.string.api_key_for_flurry)
        if (!TextUtils.isEmpty(apiKeyForFlurry)) {
            FlurryAgent.Builder()
                    .withLogEnabled(true)
                    .build(this, apiKeyForFlurry)
        }
    }

    private fun initLog() {
        if (BuildConfig.LOGGABLE) {
            Timber.plant(Timber.DebugTree())
        }
    }
}