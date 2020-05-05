package org.imcl.files

import java.io.File

object FileChecker {
    fun check() {
        val imclDir = File("imcl")
        if (!imclDir.exists()) {
            imclDir.mkdirs()
        }
        val accDir = File("imcl/account")
        if (!accDir.exists()) {
            accDir.mkdirs()
        }
        val accFile = File("imcl/account/acinf.text")
        if (!accFile.exists()) {
            accFile.writeText("""
                accessToken=none
                uuid=none
                username=none
            """.trimIndent())
        }
        val lauDir = File("imcl/launcher")
        if (!lauDir.exists()) {
            lauDir.mkdirs()
        }
        val lauPro = File("imcl/launcher/launcher_profiles.json")
        if (!lauPro.exists()) {
            lauPro.writeText("[]")
        }
        val proDir = File("imcl/properties")
        if (!proDir.exists()) {
            proDir.mkdirs()
        }
        val proPro = File("imcl/properties/ideamc.properties")
        if (!proPro.exists()) {
            proPro.writeText("""
                javapath=java
                isLoggedIn=false
                language=english
            """.trimIndent())
        }
        val cacheDir = File("imcl/cache")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

    }
}