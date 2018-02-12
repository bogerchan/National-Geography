package cc.bogerchan.geographic.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import cc.bogerchan.geographic.dao.NGCardData
import cc.bogerchan.geographic.repo.NGDailyPostRepository
import cc.bogerchan.geographic.util.FetchStatus

/**
 * Created by Boger Chan on 2018/1/31.
 */
class NGCardFlowViewModel : ViewModel() {
    enum class UIState {
        LOADING, ERROR, REFRESH, LOAD_MORE, FINISH_LOADING, FINISH_REFRESH, FINISH_LOAD_MORE
    }

    enum class FetchType {
        FROM_FIRST, FOR_NEXT
    }

    private val mEmptyCardDataWithElements = MutableLiveData<Pair<FetchStatus, NGCardData>>()
    val uiState by lazy { MutableLiveData<UIState>() }
    val cardDataList: LiveData<Pair<FetchStatus, List<NGCardData>>> by lazy {
        Transformations.switchMap(mFetchType, {
            when (it) {
                FetchType.FROM_FIRST -> mNGDailyPostRepository.fetchCardDataListByPageFromFirst()
                FetchType.FOR_NEXT -> mNGDailyPostRepository.fetchCardDataListForNext()
                else -> mNGDailyPostRepository.fetchCardDataListByPageFromFirst()
            }
        })
    }
    val cardDataWithElements: LiveData<Pair<FetchStatus, NGCardData>> by lazy {
        Transformations.switchMap(mCardDataWithElementsForQuery, {
            it ?: return@switchMap mEmptyCardDataWithElements
            mNGDailyPostRepository.fetchCardDataElements(it)
        })
    }
    private val mNGDailyPostRepository by lazy { NGDailyPostRepository() }
    private val mFetchType by lazy { MutableLiveData<FetchType>() }
    private val mCardDataWithElementsForQuery by lazy { MutableLiveData<NGCardData>() }


    fun startFetch(fetchType: FetchType) {
        mFetchType.value = fetchType
    }

    fun startQueryCardDataElements(cardData: NGCardData) {
        mCardDataWithElementsForQuery.value = cardData
    }

    fun resetCardDataElements() {
        mCardDataWithElementsForQuery.value = null
        mEmptyCardDataWithElements.value = null
    }

    override fun onCleared() {
        mNGDailyPostRepository.clear()
    }
}