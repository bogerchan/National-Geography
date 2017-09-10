package me.boger.geographic.biz.common

/**
 * Created by BogerChan on 2017/6/28.
 */
class MainActivityPresenter {

    private var mView : MainActivity? = null

    fun init(act: MainActivity) {
        act.showSelectDateContent({
            act.showNGDetailContent(it)
        })
        mView = act
    }
}