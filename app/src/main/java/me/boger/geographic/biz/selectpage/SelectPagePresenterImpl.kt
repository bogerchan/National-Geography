package me.boger.geographic.biz.selectpage

import android.os.Bundle
import android.support.design.widget.Snackbar
import me.boger.geographic.R
import me.boger.geographic.core.NGRumtime
import me.boger.geographic.biz.common.ContentType
import me.boger.geographic.util.Timber

/**
 * Created by BogerChan on 2017/6/27.
 */
class SelectPagePresenterImpl : ISelectPagePresenter {
    private var mUI: ISelectPageUI? = null

    private val mModel: ISelectPageModel by lazy {
        SelectPageModelImpl()
    }

    private var isDestroyed = false

    override fun init(ui: ISelectPageUI) {
        ui.setOnRetryClickListener({
            Timber.d("Click")
            firstLoadNGData()
        })
        ui.setOnRefreshListener(
                onRefresh = { v ->
                    if (isDestroyed) {
                        return@setOnRefreshListener
                    }
                    mModel.clearCache()
                    mModel.requestNGDateData(1,
                            onStart = {},
                            onNext = {
                                v.refreshCardData(it.album)
                            },
                            onError = {
                                Timber.e(it)
                                v.finishRefreshing()
                                Snackbar.make(v.getContentView(), R.string.tip_load_error, Snackbar.LENGTH_SHORT).show()
                            },
                            onComplete = {
                                v.finishRefreshing()
                                v.setEnableLoadMore(mModel.hasNextPage())
                            })
                },
                onLoadMore = { v ->
                    if (isDestroyed || !mModel.hasNextPage()) {
                        return@setOnRefreshListener
                    }
                    mModel.requestNGDateData(mModel.currentPage + 1,
                            onStart = {},
                            onNext = {
                                mUI?.refreshCardData(it.album, true)
                            },
                            onError = {
                                Timber.e(it)
                                v.finishLoadMore()
                                Snackbar.make(v.getContentView(), R.string.tip_load_error, Snackbar.LENGTH_SHORT).show()
                            },
                            onComplete = {
                                v.finishLoadMore()
                                v.setEnableLoadMore(mModel.hasNextPage())
                            })
                })
        mUI = ui
        firstLoadNGData()
    }

    private fun firstLoadNGData() {
        if (isDestroyed) {
            return
        }
        mModel.requestNGDateData(1,
                onStart = {
                    mUI?.contentType = ContentType.LOADING
                },
                onNext = {
                    mUI?.refreshCardData(it.album)
                    mUI?.contentType = ContentType.CONTENT
                },
                onError = {
                    Timber.e(it)
                    mUI?.contentType = ContentType.ERROR
                },
                onComplete = {
                    mUI?.setEnableLoadMore(mModel.hasNextPage())
                })
    }

    override fun notifyFavoriteNGDetailDataChanged() {
        mUI!!.refreshFavoriteData(NGRumtime.favoriteNGDataSupplier.getFavoriteAlbumData())
    }

    override fun destroy() {
        isDestroyed = true
        mModel.cancelPendingCall()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        mModel.onSaveInstanceState(outState)
    }

    override fun restoreDataIfNeed(savedInstanceState: Bundle?) {
        mModel.restoreDataIfNeed(savedInstanceState)
    }
}