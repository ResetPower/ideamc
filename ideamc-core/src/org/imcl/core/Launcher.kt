package org.imcl.core

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import javafx.application.Platform
import org.imcl.core.artifacts.ArtifactExtractor
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

object Launcher {
    fun launch(launchOptions: LaunchOptions, whenDone: () -> Unit = {}) {
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

        Thread {
            val p = Runtime.getRuntime().exec(arrayOf("sh", "-c", generateMacOSLaunchScript(launchOptions, jsonObject)))
            val fis: InputStream = p.inputStream
            val isr = InputStreamReader(fis)
            val br = BufferedReader(isr)
            var line: String? = null
            Platform.runLater {
                whenDone()
            }
            while (br.readLine().also { line = it } != null) {
                println(line)
            }
        }.start()
    }
    fun generateMacOSLaunchScript(launchOptions: LaunchOptions, jsonObject: JSONObject) : String {
        var isXStartOnFirstThread = !jsonObject.containsKey("minecraftArguments")
        val sb = StringBuffer("java ${if (isXStartOnFirstThread) "-XstartOnFirstThread" else ""} ${launchOptions.jvmArgs} -Djava.library.path=\"${launchOptions.dir}/versions/${launchOptions.version}/${launchOptions.version}-natives\" ")
        var inheritsFrom: String? = null
        var inheritsObject: JSONObject? = null
        if (jsonObject.containsKey("inheritsFrom")) {
            inheritsFrom = jsonObject.getString("inheritsFrom")
            inheritsObject = JSON.parseObject(File("${launchOptions.dir}/versions/$inheritsFrom/$inheritsFrom.json").readText())
        }
        val nativeFolder = File("${launchOptions.dir}/versions/${launchOptions.version}/${launchOptions.version}-natives")
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
                    if (downloads.containsKey("classifiers")) {
                        val artifact = downloads.getJSONObject("artifact")
                        val path = artifact.get("path")
                        val nativeLibFile = File("${launchOptions.dir}/libraries/$path")
                        if (nativeLibFile.exists()) {
                            val macosNative = File("${launchOptions.dir}/libraries/${path.toString().removeSuffix(".jar")+"-natives-macos.jar"}")
                            val osxNative = File("${launchOptions.dir}/libraries/${path.toString().removeSuffix(".jar")+"-natives-osx.jar"}")
                            if (macosNative.exists()) {
                                val files = ArtifactExtractor.extract(macosNative)
                                for (i in files) {
                                    val f = File("${nativeFolder.path}/${i.first}")
                                    if (!f.exists()) {
                                        f.createNewFile()
                                    }
                                    f.writeBytes(i.second)
                                }
                            } else if (osxNative.exists()) {
                                val files = ArtifactExtractor.extract(osxNative)
                                for (i in files) {
                                    val f = File("${nativeFolder.path}/${i.first}")
                                    if (!f.exists()) {
                                        f.createNewFile()
                                    }
                                    f.writeBytes(i.second)
                                }
                            }
                        }
                    } else {
                        val artifact = downloads.getJSONObject("artifact")
                        val path = artifact.getString("path")
                        val file = File("${launchOptions.dir}/libraries/$path")
                        if (File("${launchOptions.dir}/libraries/$path").exists()) {
                            cpBuff.append("${launchOptions.dir}/libraries/$path:")
                        }
                    }
                } else {
                    val name = jsonObject.getString("name")
                    val nameSpl = name.split(":")
                    val path = "${nameSpl[0].replace(".", "/")}/${nameSpl[1]}/${nameSpl[2]}/${nameSpl[1]}-${nameSpl[2]}.jar"
                    val file = File("${launchOptions.dir}/libraries/$path")
                    if (file.exists()) {
                        cpBuff.append("${launchOptions.dir}/libraries/$path:")
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
                            val nativeLibFile = File("${launchOptions.dir}/libraries/$path")
                            if (nativeLibFile.exists()) {
                                val macosNative = File("${launchOptions.dir}/libraries/${path.toString().removeSuffix(".jar")+"-natives-macos.jar"}")
                                val osxNative = File("${launchOptions.dir}/libraries/${path.toString().removeSuffix(".jar")+"-natives-osx.jar"}")
                                if (macosNative.exists()) {
                                    val files = ArtifactExtractor.extract(macosNative)
                                    for (i in files) {
                                        val f = File("${nativeFolder.path}/${i.first}")
                                        if (!f.exists()) {
                                            f.createNewFile()
                                        }
                                        f.writeBytes(i.second)
                                    }
                                } else if (osxNative.exists()) {
                                    val files = ArtifactExtractor.extract(osxNative)
                                    for (i in files) {
                                        val f = File("${nativeFolder.path}/${i.first}")
                                        if (!f.exists()) {
                                            f.createNewFile()
                                        }
                                        f.writeBytes(i.second)
                                    }
                                }
                            }
                        } else {
                            val classifiers = downloads.getJSONObject("classifiers")
                            if (classifiers.containsKey("natives-osx")) {
                                val files = ArtifactExtractor.extract(File("${launchOptions.dir}/libraries/"+classifiers.getJSONObject("natives-osx").getString("path")))
                                for (i in files) {
                                    val f = File("${nativeFolder.path}/${i.first}")
                                    if (!f.exists()) {
                                        f.createNewFile()
                                    }
                                    f.writeBytes(i.second)
                                }
                            } else if (classifiers.containsKey("natives-macos")) {
                                val files = ArtifactExtractor.extract(File("${launchOptions.dir}/libraries/"+classifiers.getJSONObject("natives-macos").getString("path")))
                                for (i in files) {
                                    val f = File("${nativeFolder.path}/${i.first}")
                                    if (!f.exists()) {
                                        f.createNewFile()
                                    }
                                    f.writeBytes(i.second)
                                }
                            }
                        }
                    } else {
                        val classifiers = downloads.getJSONObject("classifiers")
                        if (classifiers.containsKey("natives-osx")) {
                            val files = ArtifactExtractor.extract(File("${launchOptions.dir}/libraries/"+classifiers.getJSONObject("natives-osx").getString("path")))
                            for (i in files) {
                                val f = File("${nativeFolder.path}/${i.first}")
                                if (!f.exists()) {
                                    f.createNewFile()
                                }
                                f.writeBytes(i.second)
                            }
                        } else if (classifiers.containsKey("natives-macos")) {
                            val files = ArtifactExtractor.extract(File("${launchOptions.dir}/libraries/"+classifiers.getJSONObject("natives-macos").getString("path")))
                            for (i in files) {
                                val f = File("${nativeFolder.path}/${i.first}")
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
                    val file = File("${launchOptions.dir}/libraries/$path")
                    if (file.exists()) {
                        cpBuff.append("${launchOptions.dir}/libraries/$path:")
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
                        val nativeLibFile = File("${launchOptions.dir}/libraries/$path")
                        if (nativeLibFile.exists()) {
                            val macosNative = File("${launchOptions.dir}/libraries/${path.toString().removeSuffix(".jar")+"-natives-macos.jar"}")
                            val osxNative = File("${launchOptions.dir}/libraries/${path.toString().removeSuffix(".jar")+"-natives-osx.jar"}")
                            if (macosNative.exists()) {
                                val files = ArtifactExtractor.extract(macosNative)
                                for (i in files) {
                                    val f = File("${nativeFolder.path}/${i.first}")
                                    if (!f.exists()) {
                                        f.createNewFile()
                                    }
                                    f.writeBytes(i.second)
                                }
                            } else if (osxNative.exists()) {
                                val files = ArtifactExtractor.extract(osxNative)
                                for (i in files) {
                                    val f = File("${nativeFolder.path}/${i.first}")
                                    if (!f.exists()) {
                                        f.createNewFile()
                                    }
                                    f.writeBytes(i.second)
                                }
                            }
                        }
                    } else {
                        val classifiers = downloads.getJSONObject("classifiers")
                        if (classifiers.containsKey("natives-osx")) {
                            val files = ArtifactExtractor.extract(File("${launchOptions.dir}/libraries/"+classifiers.getJSONObject("natives-osx").getString("path")))
                            for (i in files) {
                                val f = File("${nativeFolder.path}/${i.first}")
                                if (!f.exists()) {
                                    f.createNewFile()
                                }
                                f.writeBytes(i.second)
                            }
                        } else if (classifiers.containsKey("natives-macos")) {
                            val files = ArtifactExtractor.extract(File("${launchOptions.dir}/libraries/"+classifiers.getJSONObject("natives-macos").getString("path")))
                            for (i in files) {
                                val f = File("${nativeFolder.path}/${i.first}")
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
                    val file = File("${launchOptions.dir}/libraries/$path")
                    if (file.exists()) {
                        cpBuff.append("${launchOptions.dir}/libraries/$path:")
                    }
                }
            }
        }
        if (isXStartOnFirstThread) {
            cpBuff.append("${launchOptions.dir}/versions/${launchOptions.version}/${launchOptions.version}.jar")
        } else {
            if (inheritsFrom!=null) {
                cpBuff.append("${launchOptions.dir}/versions/$inheritsFrom/$inheritsFrom.jar")
            } else {
                cpBuff.append("${launchOptions.dir}/versions/${launchOptions.version}/${launchOptions.version}.jar")
            }
        }
        sb.append("${cpBuff.removeSuffix(":")}\" ")
        sb.append(jsonObject.get("mainClass"))
        sb.append(" --username ${launchOptions.authenticator.username()} --version ${launchOptions.version} --gameDir \"${launchOptions.gameDirectory ?: launchOptions.dir}\" --assetsDir \"${launchOptions.dir}/assets\" --assetIndex ${if (inheritsObject==null) jsonObject.getJSONObject("assetIndex").get("id") else inheritsObject.getJSONObject("assetIndex").get("id")} --uuid ${launchOptions.authenticator.uuid()} --accessToken ${launchOptions.authenticator.accessToken()} --userType mojang ${launchOptions.minecraftArgs} --versionType release")
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