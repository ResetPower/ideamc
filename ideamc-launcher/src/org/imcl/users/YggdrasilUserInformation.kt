package org.imcl.users

class YggdrasilUserInformation(val username: String, val password: String) : UserInformation {
    fun username() = username
    fun password() = password
}