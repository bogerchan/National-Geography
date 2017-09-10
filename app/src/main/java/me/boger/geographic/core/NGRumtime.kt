package me.boger.geographic.core

import android.app.Application
import android.os.Environment
import com.google.gson.GsonBuilder
import me.boger.geographic.biz.common.FavoriteNGDataSupplier
import me.boger.geographic.util.TransientExclusionStrategy
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Created by BogerChan on 2017/6/30.
 */
object NGRumtime {
    val HOST = "http://dili.bdatu.com/"

    val retrofit by lazy {
        val client = OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.MINUTES)
                .connectTimeout(60, TimeUnit.MINUTES)
                .build()
        Retrofit.Builder()
                .client(client)
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .build()
    }

    lateinit var cacheImageDir: File

    lateinit var externalDataDir: File

    lateinit var externalAlbumDir: File

    lateinit var favoriteNGDataSupplier: FavoriteNGDataSupplier

    lateinit var application: Application

    val gson by lazy {
        GsonBuilder()
                .addSerializationExclusionStrategy(TransientExclusionStrategy)
                .create()
    }

    fun init(app: Application) {
        cacheImageDir = File(app.externalCacheDir, "img")
        if (!cacheImageDir.exists()) {
            cacheImageDir.mkdir()
        }
        externalDataDir = File(Environment.getExternalStorageDirectory(), "NationalGeography")
        if (!externalDataDir.exists()) {
            externalDataDir.mkdir()
        }
        externalAlbumDir = File(externalDataDir, "NGAlbum")
        if (!externalAlbumDir.exists()) {
            externalAlbumDir.mkdir()
        }
        favoriteNGDataSupplier = FavoriteNGDataSupplier(app)
        application = app
    }

    fun locale(text: String): String =
            LocalizationWorker.translate(
                    LocalizationWorker.Type.SIMPLIFIED_CHINESE,
                    LocalizationWorker.curType,
                    text)

}