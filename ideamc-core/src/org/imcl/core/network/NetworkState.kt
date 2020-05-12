package org.imcl.core.network

import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL


object NetworkState {
    @JvmStatic
    fun isConnectedToInternet(): Boolean {
        try {
            val url = URL("http://baidu.com")
            try {
                val `in`: InputStream = url.openStream()
                `in`.close()
                return true
            } catch (e: IOException) {
                return false
            }
        } catch (e: MalformedURLException) {
            return false
        }
    }
}