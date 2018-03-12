package cc.bogerchan.geographic.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import cc.bogerchan.geographic.helper.BingHelper
import cc.bogerchan.geographic.repo.BingDailyRepository
import cc.bogerchan.geographic.util.CommonUtil

/**
 * Created by hb.chen on 2018/3/10.
 */
class BingDailyViewModel : ViewModel() {

    enum class UIState {
        LOADING, ERROR, NORMAL, REFRESHING, STOP_REFRESH
    }

    enum class ImageAction {
        CLEAR, NORMAL
    }

    private val mRepository by lazy { BingDailyRepository() }

    val dailyImage by lazy { MutableLiveData<Pair<ImageAction, String>>() }

    val dailyHtmlText by lazy { MutableLiveData<String>() }

    val uiState by lazy { MutableLiveData<UIState>() }

    fun requestDailyData(width: Int, height: Int) {
        dailyImage.value = Pair(ImageAction.NORMAL, "https://www.bing.com/ImageResolution.aspx?w=$width&h=$height")
        mRepository.requestDailyHtmlContent({ success, data ->
            if (success) {
                val contentMD5 = CommonUtil.md5(data!!)
                if (contentMD5 != BingHelper.lastContentMD5) {
                    dailyImage.value = Pair(ImageAction.CLEAR, "https://www.bing.com/ImageResolution.aspx?w=$width&h=$height")
                    dailyImage.value = Pair(ImageAction.NORMAL, "https://www.bing.com/ImageResolution.aspx?w=$width&h=$height")
                    BingHelper.saveLastContentMD5(contentMD5)
                }
                dailyHtmlText.value = BingHelper.addDailyPostCss(data)
            } else {
                dailyHtmlText.value = null
            }
        })
    }

    fun prepareData() {
        BingHelper.loadLastContentMD5()
    }
}