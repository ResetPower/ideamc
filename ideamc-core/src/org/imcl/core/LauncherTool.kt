package org.imcl.core

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import org.imcl.core.artifacts.ArtifactExtractor
import org.imcl.core.ostool.OS
import java.io.File

object LauncherTool {
    @JvmStatic
    fun genCp(jsonObject: JSONObject, launchOptions: LaunchOptions, os: OS, inheritsFrom: String?, inheritsObject: JSONObject?, nativeFolder: File) : String {
        val cpBuff = StringBuffer("\"")
        if (inheritsFrom!=null&&inheritsObject!=null) {
            cpBuff.append(genCpImpl(jsonObject.getJSONArray("libraries"), launchOptions, nativeFolder, os))
            cpBuff.append(genCpImpl(inheritsObject.getJSONArray("libraries"), launchOptions, nativeFolder, os))
        } else {
            cpBuff.append(genCpImpl(jsonObject.getJSONArray("libraries"), launchOptions, nativeFolder, os))
        }
        if (inheritsFrom!=null) {
            cpBuff.append("${launchOptions.dir}${Launcher.separator}versions${Launcher.separator}$inheritsFrom${Launcher.separator}$inheritsFrom.jar")
        } else {
            cpBuff.append("${launchOptions.dir}${Launcher.separator}versions${Launcher.separator}${launchOptions.version}${Launcher.separator}${launchOptions.version}.jar")
        }
        return cpBuff.toString()
    }

    @JvmStatic
    private fun genCpImpl(libraries: JSONArray, launchOptions: LaunchOptions, nativeFolder: File, os: OS) : String {
        val buff = StringBuffer()
        val iterator = libraries.iterator()
        while (iterator.hasNext()) {
            val jsonObject = JSON.toJSON(iterator.next()) as JSONObject
            if (jsonObject.containsKey("rules")) {
                val rules = jsonObject.getJSONArray("rules")
                val iterator = rules.iterator()
                while (iterator.hasNext()) {
                    val r = iterator.next() as JSONObject
                    if (r.getString("action") == "disallow") {
                        val rOS = r.getString("os")
                        if (os==OS.MacOS&&(rOS=="macos"||rOS=="osx")) {
                        } else if ((os==OS.Windows||os==OS.Windows10)&&rOS=="windows") {
                        } else {
                            genCpImplImpl(launchOptions, nativeFolder, os, buff, jsonObject)
                        }
                    } else {
                        genCpImplImpl(launchOptions, nativeFolder, os, buff, jsonObject)
                    }
                }
            } else {
                genCpImplImpl(launchOptions, nativeFolder, os, buff, jsonObject)
            }
        }
        return buff.toString()
    }
    @JvmStatic
    private fun genCpImplImpl(launchOptions: LaunchOptions, nativeFolder: File, os: OS, buff: StringBuffer, jsonObject: JSONObject) {
        if (jsonObject.containsKey("downloads")) {
            val downloads = jsonObject.getJSONObject("downloads")
            if (downloads.containsKey("classifiers")) {
                if (downloads.containsKey("artifact")) {
                    val artifact = downloads.getJSONObject("artifact")
                    val path = artifact.getString("path")
                    val nativeLibFile =
                        File("${launchOptions.dir}${Launcher.separator}libraries${Launcher.separator}$path")
                    if (nativeLibFile.exists()) {
                        if (os == OS.MacOS) {
                            val macosNative = File(
                                "${launchOptions.dir}${Launcher.separator}libraries${Launcher.separator}${path.toString()
                                    .removeSuffix(".jar") + "-natives-macos.jar"}"
                            )
                            val osxNative = File(
                                "${launchOptions.dir}${Launcher.separator}libraries${Launcher.separator}${path.toString()
                                    .removeSuffix(".jar") + "-natives-osx.jar"}"
                            )
                            if (macosNative.exists()) {
                                val files = ArtifactExtractor.extract(macosNative)
                                for (i in files) {
                                    val f =
                                        File("${nativeFolder.path}${Launcher.separator}${i.first}")
                                    if (!f.exists()) {
                                        f.createNewFile()
                                    }
                                    f.writeBytes(i.second)
                                }
                            } else if (osxNative.exists()) {
                                val files = ArtifactExtractor.extract(osxNative)
                                for (i in files) {
                                    val f =
                                        File("${nativeFolder.path}${Launcher.separator}${i.first}")
                                    if (!f.exists()) {
                                        f.createNewFile()
                                    }
                                    f.writeBytes(i.second)
                                }
                            }
                        } else {
                            val windowsNative = File(
                                "${launchOptions.dir}${Launcher.separator}libraries${Launcher.separator}${path.toString()
                                    .removeSuffix(".jar") + "-natives-windows.jar"}"
                            )
                            if (windowsNative.exists()) {
                                val files = ArtifactExtractor.extract(windowsNative)
                                for (i in files) {
                                    val f =
                                        File("${nativeFolder.path}${Launcher.separator}${i.first}")
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
                    if (os == OS.MacOS) {
                        if (classifiers.containsKey("natives-osx")) {
                            val files = ArtifactExtractor.extract(
                                File(
                                    "${launchOptions.dir}${Launcher.separator}libraries${Launcher.separator}" + classifiers.getJSONObject(
                                        "natives-osx"
                                    ).getString("path")
                                )
                            )
                            for (i in files) {
                                val f = File("${nativeFolder.path}${Launcher.separator}${i.first}")
                                if (!f.exists()) {
                                    f.createNewFile()
                                }
                                f.writeBytes(i.second)
                            }
                        } else if (classifiers.containsKey("natives-macos")) {
                            val files = ArtifactExtractor.extract(
                                File(
                                    "${launchOptions.dir}${Launcher.separator}libraries${Launcher.separator}" + classifiers.getJSONObject(
                                        "natives-macos"
                                    ).getString("path")
                                )
                            )
                            for (i in files) {
                                val f = File("${nativeFolder.path}${Launcher.separator}${i.first}")
                                if (!f.exists()) {
                                    f.createNewFile()
                                }
                                f.writeBytes(i.second)
                            }
                        }
                    } else {
                        if (classifiers.containsKey("natives-windows")) {
                            val files = ArtifactExtractor.extract(
                                File(
                                    "${launchOptions.dir}${Launcher.separator}libraries${Launcher.separator}" + classifiers.getJSONObject(
                                        "natives-windows"
                                    ).getString("path")
                                )
                            )
                            for (i in files) {
                                val f = File("${nativeFolder.path}${Launcher.separator}${i.first}")
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
                val file =
                    File("${launchOptions.dir}${Launcher.separator}libraries${Launcher.separator}$path")
                if (file.exists()) {
                    buff.append(
                        "${launchOptions.dir}${Launcher.separator}libraries${Launcher.separator}${path.replace(
                            "/",
                            Launcher.separator
                        )}${if (os == OS.Windows || os == OS.Windows10) ";" else ":"}"
                    )
                }
            }
        } else {
            val name = jsonObject.getString("name")
            val nameSpl = name.split(":")
            val path = "${nameSpl[0].replace(
                ".",
                "${Launcher.separator}"
            )}${Launcher.separator}${nameSpl[1]}${Launcher.separator}${nameSpl[2]}${Launcher.separator}${nameSpl[1]}-${nameSpl[2]}.jar"
            val file =
                File("${launchOptions.dir}${Launcher.separator}libraries${Launcher.separator}$path")
            if (file.exists()) {
                buff.append("${launchOptions.dir}${Launcher.separator}libraries${Launcher.separator}$path${if (os == OS.Windows || os == OS.Windows10) ";" else ":"}")
            }
        }
    }
}