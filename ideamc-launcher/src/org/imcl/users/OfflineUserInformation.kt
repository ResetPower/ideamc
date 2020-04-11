package org.imcl.users

class OfflineUserInformation(val username: String) : UserInformation {
    fun username() = username
}