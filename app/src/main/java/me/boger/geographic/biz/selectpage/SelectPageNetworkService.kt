package me.boger.geographic.biz.selectpage

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by BogerChan on 2017/6/30.
 */
interface SelectPageNetworkService {
    @GET("jiekou/mains/p{page}.html")
    fun requestNGDateData(@Path("page") page: Int): Observable<SelectPageData>
}