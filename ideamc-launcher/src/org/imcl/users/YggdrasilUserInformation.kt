package org.imcl.users

class YggdrasilUserInformation(val username: String, val uuid: String, val accessToken: String) : UserInformation {
    fun username() = username
    fun uuid() = uuid
    fun accessToken() = accessToken
}