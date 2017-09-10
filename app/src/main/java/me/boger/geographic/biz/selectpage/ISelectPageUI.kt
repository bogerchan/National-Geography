package me.boger.geographic.biz.selectpage

import android.view.View
import me.boger.geographic.biz.common.ContentType

/**
 * Created by BogerChan on 2017/6/27.
 */
interface ISelectPageUI {

    var contentType: ContentType

    fun getContentView(): View

    fun refreshFavoriteData(favoriteData: SelectPageAlbumData)

    fun refreshCardData(data: List<SelectPageAlbumData>, append: Boolean = false)

    fun finishLoadMore()

    fun finishRefreshing()

    fun setOnRefreshListener(
            onRefresh: (ISelectPageUI) -> Unit,
            onLoadMore: (ISelectPageUI) -> Unit)

    fun setOnRetryClickListener(listener: (view: View) -> Unit)

    fun setEnableLoadMore(canLoadMore: Boolean)
}