package me.boger.geographic.biz.selectpage

import me.boger.geographic.core.NGRumtime
import java.io.Serializable

/**
 * Created by BogerChan on 2017/6/28.
 */
data class SelectPageData(val total: String, var page: String, var pagecount: String,
                          var album: MutableList<SelectPageAlbumData>) : Serializable

data class SelectPageAlbumData(var id: String, var title: String, var url: String,
                               var addtime: String, var adshow: String, var fabu: String,
                               var encoded: String, var amd5: String, var sort: String,
                               var ds: String, var timing: String, var timingpublish: String) : Serializable {

    @Transient
    private var locale: SelectPageAlbumData? = null

    //can't use lazy prepare, not thread safe
    fun locale(): SelectPageAlbumData {
        if (locale == null) {
            locale = copy(title = NGRumtime.locale(title))
        }
        return locale!!
    }
}