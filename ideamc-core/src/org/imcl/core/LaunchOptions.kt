package org.imcl.core

import org.imcl.core.authentication.Authenticator

class LaunchOptions(val dir: String, val version: String, val authenticator: Authenticator, val javaPath: String, val jvmArgs: String = "", val minecraftArgs: String = "", val gameDirectory: String? = null)