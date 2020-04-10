package org.imcl.core.authentication

interface Authenticator {
    fun username() : String
    fun uuid(): String
    fun accessToken(): String
    fun authenticate()
}