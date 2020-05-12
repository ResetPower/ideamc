package org.imcl.platform

import org.imcl.constraints.Toolkit
import org.imcl.plugin.PluginLoader

object IMCL {
    @JvmStatic
    fun getPluginsFolder() = Toolkit.getPluginsFolder()
    @JvmStatic
    fun getPlugins() = PluginLoader.plugins
    @JvmStatic
    fun toast(msg: String) = Toolkit.toast(msg)
}