package com.ninos.okhttp3addcookie.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit
import okhttp3.OkHttpClient

/**
 * Created by ninos on 2017/5/26.
 * 线程安全的懒加载单例模式
 */
class Net {
    companion object {
        fun getService(): ApiService = Holder.retrofit.create(ApiService::class.java)
    }

    //带cookie的okhttp请求
    private object Holder {
        val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Const.BASE_URL)
                .client(OkHttpClient().newBuilder().cookieJar(object : CookieJar {
                    val cookieStore = PersistentCookieStore()
                    override fun saveFromResponse(url: HttpUrl, cookies: MutableList<Cookie>) {
                        for (cookie in cookies) {
                            cookieStore.add(url, cookie)
                        }
                    }

                    override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
                        return cookieStore.get(url)
                    }
                }).build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
    }
}