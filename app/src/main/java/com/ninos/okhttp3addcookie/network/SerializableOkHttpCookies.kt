package com.ninos.okhttp3addcookie.network

import okhttp3.Cookie
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable


/**
 * Created by ninos on 18-5-11.
 */
class SerializableOkHttpCookies(@field:Transient private val cookies: Cookie) : Serializable {
    @Transient private var clientCookies: Cookie? = null

    fun getCookies(): Cookie {
        return if (clientCookies != null) clientCookies!! else cookies
    }

    @Throws(IOException::class)
    private fun writeObject(out: ObjectOutputStream) {
        out.writeObject(cookies.name())
        out.writeObject(cookies.value())
        out.writeLong(cookies.expiresAt())
        out.writeObject(cookies.domain())
        out.writeObject(cookies.path())
        out.writeBoolean(cookies.secure())
        out.writeBoolean(cookies.httpOnly())
        out.writeBoolean(cookies.hostOnly())
        out.writeBoolean(cookies.persistent())
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(i: ObjectInputStream) {
        val name = i.readObject() as String
        val value = i.readObject() as String
        val expiresAt = i.readLong()
        val domain = i.readObject() as String
        val path = i.readObject() as String
        val secure = i.readBoolean()
        val httpOnly = i.readBoolean()
        val hostOnly = i.readBoolean()
        val persistent = i.readBoolean()
        var builder = Cookie.Builder()
        builder = builder.name(name)
        builder = builder.value(value)
        builder = builder.expiresAt(expiresAt)
        builder = if (hostOnly) builder.hostOnlyDomain(domain) else builder.domain(domain)
        builder = builder.path(path)
        builder = if (secure) builder.secure() else builder
        builder = if (httpOnly) builder.httpOnly() else builder
        clientCookies = builder.build()
    }
}