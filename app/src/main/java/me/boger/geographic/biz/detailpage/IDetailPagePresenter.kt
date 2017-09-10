package me.boger.geographic.biz.detailpage

import android.os.Bundle

/**
 * Created by BogerChan on 2017/6/30.
 */
interface IDetailPagePresenter {
    fun init(ui: IDetailPageUI)

    fun shareNGDetailImage(url: String)

    fun saveNGDetailImage(url: String)

    fun setNGDetailItemFavoriteState(data: DetailPagePictureData)

    fun destroy()

    fun onSaveInstanceState(outState: Bundle?)

    fun restoreDataIfNeed(savedInstanceState: Bundle?)
}