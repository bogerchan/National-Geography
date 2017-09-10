package me.boger.geographic.biz.detailpage

import android.os.Bundle
import io.reactivex.disposables.Disposable

/**
 * Created by BogerChan on 2017/7/1.
 */
interface IDetailPageModel {
    fun requestNGDetailData(id: String,
                            onStart: () -> Unit,
                            onError: (Throwable) -> Unit,
                            onComplete: () -> Unit,
                            onNext: (DetailPageData) -> Unit): Disposable

    fun cancelPendingCall()

    fun onSaveInstanceState(outState: Bundle?)

    fun restoreDataIfNeed(savedInstanceState: Bundle?)
}