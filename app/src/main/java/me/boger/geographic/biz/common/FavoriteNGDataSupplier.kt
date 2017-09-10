package me.boger.geographic.biz.common

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.google.gson.reflect.TypeToken
import me.boger.geographic.R
import me.boger.geographic.core.NGRumtime
import me.boger.geographic.biz.detailpage.DetailPageData
import me.boger.geographic.biz.detailpage.DetailPagePictureData
import me.boger.geographic.biz.selectpage.SelectPageAlbumData
import me.boger.geographic.core.NGBroadcastManager
import me.boger.geographic.core.NGConstants
import me.boger.geographic.util.Timber
import java.util.*

/**
 * Created by BogerChan on 2017/7/10.
 */
class FavoriteNGDataSupplier(ctx: Context) {

    companion object {
        val KEY_FAVORITE_NG_DETAIL_DATA = "fav_ng_detail_data"
    }

    private var mDetailPageData: DetailPageData = DetailPageData("0", ArrayList<DetailPagePictureData>(0))

    private var mSP = ctx.getSharedPreferences(ctx.packageName, Context.MODE_PRIVATE)

    init {
        val jsonDetailPageData = mSP.getString(KEY_FAVORITE_NG_DETAIL_DATA, null)
        if (!TextUtils.isEmpty(jsonDetailPageData)) {
            val list = NGRumtime.gson.fromJson<MutableList<DetailPagePictureData>>(
                    jsonDetailPageData,
                    object : TypeToken<MutableList<DetailPagePictureData>>() {}.type)
            mDetailPageData.counttotal = list.size.toString()
            mDetailPageData.picture = list
        }
    }

    fun addDetailPageDataToFavorite(data: DetailPagePictureData): Boolean {
        try {
            if (mDetailPageData.picture.contains(data)) {
                return true
            }
            mDetailPageData.picture.add(data)
            mSP.edit()
                    .putString(KEY_FAVORITE_NG_DETAIL_DATA, NGRumtime.gson.toJson(mDetailPageData.picture))
                    .apply()
        } catch (e: Exception) {
            Timber.e(e)
            mDetailPageData.picture.remove(data)
            return false
        }
        NGBroadcastManager.sendLocalBroadcast(Intent(NGConstants.ACTION_FAVORITE_DATA_CHANGED))
        return true
    }

    fun removeDetailPageDataToFavorite(data: DetailPagePictureData): Boolean {
        try {
            if (!mDetailPageData.picture.contains(data)) {
                return true
            }
            mDetailPageData.picture.remove(data)
            mSP.edit()
                    .putString(KEY_FAVORITE_NG_DETAIL_DATA, NGRumtime.gson.toJson(mDetailPageData.picture))
                    .apply()
        } catch (e: Exception) {
            Timber.e(e)
            mDetailPageData.picture.add(data)
            return false
        }
        NGBroadcastManager.sendLocalBroadcast(Intent(NGConstants.ACTION_FAVORITE_DATA_CHANGED))
        return true
    }

    fun getDetailPageData(): DetailPageData {
        mDetailPageData.picture.forEach {
            it.clearLocale()
        }
        return mDetailPageData.copy(picture = mDetailPageData.picture.toMutableList())
    }

    private fun getLastCoverUrl(): String {
        return if (mDetailPageData.picture.size > 0)
            mDetailPageData.picture.last().url else ""
    }

    private fun getImageCount() = mDetailPageData.picture.size

    fun syncFavoriteState(data: DetailPageData) {
        val favoriteIdSet = mutableSetOf<String>()
        mDetailPageData.picture.forEach {
            favoriteIdSet.add(it.id)
        }
        data.picture.forEach {
            it.favorite = favoriteIdSet.contains(it.id)
        }
    }

    fun getFavoriteAlbumData(): SelectPageAlbumData {
        return SelectPageAlbumData(
                "unset",
                String.format(Locale.getDefault(),
                        NGRumtime.application.getString(R.string.text_favorite_item_title),
                        getImageCount()),
                getLastCoverUrl(), "unset", "unset", "unset", "unset", "unset", "unset",
                "unset", "unset", "unset")
    }
}