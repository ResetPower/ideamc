package org.imcl.core.bmclapi

import org.imcl.core.http.HttpRequestSender

fun String.toBMCLAPIUrl() : String {
    val url = replace("launchermeta.mojang.com/", "bmclapi2.bangbang93.com/")
        .replace("launcher.mojang.com/", "bmclapi2.bangbang93.com/")
        .replace("resources.download.minecraft.net/", "bmclapi2.bangbang93.com/assets/")
        .replace("libraries.minecraft.net/", "bmclapi2.bangbang93.com/maven/")
        .replace("files.minecraftforge.net/maven", "bmclapi2.bangbang93.com/maven")
    val result = HttpRequestSender.get(url) {
        throw Exception()
    }
    val split = result.split("\n")
    return split.last().removePrefix("Found. Redirecting to ")
}
