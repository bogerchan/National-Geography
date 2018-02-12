package cc.bogerchan.geographic.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import cc.bogerchan.geographic.dao.NGCardData

/**
 * Created by Boger Chan on 2018/2/4.
 */
class NGCardShowViewModel : ViewModel() {

    val isMenuShowing = MutableLiveData<Boolean>()

    val cardData = MutableLiveData<NGCardData>()

    fun performOperationSave(ctx: Context) {

    }

    fun performOperationShare(ctx: Context) {

    }
}