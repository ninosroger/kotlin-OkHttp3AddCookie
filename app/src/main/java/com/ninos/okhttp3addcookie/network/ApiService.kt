package com.ninos.okhttp3addcookie.network

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import rx.Observable

/**
 * Created by ninos on 2017/5/26.
 */
interface ApiService {
    /*登录
    username	字符串	帐号
    password	字符串	密码*/
    @FormUrlEncoded
    @POST("/login")
    fun login(@Field("username") username: String, @Field("password") password: String): Observable<String>
}
