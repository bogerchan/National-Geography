package me.boger.geographic.core

import android.app.Application
import android.text.TextUtils
import com.facebook.drawee.backends.pipeline.Fresco
import com.tencent.bugly.Bugly
import com.tencent.bugly.beta.Beta
import com.tencent.bugly.crashreport.CrashReport
import me.boger.geographic.BuildConfig
import me.boger.geographic.R
import me.boger.geographic.biz.common.MainActivity
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
        initBugly()
    }

    private fun initBugly() {
        val appKeyBugly = getString(R.string.app_id_bugly)
        if (!TextUtils.isEmpty(appKeyBugly)) {
            Bugly.init(this, appKeyBugly, BuildConfig.DEBUG)
        }
        Beta.canShowUpgradeActs.add(MainActivity::class.java)
        Beta.autoDownloadOnWifi = true
        Beta.autoCheckUpgrade = true
        CrashReport.setIsDevelopmentDevice(this, BuildConfig.DEBUG)
    }

    private fun initLog() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}