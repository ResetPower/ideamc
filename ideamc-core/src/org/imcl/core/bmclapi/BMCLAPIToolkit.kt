package org.imcl.core.bmclapi

import org.imcl.core.http.HttpRequestSender
import java.lang.Exception

fun String.toBMCLAPIUrl() : String {
    val url = replace("https://launchermeta.mojang.com/", "https://bmclapi2.bangbang93.com/")
        .replace("https://launcher.mojang.com/", "https://bmclapi2.bangbang93.com/")
        .replace("http://resources.download.minecraft.net/", "https://bmclapi2.bangbang93.com/assets/")
        .replace("https://libraries.minecraft.net/", "https://bmclapi2.bangbang93.com/maven/")
    val result = HttpRequestSender.get(url) {
        throw Exception()
    }
    val split = result.split("\n")
    return split.last().removePrefix("Found. Redirecting to ")
}