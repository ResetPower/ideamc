package org.imcl.core.authentication

import com.alibaba.fastjson.JSON
import javafx.scene.control.Alert
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.URL
import java.net.URLConnection

class YggdrasilAuthenticator(val username: String, val password: String) : Authenticator {
    var player = ""
    var uuid = ""
    var accessToken = ""
    override fun username() = player
    override fun uuid() = uuid
    override fun accessToken() = accessToken
    override fun authenticate() {
        val result = sendPost("https://authserver.mojang.com/authenticate", """
            {
               "agent": {
                   "name": "Minecraft",
                   "version": 1
               },
               "username": "$username",
               "password": "$password",
               "clientToken": "imcl-xxx"
            }
            """.trimIndent())
        if (result==null) {
            throw NullPointerException("result")
        } else {
            val obj = JSON.parseObject(result)
            accessToken = obj.getString("accessToken")
            val profile = obj.getJSONObject("selectedProfile")
            uuid = profile.getString("id")
            player = profile.getString("name")
        }
    }
    fun sendPost(url: String, param: String): String? {
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
            conn.setDoOutput(true)
            conn.setDoInput(true)
            // 获取URLConnection对象对应的输出流
            out = PrintWriter(conn.getOutputStream())
            // 发送请求参数
            out!!.print(param)
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
            val alert = Alert(Alert.AlertType.INFORMATION)
            alert.title = "Password error"
            alert.contentText = "Password error"
            alert.show()
        } //使用finally块来关闭输出流、输入流
        finally {
            try {
                out?.close()
                `in`?.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
        return result
    }
}