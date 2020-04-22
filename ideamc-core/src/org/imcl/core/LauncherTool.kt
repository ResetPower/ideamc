package org.imcl.core

import org.imcl.core.ostool.OS
import java.lang.Exception

object LauncherTool {
    @JvmStatic
    fun genInitiallyLaunchScript(os: OS, isHigherThan1_13: Boolean, launchOptions: LaunchOptions) : String {
        return if (os==OS.MacOS) {
            "java ${if (isHigherThan1_13) "-XstartOnFirstThread" else ""} ${launchOptions.jvmArgs} -Djava.library.path=\"${launchOptions.dir}/versions/${launchOptions.version}/${launchOptions.version}-natives\" "
        } else if (os==OS.Windows10) {
            "java ${if (isHigherThan1_13) "-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump -Dos.name=\"Windows 10\" -Dos.version=10.0" else ""} ${launchOptions.jvmArgs} -Djava.library.path=\"${launchOptions.dir}/versions/${launchOptions.version}/${launchOptions.version}-natives\" "
        } else if (os==OS.Windows) {
            "java ${if (isHigherThan1_13) "-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump" else ""} ${launchOptions.jvmArgs} -Djava.library.path=\"${launchOptions.dir}/versions/${launchOptions.version}/${launchOptions.version}-natives\" "
        } else {
            throw Exception("Not supported OS: $os")
        }
    }
}