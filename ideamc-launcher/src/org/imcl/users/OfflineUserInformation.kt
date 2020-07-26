package org.imcl.users

class OfflineUserInformation(var username: String) : UserInformation {
    override fun username() = username
}