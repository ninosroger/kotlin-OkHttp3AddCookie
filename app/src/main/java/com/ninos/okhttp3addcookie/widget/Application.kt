package com.ninos.okhttp3addcookie.widget

import android.app.Application
import android.content.SharedPreferences
import com.ninos.okhttp3addcookie.network.Const.COOKIE_PREFS

/**
 * Created by ninos on 2017/6/1.
 */
class Application : Application() {
    companion object {
        lateinit var cookiePrefs: SharedPreferences
    }

    override fun onCreate() {
        super.onCreate()
        cookiePrefs = getSharedPreferences(COOKIE_PREFS, 0)
    }
}