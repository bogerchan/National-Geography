package me.boger.geographic.core

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.text.TextUtils
import com.zqc.opencc.android.lib.ChineseConverter
import com.zqc.opencc.android.lib.ConversionType
import me.boger.geographic.util.SdkSupporter
import java.util.*

/**
 * Created by BogerChan on 2017/7/11.
 */
object LocalizationWorker {
    enum class Type {
        SIMPLIFIED_CHINESE,
        TRADITIONAL_CHINESE_HK,
        TRADITIONAL_CHINESE_TW,
    }

    private val KEY_SP_APP_SETTINGS_LANGUAGE = "sp_app_settings_language"

    var curType: Type = Type.SIMPLIFIED_CHINESE
        private set

    private val mTypefaceMap = mapOf(
            Pair(Type.SIMPLIFIED_CHINESE, AppConfiguration.primaryTypeface),
            Pair(Type.TRADITIONAL_CHINESE_HK, AppConfiguration.primaryTypeface),
            Pair(Type.TRADITIONAL_CHINESE_TW, AppConfiguration.primaryTypeface))

    var curTypeface = mTypefaceMap[curType]
        private set
        get() = mTypefaceMap[curType]

    private lateinit var mContext: Context

    private val mLanguageStateSwitchMapper = mapOf(
            Pair(Type.SIMPLIFIED_CHINESE, mapOf(
                    Pair(Type.TRADITIONAL_CHINESE_HK, ConversionType.S2HK),
                    Pair(Type.TRADITIONAL_CHINESE_TW, ConversionType.S2TWP))),
            Pair(Type.TRADITIONAL_CHINESE_TW, mapOf(
                    Pair(Type.SIMPLIFIED_CHINESE, ConversionType.TW2SP),
                    Pair(Type.TRADITIONAL_CHINESE_HK, ConversionType.T2HK))),
            Pair(Type.TRADITIONAL_CHINESE_HK, mapOf(
                    Pair(Type.SIMPLIFIED_CHINESE, ConversionType.HK2S),
                    Pair(Type.TRADITIONAL_CHINESE_TW, ConversionType.T2TW))))

    private lateinit var mSp: SharedPreferences

    fun prepare(ctx: Context) {
        mSp = ctx.getSharedPreferences(ctx.packageName, Context.MODE_PRIVATE)
        val type = mSp.getString(KEY_SP_APP_SETTINGS_LANGUAGE, null)
        if (TextUtils.isEmpty(type)) {
            saveAppLanguageSettings(curType)
        } else {
            curType = Type.valueOf(type)
        }
        mContext = ctx.applicationContext
    }

    fun startWork(act: Activity, type: Type) {
        val locale = when (type) {
            Type.SIMPLIFIED_CHINESE -> Locale.SIMPLIFIED_CHINESE
            Type.TRADITIONAL_CHINESE_TW -> Locale("zh", "TW")
            Type.TRADITIONAL_CHINESE_HK -> Locale("zh", "HK")
        }
        Locale.setDefault(locale)
        SdkSupporter.updateConfiguration(act.applicationContext.resources, locale)
        curType = type
        act.recreate()
        saveAppLanguageSettings(type)
    }

    private fun saveAppLanguageSettings(type: Type) {
        mSp.edit().putString(KEY_SP_APP_SETTINGS_LANGUAGE, type.toString()).apply()
    }

    fun createLocalChangeSupportContext(ctx: Context)
            = ContextWrapper(
            SdkSupporter.createConfigurationContext(
                    ctx,
                    ctx.applicationContext.resources.configuration))

    fun translate(from: Type, to: Type, text: String): String {
        if (from == to) {
            return text
        }
        return ChineseConverter.convert(text, mLanguageStateSwitchMapper[from]!![to], mContext)
    }

}