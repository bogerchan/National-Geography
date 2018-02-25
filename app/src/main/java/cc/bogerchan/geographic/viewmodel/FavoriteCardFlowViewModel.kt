package cc.bogerchan.geographic.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import cc.bogerchan.geographic.dao.NGCardData
import cc.bogerchan.geographic.dao.NGCardElementData
import cc.bogerchan.geographic.repo.FavoriteRepository
import cc.bogerchan.geographic.util.FetchStatus
import java.lang.UnsupportedOperationException

/**
 * Created by Boger Chan on 2018/1/31.
 */
class FavoriteCardFlowViewModel : ViewModel() {

    enum class UIState {
        LOADING, ERROR, REFRESH, FINISH_LOADING, FINISH_REFRESH
    }

    enum class OperationType {
        FETCH, ADD, REMOVE
    }

    val uiState = MutableLiveData<UIState>()
    private val mFavoriteRepository = FavoriteRepository()

    private val mCardOperation by lazy { MutableLiveData<Pair<OperationType, NGCardElementData?>>() }
    val cardDataList: LiveData<Pair<FetchStatus, List<NGCardData>>> by lazy {
        Transformations.switchMap(mCardOperation, {
            return@switchMap when (mCardOperation.value?.first) {
                OperationType.FETCH -> mFavoriteRepository.fetchCardDataList()
                OperationType.ADD -> mFavoriteRepository.addNGCardElementData(it.second!!)
                OperationType.REMOVE -> mFavoriteRepository.removeNGCardElementData(it.second!!)
                else -> throw UnsupportedOperationException("Unknown error type!")
            }
        })
    }

    fun fetchCardDataList() {
        mCardOperation.value = Pair(OperationType.FETCH, null)
    }

    fun addNGCardElementData(ngCardElementData: NGCardElementData) {
        mCardOperation.value = Pair(OperationType.ADD, ngCardElementData)
    }

    fun removeNGCardElementData(ngCardElementData: NGCardElementData) {
        mCardOperation.value = Pair(OperationType.REMOVE, ngCardElementData)
    }

    override fun onCleared() {
        mFavoriteRepository.clear()
    }
}