package org.imcl.core.authentication

import com.alibaba.fastjson.JSON
import org.imcl.core.http.HttpRequestSender.post

class YggdrasilAuthenticator(val username: String, val uuid: String, val accessToken: String) : Authenticator {
    override fun username() = username
    override fun uuid() = uuid
    override fun accessToken() = accessToken
    companion object {
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