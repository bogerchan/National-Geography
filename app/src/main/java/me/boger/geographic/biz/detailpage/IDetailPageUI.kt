package me.boger.geographic.biz.detailpage

import android.content.ContentResolver
import android.content.Intent
import me.boger.geographic.biz.common.ContentType

/**
 * Created by BogerChan on 2017/6/30.
 */
interface IDetailPageUI {
    var contentType: ContentType

    fun refreshData(data: List<DetailPagePictureData>)

    fun showTipMessage(msg: String)

    fun showTipMessage(msgId: Int)

    fun startActivity(intent: Intent)

    fun getResourceString(id: Int): String

    fun getContentResolver(): ContentResolver

    fun sendBroadcast(intent: Intent)

    fun setFavoriteButtonState(favorite: Boolean)

    fun hasOfflineData(): Boolean

    fun getOfflineData(): DetailPageData

    fun getNGDetailDataId(): String
}