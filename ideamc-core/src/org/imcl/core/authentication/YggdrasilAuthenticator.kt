package org.imcl.core.authentication

import com.alibaba.fastjson.JSON
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.net.URLEncoder

class YggdrasilAuthenticator(val username: String, val uuid: String, val accessToken: String) : Authenticator {
    override fun username() = username
    override fun uuid() = uuid
    override fun accessToken() = accessToken
    companion object {
        @JvmStatic
        fun post(spec: String, param: String, whenError: () -> Unit): String? {
            var responseBuilder: StringBuilder? = null
            var reader: BufferedReader? = null
            var wr: OutputStreamWriter? = null
            val url: URL
            try {
                url = URL(spec)
                val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 1000 * 5
                wr = OutputStreamWriter(conn.outputStream)
                wr.write(param)
                wr.flush()
                reader = BufferedReader(InputStreamReader(conn.inputStream))
                responseBuilder = StringBuilder()
                var line: String? = null
                while (reader.readLine().also { line = it } != null) {
                    responseBuilder.append(line)
                }
                wr.close()
                reader.close()
                return responseBuilder.toString()
            } catch (e: IOException) {
                whenError()
                return null
            }
        }
        @JvmStatic
        fun sendPost(url: String, param: String, whenError: () -> Unit): String? {
            var out: PrintWriter? = null
            var `in`: BufferedReader? = null
            var result: String? = ""
            try {
                val realUrl = URL(url)
                // 打开和URL之间的连接
                val conn: URLConnection = realUrl.openConnection()
                // 设置通用的请求属性
                conn.setRequestProperty("Content-Type", "application/json")
                // 发送POST请求必须设置如下两行
                conn.doOutput = true
                conn.doInput = true
                // 获取URLConnection对象对应的输出流
                out = PrintWriter(conn.getOutputStream())
                // 发送请求参数
                out.print(URLEncoder.encode(param, "utf-8"))
                // flush输出流的缓冲
                out.flush()
                // 定义BufferedReader输入流来读取URL的响应
                `in` = BufferedReader(
                    InputStreamReader(conn.getInputStream())
                )
                var line: String?
                while (`in`.readLine().also { line = it } != null) {
                    result += line
                }
            } catch (e: Exception) {
                whenError()
            } finally {
                try {
                    out?.close()
                    `in`?.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
            return result
        }
        @JvmStatic
        fun authenticate(username: String, password: String) : String {
            var ret = true
            val response = post("https://authserver.mojang.com/authenticate", """
                {
                   "agent": {
                       "name": "Minecraft",
                       "version": 1
                   },
                   "username": "$username",
                   "password": "$password"
                }
            """.trimIndent()) {
                ret = false
            }
            if (ret) {
                val obj = JSON.parseObject(response)
                val selectedProfile = obj.getJSONObject("selectedProfile")
                val player = selectedProfile.getString("name")
                val uuid = selectedProfile.getString("id")
                val accessToken = obj.getString("accessToken")
                return "$ret $player $uuid $accessToken"
            } else return "false"
        }
        @JvmStatic
        fun validate(accessToken: String) : Boolean {
            var ret = true
            post("https://authserver.mojang.com/validate", """
                {
                   "accessToken": "$accessToken"
                }
            """.trimIndent()) {
                ret = false
            }
            return ret
        }
        @JvmStatic
        fun refresh(accessToken: String) : String {
            return JSON.parseObject(post("https://authserver.mojang.com/refresh", """
                {
                   "accessToken": "$accessToken"
                }
            """.trimIndent()) {
            }?:throw NullPointerException("Exception occurred in refreshing")).getString("accessToken")
        }
    }
}