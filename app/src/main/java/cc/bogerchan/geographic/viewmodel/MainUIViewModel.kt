package cc.bogerchan.geographic.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

/**
 * Created by Boger Chan on 2018/1/28.
 */
class MainUIViewModel : ViewModel() {

    enum class MenuState {
        SHOW_MENU, CLOSE_MENU, SHOW_RETURN, BACK_FROM_RETURN
    }

    enum class UIAction {
        LOADING, FINISH_LOADING, GO_TO_NG_SHOW_PAGE, NORMAL
    }

    val menuState = MutableLiveData<MenuState>()

    val uiAction = MutableLiveData<UIAction>()

    val isTitleShowing = MutableLiveData<Boolean>()
}