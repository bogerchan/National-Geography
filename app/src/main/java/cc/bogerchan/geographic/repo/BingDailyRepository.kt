package cc.bogerchan.geographic.repo

import cc.bogerchan.geographic.util.CommonUtil
import cc.bogerchan.geographic.util.Timber
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET

/**
 * Created by hb.chen on 2018/3/10.
 */
class BingDailyRepository {

    companion object {
        val HOST = "https://www.bing.com/"
    }

    interface BingDailyService {

        @GET("cnhp/life")
        fun requestDailyHtmlContent(): Call<String>
    }

    private val mService by lazy { CommonUtil.obtainRetrofit(HOST, ScalarsConverterFactory.create()).create(BingDailyService::class.java) }

    fun requestDailyHtmlContent(callback: (Boolean, String?) -> Unit) {
        mService.requestDailyHtmlContent().apply {
            enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>?, t: Throwable?) {
                    Timber.e(t)
                    callback(false, null)
                }

                override fun onResponse(call: Call<String>?, response: Response<String>?) {
                    if (response == null) {
                        callback(false, null)
                    } else {
                        callback(true, response.body())
                    }
                }

            })
        }
    }
}