package org.imcl.core

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import javafx.application.Platform
import org.imcl.core.exceptions.LauncherCoreException
import org.imcl.core.log.Log
import org.imcl.core.ostool.OS
import org.imcl.core.ostool.OSTool
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception

object Launcher {
    val separator = File.separator
    fun launch(launchOptions: LaunchOptions, whenDone: () -> Unit = {}) {
        val dir = File("${launchOptions.dir}${separator}versions${separator}${launchOptions.version}")
        if (!dir.exists()) {
            Log.e("Version not found.")
            throw LauncherCoreException("Version directory not found: ${dir.path}")
        }
        val json = File("${dir.path}${separator}${launchOptions.version}.json")
        if (!json.exists()) {
            Log.e("JSON not found")
            throw LauncherCoreException("JSON not found: ${json.path}")
            return
        }
        val jsonObject = JSON.parseObject(json.readText())

        Log.i("Looking for OS!")
        val os = OSTool.getOS()
        Log.i("Found OS! $os!")

        if (os==OS.Unknown) {
            whenDone()
            Log.e("Unknown OS is not supported!")
            throw LauncherCoreException("Unknown OS is not supported!")
            return
        }
        Thread {
            Log.i("Generating $os Launch Script!")
            val cmd = if (jsonObject.containsKey("patches")) {
                generateLaunchScript(launchOptions, jsonObject.getJSONArray("patches").getJSONObject(0), os)
            } else {
                generateLaunchScript(launchOptions, jsonObject, os)
            }
            var c = cmd.indexOf("--accessToken")+14
            var end = c
            while (true) {
                if (cmd[end]==' ') {
                    break
                }
                end++
            }
            Log.i("Generated Launch Script\n${cmd.replace(cmd.substring(c, end), "*****")}")
            val p = if (os==OS.MacOS||os==OS.Linux) {
                Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd), null, File(launchOptions.dir))
            } else if (os==OS.Windows||os==OS.Windows10) {
                Runtime.getRuntime().exec(arrayOf("cmd.exe", "/C", cmd), null, File(launchOptions.dir))
            } else {
                Runtime.getRuntime().exec(arrayOf(cmd), null, File(launchOptions.dir))
            }
            val fis: InputStream = p.inputStream
            val isr = InputStreamReader(fis)
            val br = BufferedReader(isr)
            var line: String? = null
            var flag = false
            Log.i("Launching Minecraft!")
            while (br.readLine().also { line = it } != null) {
                println(line)
                if (!flag) {
                    flag = true
                    Platform.runLater {
                        whenDone()
                    }
                }
            }
        }.start()
    }
    fun genInitiallyLaunchScript(os: OS, isHigherThan1_13: Boolean, launchOptions: LaunchOptions) : String {
        return if (os==OS.MacOS) {
            "\"${launchOptions.javaPath}\"${if (isHigherThan1_13) " -XstartOnFirstThread" else ""} ${launchOptions.jvmArgs} -Djava.library.path=\"${launchOptions.dir}${separator}versions${separator}${launchOptions.version}${separator}${launchOptions.version}-natives\" "
        } else if (os==OS.Windows10) {
            "\"${launchOptions.javaPath}\" -XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump ${launchOptions.jvmArgs} \"-Djava.library.path=${launchOptions.dir}${separator}versions${separator}${launchOptions.version}${separator}${launchOptions.version}-natives\" "
        } else if (os==OS.Windows) {
            "\"${launchOptions.javaPath}\" -XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump ${launchOptions.jvmArgs} \"-Djava.library.path=${launchOptions.dir}${separator}versions${separator}${launchOptions.version}${separator}${launchOptions.version}-natives\" "
        } else if (os==OS.Linux) {
            "\"${launchOptions.javaPath}\" ${launchOptions.jvmArgs} -Djava.library.path=\"${launchOptions.dir}${separator}versions${separator}${launchOptions.version}${separator}${launchOptions.version}-natives\" "
        } else {
            throw Exception("Not supported OS: $os")
        }
    }
    fun generateLaunchScript(launchOptions: LaunchOptions, jsonObject: JSONObject, os: OS) : String {
        var isHigherThan1_13 = !jsonObject.containsKey("minecraftArguments")
        val sb = StringBuffer(genInitiallyLaunchScript(os, isHigherThan1_13, launchOptions))
        var inheritsFrom: String? = null
        var inheritsObject: JSONObject? = null
        if (jsonObject.containsKey("inheritsFrom")) {
            inheritsFrom = jsonObject.getString("inheritsFrom")
            inheritsObject = JSON.parseObject(File("${launchOptions.dir}${separator}versions${separator}$inheritsFrom${separator}$inheritsFrom.json").readText())
        }
        val nativeFolder = File("${launchOptions.dir}${separator}versions${separator}${launchOptions.version}${separator}${launchOptions.version}-natives")
        if (!nativeFolder.exists()) {
            nativeFolder.mkdir()
        }
        nativeFolder.listFiles().forEach {
            it.delete()
        }
        sb.append("-cp ")
        sb.append("${LauncherTool.genCp(jsonObject, launchOptions, os, inheritsFrom, inheritsObject, nativeFolder)}\" ")
        sb.append(jsonObject.get("mainClass"))
        sb.append(" --username ${launchOptions.authenticator.username()} --version ${launchOptions.version} --gameDir \"${launchOptions.gameDirectory ?: launchOptions.dir}\" --assetsDir \"${launchOptions.dir}${separator}assets\" --assetIndex ${if (inheritsObject==null) jsonObject.getJSONObject("assetIndex").get("id") else inheritsObject.getJSONObject("assetIndex").get("id")} --uuid ${launchOptions.authenticator.uuid()} --accessToken ${launchOptions.authenticator.accessToken()} --userType mojang ${launchOptions.minecraftArgs} --versionType release")
        if (inheritsFrom!=null) {
            if (jsonObject.containsKey("arguments")) {
                val args = jsonObject.getJSONObject("arguments").getJSONArray("game")
                val iterator = args.iterator()
                while (iterator.hasNext()) {
                    sb.append(" ${iterator.next()}")
                }
            } else if (jsonObject.containsKey("minecraftArguments")) {
                val minecraftArguments = jsonObject.getString("minecraftArguments")
                val indexOfResult = minecraftArguments.indexOf(" --tweakClass")
                sb.removeSuffix("--versionType release")
                sb.append(minecraftArguments.substring(indexOfResult, minecraftArguments.length))
            }
        }
        return sb.toString()
    }
}