package me.boger.geographic.core

import android.app.Application
import android.text.TextUtils
import com.facebook.drawee.backends.pipeline.Fresco
import com.flurry.android.FlurryAgent
import me.boger.geographic.BuildConfig
import me.boger.geographic.R
import me.boger.geographic.util.Timber


/**
 * Created by BogerChan on 2017/6/25.
 */
class NGApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initLog()
        Fresco.initialize(this)
        AppConfiguration.init(this)
        NGRumtime.init(this)
        LocalizationWorker.prepare(this)
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