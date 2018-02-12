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

    enum class FetchType {
        ALL
    }

    enum class OperationType {
        ADD, REMOVE
    }

    val uiState = MutableLiveData<UIState>()
    private val mFavoriteRepository = FavoriteRepository()

    private val mFetchType by lazy { MutableLiveData<FetchType>() }
    val cardDataList: LiveData<Pair<FetchStatus, List<NGCardData>>> by lazy {
        Transformations.switchMap(mFetchType, {
            return@switchMap when (mFetchType.value) {
                FetchType.ALL -> mFavoriteRepository.fetchCardDataList()
                else -> throw UnsupportedOperationException("Unknown error type!")
            }
        })
    }
    private val mNGCardElementOperation by lazy { MutableLiveData<Pair<OperationType, NGCardElementData>>() }
    val ngCardElementUpdate: LiveData<Pair<FetchStatus, List<NGCardData>>> by lazy {
        Transformations.switchMap(mNGCardElementOperation, {
            return@switchMap when (it.first) {
                OperationType.ADD -> mFavoriteRepository.addNGCardElementData(it.second)
                OperationType.REMOVE -> mFavoriteRepository.removeNGCardElementData(it.second)
                else -> throw UnsupportedOperationException("Unknown error type!")
            }
        })
    }

    fun fetchCardDataList() {
        mFetchType.value = FetchType.ALL
    }

    fun addNGCardElementData(ngCardElementData: NGCardElementData) {
        mNGCardElementOperation.value = Pair(OperationType.ADD, ngCardElementData)
    }

    fun removeNGCardElementData(ngCardElementData: NGCardElementData) {
        mNGCardElementOperation.value = Pair(OperationType.REMOVE, ngCardElementData)
    }

    override fun onCleared() {
        mFavoriteRepository.clear()
    }
}