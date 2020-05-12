package org.imcl.plugin

import org.imcl.constraints.Toolkit
import org.imcl.constraints.logger
import org.imcl.exceptions.PluginLoaderException
import org.imcl.platform.Plugin
import org.imcl.platform.PluginInfo
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import java.util.jar.JarFile

object PluginLoader {
    @JvmStatic
    val plugins = Vector<Plugin>()
    @JvmStatic
    fun preLoad() {
        val pluginsFolder = Toolkit.getPluginsFolder()
        logger.info("Loading plugins in ${pluginsFolder.absolutePath} ...")
        for (i in pluginsFolder.listFiles()) {
            if (i.isFile&&i.name.toLowerCase().endsWith(".jar")) {
                logger.info("Loading plugin file: ${i.name}")
                load(i)
            }
        }
        logger.info("All plugins loaded")
    }
    @JvmStatic
    fun load(file: File) {
        val urlClassLoader = URLClassLoader(arrayOf(URL("jar:file://${file.absolutePath}!/")))
        val jarFile = JarFile(file)
        logger.info("Looking for plugin.yml")
        val content = try {
            String(jarFile.getInputStream(jarFile.getJarEntry("plugin.yml")).readBytes())
        } catch (e: Exception) {
            logger.error("Unable to load plugin.yml in ${file.name}")
            throw PluginLoaderException("Error occurred in loading plugin.yml", e)
        }
        logger.info("Successfully found plugin.yml, parsing...")
        try {
            val yaml = Yaml().loadAs(content, Map::class.java)
            val name = yaml["name"]
            logger.info("Plugin Name: $name")
            val version = yaml["version"]
            logger.info("$name's version: $version")
            val description = yaml["description"]
            logger.info("$name's description: $description")
            val main = yaml["main"]
            logger.info("$name's main: $main")
            val plugin = Class.forName(main.toString(), true, urlClassLoader).newInstance() as Plugin
            plugin.info = PluginInfo(name.toString(), version.toString(), description.toString(), main.toString())
            plugin.onLoad()
            plugins.add(plugin)
        } catch (e: Exception) {
            logger.error("Unable to load plugin file: ${file.name}")
            throw PluginLoaderException("Error occurred in loading plugin file: ${file.name}", e)
        }
    }
}