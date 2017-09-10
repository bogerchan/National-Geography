package me.boger.geographic.biz.selectpage

import me.boger.geographic.biz.detailpage.DetailPageData
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by BogerChan on 2017/6/30.
 */
interface DetailPageNetworkService {
    @GET("jiekou/albums/a{id}.html")
    fun requestNGDetailData(@Path("id") id: String): Observable<DetailPageData>
}