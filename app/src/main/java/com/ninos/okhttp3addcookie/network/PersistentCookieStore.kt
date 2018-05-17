package com.ninos.okhttp3addcookie.network

import android.content.SharedPreferences
import android.text.TextUtils
import okhttp3.Cookie
import okhttp3.HttpUrl
import com.ninos.okhttp3addcookie.widget.Application

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.ArrayList
import java.util.HashMap
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.experimental.and

/**
 * Created by ninos on 18-5-17.
 */
class PersistentCookieStore {
    private var cookies: HashMap<String, ConcurrentHashMap<String, Cookie>> = HashMap()
    private var cookiePrefs: SharedPreferences = Application.cookiePrefs

    init {
        //将持久化的cookies缓存到内存中 即map cookies
        var prefsMap = cookiePrefs.all
        prefsMap.entries.forEach { entry ->
            var cookieNames = TextUtils.split(entry.value.toString(), ",")
            cookieNames.forEach {
                var encodeCookie = cookiePrefs.getString(it, null)
                if (encodeCookie != null) {
                    var decodedCookie = decodeCookie(encodeCookie)
                    if (decodedCookie != null) {
                        if (!cookies.containsKey(entry.key))
                            cookies.put(entry.key, ConcurrentHashMap())
                        cookies[entry.key]!!.put(it, decodedCookie)
                    }
                }
            }
        }
    }

    private fun getCookieToken(cookie: Cookie): String = cookie.name() + "@" + cookie.domain()

    fun add(url: HttpUrl, cookie: Cookie) {
        var name = getCookieToken(cookie)

        //將cookies缓存到内存中，如果缓存过期，就重置此cookie
        if (cookies.containsKey(url.host()))
            cookies[url.host()]!!.remove(name)
        if (!cookies.containsKey(url.host()))
            cookies.put(url.host(), ConcurrentHashMap())
        cookies[url.host()]!!.put(name, cookie)

        //将cookies持久化到本地
        var prefsWriter = cookiePrefs.edit()
        prefsWriter.putString(url.host(), TextUtils.join(",", cookies[url.host()]!!.keys))
        prefsWriter.putString(name, encodeCookie(SerializableOkHttpCookies(cookie)))
        prefsWriter.apply()
    }

    fun get(url: HttpUrl): ArrayList<Cookie> {
        var ret = ArrayList<Cookie>()
        if (cookies.containsKey(url.host()))
            ret.addAll(cookies[url.host()]!!.values)
        return ret
    }

    fun removeAll(): Boolean {
        var prefsWriter = cookiePrefs.edit()
        prefsWriter.clear()
        prefsWriter.apply()
        cookies.clear()
        return true
    }

    fun remove(url: HttpUrl, cookie: Cookie): Boolean {
        var name = getCookieToken(cookie)
        return if (cookies.containsKey(url.host()) && cookies[url.host()]!!.containsKey(name)) {
            cookies[url.host()]!!.remove(name)
            var prefsWriter = cookiePrefs.edit()
            if (cookiePrefs.contains(name))
                prefsWriter.remove(name)
            prefsWriter.putString(url.host(), TextUtils.join(",", cookies[url.host()]!!.keys))
            prefsWriter.apply()
            true
        } else
            false
    }

    fun getCookies(): ArrayList<Cookie> {
        var ret = ArrayList<Cookie>()
        cookies.keys.forEach {
            ret.addAll(cookies[it]!!.values)
        }
        return ret
    }

    /**
     * cookies 序列化成 string
     *
     * @param cookie 要序列化的cookie
     * @return 序列化之后的string
     */
    private fun encodeCookie(cookie: SerializableOkHttpCookies): String? {
        if (cookie == null)
            return null
        var os = ByteArrayOutputStream()
        try {
            var outputStream = ObjectOutputStream(os)
            outputStream.writeObject(cookie)
        } catch (e: IOException) {
            return null
        }
        return byteArrayToHexString(os.toByteArray())
    }

    /**
     * 二进制数组转十六进制字符串
     *
     * @param bytes byte array to be converted
     * @return string containing hex values
     */
    private fun byteArrayToHexString(bytes: ByteArray): String {
        var sb = StringBuilder(bytes.size * 2)
        bytes.forEach {
            var v = it and 0xff.toByte()
            if (v < 16)
                sb.append('0')
            sb.append(Integer.toHexString(v.toInt()))
        }
        return sb.toString().toUpperCase(Locale.US)
    }

    /**
     * 将字符串反序列化成cookies
     *
     * @param cookieString cookies string
     * @return cookie object
     */
    private fun decodeCookie(cookieString: String): Cookie {
        var bytes = hexStringToByteArray(cookieString)
        var byteArrayInputStream = ByteArrayInputStream(bytes)
        var cookie: Cookie? = null
        try {
            var objectInputStream = ObjectInputStream(byteArrayInputStream)
            cookie = (objectInputStream.readObject() as SerializableOkHttpCookies).getCookies()
        } catch (e: IOException) {
        } catch (e: ClassNotFoundException) {
        }
        return cookie!!
    }

    /**
     * 十六进制字符串转二进制数组
     *
     * @param hexString string of hex-encoded values
     * @return decoded byte array
     */
    private fun hexStringToByteArray(hexString: String): ByteArray {
        var len = hexString.length
        var data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(hexString[i], 16) shl 4) + Character.digit(hexString[i + 1], 16)).toByte()
        }
        return data
    }
}