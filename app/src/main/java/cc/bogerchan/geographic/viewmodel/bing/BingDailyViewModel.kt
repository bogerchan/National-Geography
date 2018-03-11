package cc.bogerchan.geographic.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import cc.bogerchan.geographic.repo.BingDailyRepository

/**
 * Created by hb.chen on 2018/3/10.
 */
class BingDailyViewModel : ViewModel() {

    enum class UIState {
        LOADING, ERROR, NORMAL, REFRESHING, STOP_REFRESH
    }

    private val mRepository by lazy { BingDailyRepository() }

    val dailyImage by lazy { MutableLiveData<String>() }

    val dailyHtmlText by lazy { MutableLiveData<String>() }

    val uiState by lazy { MutableLiveData<UIState>() }

    fun requestDailyData(width: Int, height: Int) {
        dailyImage.value = "https://www.bing.com/ImageResolution.aspx?w=$width&h=$height"
        mRepository.requestDailyHtmlContent({ success, data ->
            if (success) {
                dailyHtmlText.value = data
            } else {
                dailyHtmlText.value = null
            }
        })
    }
}