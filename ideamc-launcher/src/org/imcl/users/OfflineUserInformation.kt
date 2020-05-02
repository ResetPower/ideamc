package org.imcl.users

class OfflineUserInformation(val username: String) : UserInformation {
    override fun username() = username
}