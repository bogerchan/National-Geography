package me.boger.geographic.biz.detailpage

import me.boger.geographic.core.NGRumtime
import java.io.Serializable

/**
 * Created by BogerChan on 2017/7/1.
 */

data class DetailPageData(var counttotal: String, var picture: MutableList<DetailPagePictureData>) : Serializable

data class DetailPagePictureData(val id: String, val albumid: String, var title: String,
                                 var content: String, val url: String, val size: String,
                                 val addtime: String, var author: String, val thumb: String,
                                 val encoded: String, val weburl: String, val type: String,
                                 val yourshotlink: String, val copyright: String, val pmd5: String,
                                 val sort: String, var favorite: Boolean = false) : Serializable {
    override fun equals(other: Any?): Boolean {
        return other is DetailPagePictureData && id == other.id
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + albumid.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + addtime.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + thumb.hashCode()
        result = 31 * result + encoded.hashCode()
        result = 31 * result + weburl.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + yourshotlink.hashCode()
        result = 31 * result + copyright.hashCode()
        result = 31 * result + pmd5.hashCode()
        result = 31 * result + sort.hashCode()
        return result
    }

    @Transient
    private var locale: DetailPagePictureData? = null

    //can't use lazy prepare, not thread safe
    fun locale(): DetailPagePictureData {
        if (locale == null) {
            locale = copy(title = NGRumtime.locale(title),
                    content = NGRumtime.locale(content),
                    author = NGRumtime.locale(author))
        }
        return locale!!
    }

    fun clearLocale() {
        locale = null
    }
}