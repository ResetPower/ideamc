package org.imcl.core

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import javafx.application.Platform
import org.imcl.core.artifacts.ArtifactExtractor
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
            Log.e("Version not found")
            return
        }
        val json = File("${dir.path}${separator}${launchOptions.version}.json")
        if (!json.exists()) {
            Log.e("JSON not found")
            return
        }
        val jsonObject = JSON.parseObject(json.readText())

        Log.i("Looking for OS!")
        val os = OSTool.getOS()
        Log.i("Found OS! $os!")

        if (os==OS.Linux) {
            whenDone()
            Log.e("Linux is not supported!")
            return
        } else if (os==OS.Unknown) {
            whenDone()
            Log.e("Unknown OS is not supported!")
            return
        }
        Thread {
            Log.i("Generating $os Launch Script!")
            val cmd = if (jsonObject.containsKey("patches")) {
                generateLaunchScript(launchOptions, jsonObject.getJSONArray("patches").getJSONObject(0), os)
            } else {
                generateLaunchScript(launchOptions, jsonObject, os)
            }
            Log.i("Generated Launch Script\n$cmd")
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
            Platform.runLater {
                whenDone()
            }
            Log.i("Launching Minecraft!")
            while (br.readLine().also { line = it } != null) {
                println(line)
            }
        }.start()
    }
    fun genInitiallyLaunchScript(os: OS, isHigherThan1_13: Boolean, launchOptions: LaunchOptions) : String {
        return if (os==OS.MacOS) {
            "\"${launchOptions.javaPath}\" ${if (isHigherThan1_13) "-XstartOnFirstThread" else ""} ${launchOptions.jvmArgs} -Djava.library.path=\"${launchOptions.dir}${separator}versions${separator}${launchOptions.version}${separator}${launchOptions.version}-natives\" "
        } else if (os==OS.Windows10) {
            "\"${launchOptions.javaPath}\" -XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump \"-Dos.name=Windows 10\" -Dos.version=10.0 ${launchOptions.jvmArgs} \"-Djava.library.path=${launchOptions.dir}${separator}versions${separator}${launchOptions.version}${separator}${launchOptions.version}-natives\" "
        } else if (os==OS.Windows) {
            "\"${launchOptions.javaPath}\" -XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump ${launchOptions.jvmArgs} \"-Djava.library.path=${launchOptions.dir}${separator}versions${separator}${launchOptions.version}${separator}${launchOptions.version}-natives\" "
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
        val cpBuff = StringBuffer("\"")
        if (inheritsFrom!=null&&inheritsObject!=null) {
            val modLoaderLibraries = jsonObject.getJSONArray("libraries")
            val modLoaderIterator = modLoaderLibraries.iterator()
            while (modLoaderIterator.hasNext()) {
                val jsonObject = JSON.toJSON(modLoaderIterator.next()) as JSONObject
                if (jsonObject.containsKey("downloads")) {
                    val downloads = jsonObject.getJSONObject("downloads")
                    if (downloads.containsKey("artifact")) {
                        if (downloads.containsKey("classifiers")) {
                            val artifact = downloads.getJSONObject("artifact")
                            val path = artifact.get("path")
                            val nativeLibFile = File("${launchOptions.dir}${separator}libraries${separator}$path")
                            if (nativeLibFile.exists()) {
                                if (os==OS.MacOS) {
                                    val macosNative = File("${launchOptions.dir}${separator}libraries${separator}${path.toString().removeSuffix(".jar")+"-natives-macos.jar"}")
                                    val osxNative = File("${launchOptions.dir}${separator}libraries${separator}${path.toString().removeSuffix(".jar")+"-natives-osx.jar"}")
                                    if (macosNative.exists()) {
                                        val files = ArtifactExtractor.extract(macosNative)
                                        for (i in files) {
                                            val f = File("${nativeFolder.path}${separator}${i.first}")
                                            if (!f.exists()) {
                                                f.createNewFile()
                                            }
                                            f.writeBytes(i.second)
                                        }
                                    } else if (osxNative.exists()) {
                                        val files = ArtifactExtractor.extract(osxNative)
                                        for (i in files) {
                                            val f = File("${nativeFolder.path}${separator}${i.first}")
                                            if (!f.exists()) {
                                                f.createNewFile()
                                            }
                                            f.writeBytes(i.second)
                                        }
                                    }
                                } else {
                                    val windowsNative = File("${launchOptions.dir}${separator}libraries${separator}${path.toString().removeSuffix(".jar")+"-natives-windows.jar"}")
                                    if (windowsNative.exists()) {
                                        val files = ArtifactExtractor.extract(windowsNative)
                                        for (i in files) {
                                            val f = File("${nativeFolder.path}${separator}${i.first}")
                                            if (!f.exists()) {
                                                f.createNewFile()
                                            }
                                            f.writeBytes(i.second)
                                        }
                                    }
                                }
                            }
                        } else {
                            val artifact = downloads.getJSONObject("artifact")
                            val path = artifact.getString("path")
                            val file = File("${launchOptions.dir}${separator}libraries${separator}$path")
                            if (file.exists()) {
                                cpBuff.append("${launchOptions.dir}${separator}libraries${separator}${path.replace("/", separator)}${if (os==OS.Windows||os==OS.Windows10) ";" else ":"}")
                            }
                        }
                    } else {
                        val classifiers = downloads.getJSONObject("classifiers")
                        if (os==OS.MacOS) {
                            if (classifiers.containsKey("natives-osx")) {
                                val files = ArtifactExtractor.extract(File("${launchOptions.dir}${separator}libraries${separator}"+classifiers.getJSONObject("natives-osx").getString("path")))
                                for (i in files) {
                                    val f = File("${nativeFolder.path}${separator}${i.first}")
                                    if (!f.exists()) {
                                        f.createNewFile()
                                    }
                                    f.writeBytes(i.second)
                                }
                            } else if (classifiers.containsKey("natives-macos")) {
                                val files = ArtifactExtractor.extract(File("${launchOptions.dir}${separator}libraries${separator}"+classifiers.getJSONObject("natives-macos").getString("path")))
                                for (i in files) {
                                    val f = File("${nativeFolder.path}${separator}${i.first}")
                                    if (!f.exists()) {
                                        f.createNewFile()
                                    }
                                    f.writeBytes(i.second)
                                }
                            }
                        } else {
                            if (classifiers.containsKey("natives-windows")) {
                                val files = ArtifactExtractor.extract(File("${launchOptions.dir}${separator}libraries${separator}"+classifiers.getJSONObject("natives-windows").getString("path")))
                                for (i in files) {
                                    val f = File("${nativeFolder.path}${separator}${i.first}")
                                    if (!f.exists()) {
                                        f.createNewFile()
                                    }
                                    f.writeBytes(i.second)
                                }
                            }
                        }
                    }
                } else {
                    val name = jsonObject.getString("name")
                    val nameSpl = name.split(":")
                    val path = "${nameSpl[0].replace(".", "${separator}")}${separator}${nameSpl[1]}${separator}${nameSpl[2]}${separator}${nameSpl[1]}-${nameSpl[2]}.jar"
                    val file = File("${launchOptions.dir}${separator}libraries${separator}$path")
                    if (file.exists()) {
                        cpBuff.append("${launchOptions.dir}${separator}libraries${separator}$path${if (os==OS.Windows||os==OS.Windows10) ";" else ":"}")
                    }
                }
            }
            val libraries = inheritsObject.getJSONArray("libraries")
            val iterator = libraries.iterator()
            while (iterator.hasNext()) {
                val jsonObject = JSON.toJSON(iterator.next()) as JSONObject
                val downloads = jsonObject.getJSONObject("downloads")
                if (downloads.containsKey("classifiers")) {
                    if (downloads.containsKey("artifact")) {
                        if (downloads.containsKey("artifact")) {
                            val artifact = downloads.getJSONObject("artifact")
                            val path = artifact.getString("path")
                            val nativeLibFile = File("${launchOptions.dir}${separator}libraries${separator}$path")
                            if (nativeLibFile.exists()) {
                                val macosNative = File("${launchOptions.dir}${separator}libraries${separator}${path.toString().removeSuffix(".jar")+"-natives-macos.jar"}")
                                val osxNative = File("${launchOptions.dir}${separator}libraries${separator}${path.toString().removeSuffix(".jar")+"-natives-osx.jar"}")
                                if (macosNative.exists()) {
                                    val files = ArtifactExtractor.extract(macosNative)
                                    for (i in files) {
                                        val f = File("${nativeFolder.path}${separator}${i.first}")
                                        if (!f.exists()) {
                                            f.createNewFile()
                                        }
                                        f.writeBytes(i.second)
                                    }
                                } else if (osxNative.exists()) {
                                    val files = ArtifactExtractor.extract(osxNative)
                                    for (i in files) {
                                        val f = File("${nativeFolder.path}${separator}${i.first}")
                                        if (!f.exists()) {
                                            f.createNewFile()
                                        }
                                        f.writeBytes(i.second)
                                    }
                                }
                            }
                        } else {
                            val classifiers = downloads.getJSONObject("classifiers")
                            if (os==OS.MacOS) {
                                if (classifiers.containsKey("natives-osx")) {
                                    val files = ArtifactExtractor.extract(File("${launchOptions.dir}${separator}libraries${separator}"+classifiers.getJSONObject("natives-osx").getString("path")))
                                    for (i in files) {
                                        val f = File("${nativeFolder.path}${separator}${i.first}")
                                        if (!f.exists()) {
                                            f.createNewFile()
                                        }
                                        f.writeBytes(i.second)
                                    }
                                } else if (classifiers.containsKey("natives-macos")) {
                                    val files = ArtifactExtractor.extract(File("${launchOptions.dir}${separator}libraries${separator}"+classifiers.getJSONObject("natives-macos").getString("path")))
                                    for (i in files) {
                                        val f = File("${nativeFolder.path}${separator}${i.first}")
                                        if (!f.exists()) {
                                            f.createNewFile()
                                        }
                                        f.writeBytes(i.second)
                                    }
                                }
                            } else {
                                if (classifiers.containsKey("natives-windows")) {
                                    val files = ArtifactExtractor.extract(File("${launchOptions.dir}${separator}libraries${separator}"+classifiers.getJSONObject("natives-windows").getString("path")))
                                    for (i in files) {
                                        val f = File("${nativeFolder.path}${separator}${i.first}")
                                        if (!f.exists()) {
                                            f.createNewFile()
                                        }
                                        f.writeBytes(i.second)
                                    }
                                }
                            }
                        }
                    } else {
                        val classifiers = downloads.getJSONObject("classifiers")
                        if (os==OS.MacOS) {
                            if (classifiers.containsKey("natives-osx")) {
                                val files = ArtifactExtractor.extract(File("${launchOptions.dir}${separator}libraries${separator}"+classifiers.getJSONObject("natives-osx").getString("path")))
                                for (i in files) {
                                    val f = File("${nativeFolder.path}${separator}${i.first}")
                                    if (!f.exists()) {
                                        f.createNewFile()
                                    }
                                    f.writeBytes(i.second)
                                }
                            } else if (classifiers.containsKey("natives-macos")) {
                                val files = ArtifactExtractor.extract(File("${launchOptions.dir}${separator}libraries${separator}"+classifiers.getJSONObject("natives-macos").getString("path")))
                                for (i in files) {
                                    val f = File("${nativeFolder.path}${separator}${i.first}")
                                    if (!f.exists()) {
                                        f.createNewFile()
                                    }
                                    f.writeBytes(i.second)
                                }
                            }
                        } else {
                            if (classifiers.containsKey("natives-windows")) {
                                val files = ArtifactExtractor.extract(File("${launchOptions.dir}${separator}libraries${separator}"+classifiers.getJSONObject("natives-windows").getString("path")))
                                for (i in files) {
                                    val f = File("${nativeFolder.path}${separator}${i.first}")
                                    if (!f.exists()) {
                                        f.createNewFile()
                                    }
                                    f.writeBytes(i.second)
                                }
                            }
                        }
                    }
                } else {
                    val artifact = downloads.getJSONObject("artifact")
                    val path = artifact.getString("path")
                    val file = File("${launchOptions.dir}${separator}libraries${separator}$path")
                    if (file.exists()) {
                        cpBuff.append("${launchOptions.dir}${separator}libraries${separator}${path.replace("/", separator)}${if (os==OS.Windows||os==OS.Windows10) ";" else ":"}")
                    }
                }
            }
        } else {
            val libraries = jsonObject.getJSONArray("libraries")
            val iterator = libraries.iterator()
            while (iterator.hasNext()) {
                val jsonObject = JSON.toJSON(iterator.next()) as JSONObject
                val downloads = jsonObject.getJSONObject("downloads")
                if (downloads.containsKey("classifiers")) {
                    if (downloads.containsKey("artifact")) {
                        val artifact = downloads.getJSONObject("artifact")
                        val path = artifact.getString("path")
                        val nativeLibFile = File("${launchOptions.dir}${separator}libraries${separator}$path")
                        if (nativeLibFile.exists()) {
                            if (os==OS.MacOS) {
                                val macosNative = File("${launchOptions.dir}${separator}libraries${separator}${path.toString().removeSuffix(".jar")+"-natives-macos.jar"}")
                                val osxNative = File("${launchOptions.dir}${separator}libraries${separator}${path.toString().removeSuffix(".jar")+"-natives-osx.jar"}")
                                if (macosNative.exists()) {
                                    val files = ArtifactExtractor.extract(macosNative)
                                    for (i in files) {
                                        val f = File("${nativeFolder.path}${separator}${i.first}")
                                        if (!f.exists()) {
                                            f.createNewFile()
                                        }
                                        f.writeBytes(i.second)
                                    }
                                } else if (osxNative.exists()) {
                                    val files = ArtifactExtractor.extract(osxNative)
                                    for (i in files) {
                                        val f = File("${nativeFolder.path}${separator}${i.first}")
                                        if (!f.exists()) {
                                            f.createNewFile()
                                        }
                                        f.writeBytes(i.second)
                                    }
                                }
                            } else {
                                val windowsNative = File("${launchOptions.dir}${separator}libraries${separator}${path.toString().removeSuffix(".jar")+"-natives-windows.jar"}")
                                if (windowsNative.exists()) {
                                    val files = ArtifactExtractor.extract(windowsNative)
                                    for (i in files) {
                                        val f = File("${nativeFolder.path}${separator}${i.first}")
                                        if (!f.exists()) {
                                            f.createNewFile()
                                        }
                                        f.writeBytes(i.second)
                                    }
                                }
                            }
                        }
                    } else {
                        val classifiers = downloads.getJSONObject("classifiers")
                        if (os==OS.MacOS) {
                            if (classifiers.containsKey("natives-osx")) {
                                val files = ArtifactExtractor.extract(File("${launchOptions.dir}${separator}libraries${separator}"+classifiers.getJSONObject("natives-osx").getString("path")))
                                for (i in files) {
                                    val f = File("${nativeFolder.path}${separator}${i.first}")
                                    if (!f.exists()) {
                                        f.createNewFile()
                                    }
                                    f.writeBytes(i.second)
                                }
                            } else if (classifiers.containsKey("natives-macos")) {
                                val files = ArtifactExtractor.extract(File("${launchOptions.dir}${separator}libraries${separator}"+classifiers.getJSONObject("natives-macos").getString("path")))
                                for (i in files) {
                                    val f = File("${nativeFolder.path}${separator}${i.first}")
                                    if (!f.exists()) {
                                        f.createNewFile()
                                    }
                                    f.writeBytes(i.second)
                                }
                            }
                        } else {
                            if (classifiers.containsKey("natives-windows")) {
                                val files = ArtifactExtractor.extract(File("${launchOptions.dir}${separator}libraries${separator}"+classifiers.getJSONObject("natives-windows").getString("path")))
                                for (i in files) {
                                    val f = File("${nativeFolder.path}${separator}${i.first}")
                                    if (!f.exists()) {
                                        f.createNewFile()
                                    }
                                    f.writeBytes(i.second)
                                }
                            }
                        }
                    }
                } else {
                    val artifact = downloads.getJSONObject("artifact")
                    val path = artifact.getString("path")
                    val file = File("${launchOptions.dir}${separator}libraries${separator}$path")
                    if (file.exists()) {
                        cpBuff.append("${launchOptions.dir}${separator}libraries${separator}${path.replace("/", separator)}${if (os==OS.Windows||os==OS.Windows10) ";" else ":"}")
                    }
                }
            }
        }
        if (isHigherThan1_13) {
            cpBuff.append("${launchOptions.dir}${separator}versions${separator}${launchOptions.version}${separator}${launchOptions.version}.jar")
        } else {
            if (inheritsFrom!=null) {
                cpBuff.append("${launchOptions.dir}${separator}versions${separator}$inheritsFrom${separator}$inheritsFrom.jar")
            } else {
                cpBuff.append("${launchOptions.dir}${separator}versions${separator}${launchOptions.version}${separator}${launchOptions.version}.jar")
            }
        }
        sb.append("${cpBuff.removeSuffix(if (os==OS.Windows||os==OS.Windows10) ";" else ":")}\" ")
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