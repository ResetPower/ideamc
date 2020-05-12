package org.imcl.core

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import javafx.application.Platform
import javafx.scene.control.*
import javafx.scene.layout.VBox
import org.imcl.core.constraints.logger
import org.imcl.core.exceptions.LauncherCoreException
import org.imcl.core.ostool.OS
import org.imcl.core.ostool.OSTool
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.util.*

object Launcher {
    val separator = File.separator
    fun launch(launchOptions: LaunchOptions, whenDone: () -> Unit = {}, whenFinish: () -> Unit = {}) {
        val dir = File("${launchOptions.dir}${separator}versions${separator}${launchOptions.version}")
        logger.info("Minecraft version directory: ${dir.path}")
        if (!dir.exists()) {
            logger.error("Version directory not found: ${dir.path}")
            throw LauncherCoreException("Version directory not found: ${dir.path}")
        }
        val json = File("${dir.path}${separator}${launchOptions.version}.json")
        logger.info("Minecraft json file: ${json.path}")
        if (!json.exists()) {
            logger.error("Version JSON not found: ${json.path}")
            throw LauncherCoreException("JSON not found: ${json.path}")
            return
        }
        val jsonObject = JSON.parseObject(json.readText())

        logger.info("Looking for OS")
        val os = OSTool.getOS()
        logger.info("Found OS: $os")

        if (os==OS.Unknown) {
            whenDone()
            logger.error("Unknown OS is not supported!")
            throw LauncherCoreException("Unknown OS is not supported!")
            return
        }
        Thread {
            logger.info("Generating $os Launch Script!")
            val cmd = if (jsonObject.containsKey("patches")) {
                generateLaunchScript(launchOptions, jsonObject.getJSONArray("patches").getJSONObject(0), os)
            } else {
                generateLaunchScript(launchOptions, jsonObject, os)
            }
            logger.info("Encrypting accessToken")
            var c = cmd.indexOf("--accessToken")+14
            var end = c
            while (true) {
                if (cmd[end]==' ') {
                    break
                }
                end++
            }
            logger.info("Encrypted accessToken")
            logger.info("Generated Launch Script\n${cmd.replace(cmd.substring(c, end), "*****")}")
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
            logger.info("Launching Minecraft!")
            val lines = Vector<String>()
            while (br.readLine().also { line = it } != null) {
                println(line)
                lines.add(line)
                if (!flag) {
                    flag = true
                    Platform.runLater {
                        whenDone()
                    }
                }
            }
            if (!lines.lastElement().endsWith(" [Render thread/INFO]: Stopping!")) {
                Platform.runLater {
                    val alert = Alert(Alert.AlertType.CONFIRMATION)
                    alert.headerText = "Game looks abnormally exited, do you want to read the log?"
                    val optional = alert.showAndWait()
                    if (optional.get()== ButtonType.OK) {
                        val alert = Alert(Alert.AlertType.INFORMATION)
                        alert.headerText = ""
                        alert.graphic = ScrollPane(VBox().apply {
                            for (i in lines) {
                                children.add(Label(i))
                            }
                        })
                        alert.show()
                    }
                }
            }
            logger.info("Game finished")
            whenFinish()
        }.start()
    }
    fun genInitiallyLaunchScript(os: OS, isHigherThan1_13: Boolean, launchOptions: LaunchOptions) : String {
        val ret = if (os==OS.MacOS) {
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
        logger.info("Generating initially launch script: $ret, isHigherThan1_13: $isHigherThan1_13")
        return ret
    }
    fun generateLaunchScript(launchOptions: LaunchOptions, jsonObject: JSONObject, os: OS) : String {
        var isHigherThan1_13 = !jsonObject.containsKey("minecraftArguments")
        val sb = StringBuffer(genInitiallyLaunchScript(os, isHigherThan1_13, launchOptions))
        var inheritsFrom: String? = null
        var inheritsObject: JSONObject? = null
        if (jsonObject.containsKey("inheritsFrom")) {
            logger.info("inheritsFrom isn't null, this version is on a mod API")
            inheritsFrom = jsonObject.getString("inheritsFrom")
            inheritsObject = JSON.parseObject(File("${launchOptions.dir}${separator}versions${separator}$inheritsFrom${separator}$inheritsFrom.json").readText())
        }
        val nativeFolder = File("${launchOptions.dir}${separator}versions${separator}${launchOptions.version}${separator}${launchOptions.version}-natives")
        if (!nativeFolder.exists()) {
            logger.info("native Folder not exists, creating: ${nativeFolder.path}")
            nativeFolder.mkdir()
        } else {
            logger.info("native Folder exists: ${nativeFolder.path}")
        }
        logger.info("Deleting all files in native Folder")
        nativeFolder.listFiles().forEach {
            it.delete()
        }
        logger.info("Generating Minecraft classpath")
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
        logger.info("Launch Script Generated")
        return sb.toString()
    }
}