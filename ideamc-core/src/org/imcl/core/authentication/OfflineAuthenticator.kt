package org.imcl.core.authentication

class OfflineAuthenticator(val username: String) : Authenticator {
    override fun username() = username
    override fun uuid() = username.hashCode().toString()
    override fun accessToken() = username.hashCode().toString()+"1"
    override fun authenticate() {}
}