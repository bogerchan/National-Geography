package cc.bogerchan.geographic.repo

import android.arch.lifecycle.MutableLiveData
import cc.bogerchan.geographic.dao.NGCardData
import cc.bogerchan.geographic.dao.NGCardElementData
import cc.bogerchan.geographic.util.CommonUtil
import cc.bogerchan.geographic.util.FetchStatus
import cc.bogerchan.geographic.util.Timber
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by Boger Chan on 2018/1/31.
 */
class NGDailyPostRepository {

    companion object {
        val HOST = "http://dili.bdatu.com/"
    }

    data class NGDailyPostData1(var total: String, var album: List<NGDailyPostData2>)
    data class NGDailyPostData2(var id: Int, var title: String, var url: String)
    data class NGDailyPostData3(var picture: List<NGDailyPostData4>)
    data class NGDailyPostData4(var id: Int, var title: String, var content: String, var url: String, var author: String)

    interface NGDailyPostService {
        @GET("jiekou/mains/p{page}.html")
        fun requestNGDailyCardDataList(@Path("page") page: Int): Call<NGDailyPostData1>

        @GET("jiekou/albums/a{id}.html")
        fun requestNGDailyCardDataElements(@Path("id") id: Int): Call<NGDailyPostData3>
    }

    private val mNGDailyPostService by lazy { CommonUtil.obtainRetrofit(HOST).create(NGDailyPostService::class.java) }
    private var mPageIdx = 1
    private var mBufferDataList: MutableList<NGCardData> = arrayListOf()
    private val mPendingCallList = arrayListOf<Call<*>>()
    private var mTotalItems = 0

    fun fetchCardDataListByPageFromFirst(): MutableLiveData<Pair<FetchStatus, List<NGCardData>>> {
        mPageIdx = 1
        mBufferDataList.clear()
        return fetchCardDataListForNext()
    }

    fun fetchCardDataListForNext(): MutableLiveData<Pair<FetchStatus, List<NGCardData>>> {
        val liveData = MutableLiveData<Pair<FetchStatus, List<NGCardData>>>()
        if (mTotalItems > 0 && mBufferDataList.size == mTotalItems) {
            liveData.value = Pair<FetchStatus, List<NGCardData>>(FetchStatus.REACHED, mBufferDataList)
            return liveData
        }
        mNGDailyPostService.requestNGDailyCardDataList(mPageIdx).apply {
            enqueue(object : Callback<NGDailyPostData1> {
                override fun onResponse(call: Call<NGDailyPostData1>?, response: Response<NGDailyPostData1>?) {
                    mPendingCallList.remove(this@apply)
                    if (response == null || !response.isSuccessful) {
                        Timber.e("fetch url(${call?.request()?.url()?.toString()}) error, code: ${response?.code()}!")
                        liveData.value = Pair<FetchStatus, List<NGCardData>>(FetchStatus.ERROR, mBufferDataList)
                        return
                    }
                    val data = response.body()
                    if (data == null || !transformCardDataList(data)) {
                        Timber.e("fetch url(${call?.request()?.url()?.toString()}) error, data is empty or transform error!")
                        liveData.value = Pair<FetchStatus, List<NGCardData>>(FetchStatus.ERROR, mBufferDataList)
                        return
                    }
                    mPageIdx++
                    liveData.value = Pair<FetchStatus, List<NGCardData>>(FetchStatus.SUCCESS, mBufferDataList)
                }

                override fun onFailure(call: Call<NGDailyPostData1>?, t: Throwable?) {
                    mPendingCallList.remove(this@apply)
                    Timber.e(t, "fetch url(${call?.request()?.url()?.toString()}) error!")
                    liveData.value = Pair<FetchStatus, List<NGCardData>>(FetchStatus.ERROR, mBufferDataList)
                }

            }).let { mPendingCallList.add(this) }
        }
        return liveData
    }

    fun fetchCardDataElements(cardData: NGCardData): MutableLiveData<Pair<FetchStatus, NGCardData>> {
        val cardDataWithElements = MutableLiveData<Pair<FetchStatus, NGCardData>>()
        if (cardData.cardElements != null && cardData.cardElements!!.isNotEmpty()) {
            cardDataWithElements.value = Pair(FetchStatus.SUCCESS, cardData)
            return cardDataWithElements
        }
        mNGDailyPostService.requestNGDailyCardDataElements(cardData.id).apply {
            enqueue(object : Callback<NGDailyPostData3> {
                override fun onResponse(call: Call<NGDailyPostData3>?, response: Response<NGDailyPostData3>?) {
                    mPendingCallList.remove(this@apply)
                    if (response == null || !response.isSuccessful) {
                        Timber.e("fetch url(${call?.request()?.url()?.toString()}) error, code: ${response?.code()}!")
                        cardDataWithElements.value = Pair(FetchStatus.ERROR, cardData)
                        return
                    }
                    val data = response.body()
                    if (data == null) {
                        Timber.e("fetch url(${call?.request()?.url()?.toString()}) error, data is empty!")
                        cardDataWithElements.value = Pair(FetchStatus.ERROR, cardData)
                        return
                    }
                    cardData.cardElements = ArrayList(data.picture.size)
                    data.picture.forEach {
                        cardData.cardElements?.add(NGCardElementData(it.id, it.title, it.content, it.author, it.url, 0))
                    }
                    cardDataWithElements.value = Pair(FetchStatus.SUCCESS, cardData)
                }

                override fun onFailure(call: Call<NGDailyPostData3>?, t: Throwable?) {
                    mPendingCallList.remove(this@apply)
                    Timber.e(t, "fetch url(${call?.request()?.url()?.toString()}) error!")
                    cardDataWithElements.value = Pair(FetchStatus.ERROR, cardData)
                }

            }).let { mPendingCallList.add(this) }
        }
        return cardDataWithElements
    }

    private fun transformCardDataList(raw: NGDailyPostData1): Boolean {
        return try {
            mTotalItems = raw.total.toInt()
            raw.album.forEach {
                mBufferDataList.add(NGCardData(it.id, it.title, it.url, 0, null))
            }
            true
        } catch (tr: Exception) {
            Timber.e(tr)
            false
        }
    }

    fun clear() {
        mPendingCallList.forEach { it.cancel() }
        mPendingCallList.clear()
    }
}