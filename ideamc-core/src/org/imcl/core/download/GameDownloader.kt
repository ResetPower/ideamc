package org.imcl.core.download

import org.imcl.core.http.HttpRequestSender

object GameDownloader {
    @JvmStatic
    fun getAllVersions() : String {
        return HttpRequestSender.readFileByUrl("http://launchermeta.mojang.com/mc/game/version_manifest.json") {
            println("Error occurred in getting all Minecraft versions")
        }
    }
}