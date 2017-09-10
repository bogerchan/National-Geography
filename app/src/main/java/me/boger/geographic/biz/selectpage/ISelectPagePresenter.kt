package me.boger.geographic.biz.selectpage

import android.os.Bundle

/**
 * Created by BogerChan on 2017/6/27.
 */
interface ISelectPagePresenter {
    fun init(ui: ISelectPageUI)

    fun notifyFavoriteNGDetailDataChanged()

    fun destroy()

    fun onSaveInstanceState(outState: Bundle?)

    fun restoreDataIfNeed(savedInstanceState: Bundle?)

}