package me.zeroeightsix.kami.plugin

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import me.zeroeightsix.kami.KamiMod
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.reflect.Type
import java.net.URLClassLoader
import java.security.MessageDigest
import java.util.jar.JarInputStream

internal class PluginLoader(
    val file: File
) {

    private val url = file.toURI().toURL()
    private val loader = URLClassLoader(arrayOf(url), this.javaClass.classLoader)
    private val mainClassPath: String

    init {
        mainClassPath = readClassPath()
            ?:scanForPath()
                ?: throw ClassNotFoundException("Plugin main class not found in jar ${file.name}")
    }

    private fun readClassPath(): String? {
        return loader.getResourceAsStream("plugin.info")?.use { stream ->
            ByteArrayOutputStream().use { result ->
                stream.copyTo(result)
                result.toString("UTF-8")
            }
        }
    }

    private fun scanForPath(): String? {
        KamiMod.LOG.warn("plugin.info is not found under jar ${file.name}, scanning for main class")
        file.inputStream().use { stream ->
            JarInputStream(stream).use {
                var entry = it.nextJarEntry
                while (entry != null) {
                    if (!entry.isDirectory && entry.name.endsWith(".class")) {
                        val pack = entry.name.removeSuffix(".class")
                            .replace('/', '.')
                        val clazz = Class.forName(pack, false, loader)
                        if (pluginClass.isAssignableFrom(clazz)) return pack
                    }
                    entry = it.nextJarEntry
                }
            }
        }
        return null
    }

    fun verify(): Boolean {
        val bytes = file.inputStream().use {
            it.readBytes()
        }

        val result = StringBuilder().run {
            sha256.digest(bytes).forEach {
                append(String.format("%02x", it))
            }
            toString()
        }

        KamiMod.LOG.info("SHA-256 checksum for ${file.name}: $result")

        return checksumSets.contains(result)
    }

    fun load(): Plugin {
        val clazz = Class.forName(mainClassPath, true, loader)
        return clazz.newInstance() as Plugin
    }

    fun close() {
        loader.close()
    }

    private companion object {
        val pluginClass = Plugin::class.java
        val sha256: MessageDigest = MessageDigest.getInstance("SHA-256")
        val type: Type = object : TypeToken<HashSet<String>>() {}.type
        val checksumSets = runCatching<HashSet<String>> {
            Gson().fromJson(File("verify.json").bufferedReader(), type)
        }.getOrElse { HashSet() }
    }

}