package org.imcl.core

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import org.imcl.core.artifacts.ArtifactExtractor
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

object Launcher {
    fun launch(launchOptions: LaunchOptions) {
        val dir = File("${launchOptions.dir}/versions/${launchOptions.version}")
        if (!dir.exists()) {
            System.err.println("[IMCL Core] Version not found")
            return
        }
        val json = File("${dir.path}/${launchOptions.version}.json")
        if (!json.exists()) {
            System.err.println("[IMCL Core] JSON not found")
            return
        }
        val jsonObject = JSON.parseObject(json.readText())

        val p = Runtime.getRuntime().exec(arrayOf("sh", "-c", generateMacOSLaunchScript(launchOptions, jsonObject)))
        val fis: InputStream = p.inputStream
        val isr = InputStreamReader(fis)
        val br = BufferedReader(isr)
        var line: String? = null
        while (br.readLine().also { line = it } != null) {
            println(line)
        }
    }
    fun generateMacOSLaunchScript(launchOptions: LaunchOptions, jsonObject: JSONObject) : String {
        val sb = StringBuffer("/Library/Java/JavaVirtualMachines/jdk1.8.0_221.jdk/Contents/Home/bin/java -XstartOnFirstThread -Djava.library.path=\"${launchOptions.dir}/versions/${launchOptions.version}/${launchOptions.version}-natives\" -Dfml.ignoreInvalidMinecraftCertificates=true -Dfml.ignorePatchDiscrepancies=true ")
        val nativeFolder = File("${launchOptions.dir}/versions/${launchOptions.version}/${launchOptions.version}-natives")
        if (!nativeFolder.exists()) {
            nativeFolder.mkdir()
        }
        nativeFolder.listFiles().forEach {
            it.delete()
        }
        sb.append("-cp ")
        val cpBuff = StringBuffer("\"")
        val libraries = jsonObject.getJSONArray("libraries")
        val iterator = libraries.iterator()
        while (iterator.hasNext()) {
            val jsonObject = JSON.toJSON(iterator.next()) as JSONObject
            val downloads = jsonObject.getJSONObject("downloads")
            if (downloads.containsKey("classifiers")) {
                val artifact = downloads.getJSONObject("artifact")
                val path = artifact.get("path")
                val nativeLibFile = File("${launchOptions.dir}/libraries/$path")
                if (nativeLibFile.exists()) {
                    val macosNative = File("${launchOptions.dir}/libraries/${path.toString().removeSuffix(".jar")+"-natives-macos.jar"}")
                    if (macosNative.exists()) {
                        val files = ArtifactExtractor.extract(macosNative)
                        for (i in files) {
                            val f = File("${nativeFolder.path}/${i.first}")
                            if (!f.exists()) {
                                f.createNewFile()
                            }
                            f.writeBytes(i.second)
                        }
                    } else {
                        val files = ArtifactExtractor.extract(nativeLibFile)
                        for (i in files) {
                            val f = File("${nativeFolder.path}/${i.first}")
                            println(f.path)
                            if (!f.exists()) {
                                f.createNewFile()
                            }
                            f.writeBytes(i.second)
                        }
                    }
                }
            } else {
                val artifact = downloads.getJSONObject("artifact")
                val path = artifact.get("path")
                if (File("${launchOptions.dir}/libraries/$path").exists()) {
                    cpBuff.append("${launchOptions.dir}/libraries/$path:")
                }
            }
        }
        cpBuff.append("${launchOptions.dir}/versions/${launchOptions.version}/${launchOptions.version}.jar")
        sb.append("${cpBuff.removeSuffix(":")}\" ")
        sb.append(jsonObject.get("mainClass"))
        sb.append(" --username ${launchOptions.authenticator.username()} --version ${launchOptions.version} --gameDir \"${launchOptions.dir}\" --assetsDir \"${launchOptions.dir}/assets\" --assetIndex ${jsonObject.getJSONObject("assetIndex").get("id")} --uuid ${((launchOptions.authenticator.username().hashCode()).toString()).removePrefix("-")} --accessToken ${((launchOptions.authenticator.username().hashCode()+1).toString()).removePrefix("-")} --userType mojang --versionType release")
        return sb.toString()
    }
}