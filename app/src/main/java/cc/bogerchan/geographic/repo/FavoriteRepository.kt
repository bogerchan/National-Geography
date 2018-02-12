package cc.bogerchan.geographic.repo

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import cc.bogerchan.geographic.GApplication
import cc.bogerchan.geographic.R
import cc.bogerchan.geographic.dao.NGCardData
import cc.bogerchan.geographic.dao.NGCardElementData
import cc.bogerchan.geographic.util.FetchStatus
import cc.bogerchan.geographic.util.removeIfCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Created by Boger Chan on 2018/1/31.
 */
class FavoriteRepository {

    private var mBufferDataList: MutableList<NGCardData> = arrayListOf()
    private val mGson by lazy { Gson() }
    private val mSp by lazy { GApplication.app.getSharedPreferences("g_favorite", Context.MODE_PRIVATE) }

    private val mSuccessData by lazy { Pair(FetchStatus.SUCCESS, mBufferDataList) }

    fun fetchCardDataList(): MutableLiveData<Pair<FetchStatus, List<NGCardData>>> {
        readFromDisk()
        return MutableLiveData<Pair<FetchStatus, List<NGCardData>>>().apply {
            value = mSuccessData
        }
    }

    fun addNGCardElementData(cardElementData: NGCardElementData): MutableLiveData<Pair<FetchStatus, List<NGCardData>>> {
        if (mBufferDataList.isEmpty()) {
            mBufferDataList.add(NGCardData(0, GApplication.app.getString(R.string.text_ng_favorite_set), cardElementData.imgUrl, 0, arrayListOf(cardElementData)))
        } else {
            mBufferDataList[0].apply {
                imgUrl = cardElementData.imgUrl
                cardElements?.add(0, cardElementData)
            }
        }
        flushToDisk()
        return MutableLiveData<Pair<FetchStatus, List<NGCardData>>>().apply {
            value = mSuccessData
        }
    }

    fun removeNGCardElementData(cardElementData: NGCardElementData): MutableLiveData<Pair<FetchStatus, List<NGCardData>>> {
        val result = MutableLiveData<Pair<FetchStatus, List<NGCardData>>>()
        if (mBufferDataList.isEmpty()) {
            return result
        } else {
            mBufferDataList[0].cardElements?.apply {
                removeIfCompat { it.id == cardElementData.id }
                if (isEmpty()) {
                    mBufferDataList.clear()
                } else {
                    mBufferDataList[0].imgUrl = get(0).imgUrl
                }
            }
        }
        flushToDisk()
        return MutableLiveData<Pair<FetchStatus, List<NGCardData>>>().apply {
            value = mSuccessData
        }
    }

    private fun readFromDisk() {
        mBufferDataList.clear()
        mSp.getString("_favorite_json", null)?.apply {
            mGson.fromJson<MutableList<NGCardData>>(this, object : TypeToken<MutableList<NGCardData>>() {}.type)?.forEach {
                mBufferDataList.add(it)
            }
        }
    }

    private fun flushToDisk() {
        mSp.edit().putString("_favorite_json", mGson.toJson(mBufferDataList)).apply()
    }

    fun clear() {
    }
}