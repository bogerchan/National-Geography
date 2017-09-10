package me.boger.geographic.biz.selectpage

import android.os.Bundle
import me.boger.geographic.core.NGRumtime
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/**
 * Created by BogerChan on 2017/6/28.
 */

class SelectPageModelImpl : ISelectPageModel {

    private var mSelectDateDataList = arrayListOf<SelectPageData>()
    private val mSelectDateNetworkService by lazy {
        NGRumtime.retrofit.create(SelectPageNetworkService::class.java)
    }

    private val KEY_MODEL_SELECT_DATE = "key_model_select_date"

    override var currentPage: Int = 1

    private var totalPage: Int = 0

    private val mPendingCall = mutableListOf<Disposable>()

    override fun hasNextPage(): Boolean {
        return currentPage < totalPage
    }

    override fun requestNGDateData(
            pageIdx: Int,
            onStart: () -> Unit,
            onError: (Throwable) -> Unit,
            onComplete: () -> Unit,
            onNext: (SelectPageData) -> Unit
    ): Disposable {
        val pageIdxStr: String = pageIdx.toString()
        mSelectDateDataList.forEach {
            if (it.page == pageIdxStr) {
                return Observable.just(it)
                        .doOnNext {
                            currentPage = it.page.toInt()
                            totalPage = it.pagecount.toInt()
                            if (currentPage == 1) {
                                it.album[0] = NGRumtime.favoriteNGDataSupplier.getFavoriteAlbumData()
                            }
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(onError, onComplete, onNext)
            }
        }
        var disposable: Disposable? = null
        disposable = mSelectDateNetworkService.requestNGDateData(pageIdx)
                .doOnNext {
                    mSelectDateDataList.add(it)
                    currentPage = it.page.toInt()
                    totalPage = it.pagecount.toInt()
                    if (currentPage == 1) {
                        it.album.add(0, NGRumtime.favoriteNGDataSupplier.getFavoriteAlbumData())
                    }
                }
                .doOnSubscribe { onStart() }
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        {
                            onError(it)
                            mPendingCall.remove(disposable)
                        },
                        {
                            onComplete()
                            mPendingCall.remove(disposable)
                        },
                        onNext)
        mPendingCall.add(disposable)
        return disposable
    }


    override fun cancelPendingCall() {
        mPendingCall.forEach {
            if (!it.isDisposed) {
                it.dispose()
            }
        }
        mPendingCall.clear()
    }

    override fun clearCache() {
        mSelectDateDataList.clear()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        if (outState == null) {
            return
        }
        outState.putSerializable(KEY_MODEL_SELECT_DATE, mSelectDateDataList)
    }

    override fun restoreDataIfNeed(savedInstanceState: Bundle?) {
        if (savedInstanceState == null
                || !savedInstanceState.containsKey(KEY_MODEL_SELECT_DATE)) {
            return
        }
        mSelectDateDataList = savedInstanceState
                .getSerializable(KEY_MODEL_SELECT_DATE) as ArrayList<SelectPageData>
    }
}