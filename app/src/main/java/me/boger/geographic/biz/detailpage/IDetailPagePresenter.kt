package me.boger.geographic.biz.detailpage

import android.os.Bundle

/**
 * Created by BogerChan on 2017/6/30.
 */
interface IDetailPagePresenter {
    fun init(ui: IDetailPageUI)

    fun shareDetailPageImage(url: String)

    fun saveDetailPageImage(url: String)

    fun setDetailPageItemFavoriteState(data: DetailPagePictureData)

    fun destroy()

    fun onSaveInstanceState(outState: Bundle?)

    fun restoreDataIfNeed(savedInstanceState: Bundle?)
}