package org.imcl.users

class YggdrasilUserInformation(var username: String, var uuid: String, var accessToken: String, var email: String) : UserInformation {
    override fun username() = username
}