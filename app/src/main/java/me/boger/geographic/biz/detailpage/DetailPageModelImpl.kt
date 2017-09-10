package me.boger.geographic.biz.detailpage

import android.os.Bundle
import me.boger.geographic.core.NGRumtime
import me.boger.geographic.biz.selectpage.DetailPageNetworkService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/**
 * Created by BogerChan on 2017/6/30.
 */
class DetailPageModelImpl : IDetailPageModel {
    private val mService by lazy {
        NGRumtime.retrofit.create(DetailPageNetworkService::class.java)
    }

    private var disposable: Disposable? = null

    private val KEY_MODEL_NG_DETAIL_DATA = "key_model_ng_detail_data"

    private var mBufferedData: DetailPageData? = null

    private var isUseBufferedData = false

    override fun requestNGDetailData(id: String,
                                     onStart: () -> Unit,
                                     onError: (Throwable) -> Unit,
                                     onComplete: () -> Unit,
                                     onNext: (DetailPageData) -> Unit): Disposable {
        val buffData = mBufferedData
        if (buffData != null && isUseBufferedData) {
            isUseBufferedData = false
            return Observable.just(buffData)
                    .doOnSubscribe { onStart() }
                    .subscribeBy(onError, onComplete, onNext)
        }
        val dis = mService.requestNGDetailData(id)
                .doOnSubscribe { onStart() }
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError, onComplete, {
                    mBufferedData = it
                    onNext(it)
                })
        disposable = dis
        return dis
    }

    override fun cancelPendingCall() {
        val dis = disposable
        if (dis != null && !dis.isDisposed) {
            dis.dispose()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        if (outState == null || mBufferedData == null) {
            return
        }
        outState.putSerializable(KEY_MODEL_NG_DETAIL_DATA, mBufferedData)
    }

    override fun restoreDataIfNeed(savedInstanceState: Bundle?) {
        if (savedInstanceState == null || !savedInstanceState.containsKey(KEY_MODEL_NG_DETAIL_DATA)) {
            return
        }
        isUseBufferedData = true
        mBufferedData = savedInstanceState.getSerializable(KEY_MODEL_NG_DETAIL_DATA) as DetailPageData
    }
}