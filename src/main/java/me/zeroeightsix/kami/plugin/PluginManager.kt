package me.zeroeightsix.kami.plugin

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.util.mainScope
import org.kamiblue.commons.collections.NameableSet
import java.io.File

internal object PluginManager {

    val loadedPlugins = NameableSet<Plugin>()
    val pluginLoaderMap = HashMap<Plugin, PluginLoader>()
    val pluginPath = "${KamiMod.DIRECTORY}plugins/"

    private val lockObject = Any()
    private lateinit var deferred: Deferred<List<PluginLoader>>

    @JvmStatic
    fun preInit() {
        deferred = mainScope.async {
            preLoad()
        }
    }

    @JvmStatic
    fun init() {
        runBlocking {
            loadAll(deferred.await())
        }
    }

    fun preLoad(): List<PluginLoader> {
        // Create directory if not exist
        val dir = File(pluginPath)
        if (!dir.exists()) dir.mkdir()

        val files = dir.listFiles() ?: return emptyList()
        val jarFiles = files.filter { it.extension.equals("jar", true) }
        val plugins = ArrayList<PluginLoader>()

        jarFiles.forEach {
            try {
                val loader = PluginLoader(it)
                loader.verify()
                plugins.add(loader)
            } catch (e: ClassNotFoundException) {
                KamiMod.LOG.info("${it.name} is not a valid plugin, skipping")
            } catch (e: Exception) {
                KamiMod.LOG.error("Failed to prepare plugin ${it.name}", e)
            }
        }

        return plugins
    }

    fun loadAll(plugins: List<PluginLoader>) {
        synchronized(lockObject) {
            plugins.forEach {
                val plugin = it.load()
                plugin.onLoad()
                plugin.register()
                loadedPlugins.add(plugin)
                pluginLoaderMap[plugin] = it
            }
        }
        KamiMod.LOG.info("Loaded ${loadedPlugins.size} plugins!")
    }

    fun load(loader: PluginLoader) {
        val plugin = synchronized(lockObject) {
            val plugin = loader.load()
            plugin.onLoad()
            plugin.register()
            loadedPlugins.add(plugin)
            pluginLoaderMap[plugin] = loader
            plugin
        }
        KamiMod.LOG.info("Loaded plugin ${plugin.name}")
    }

    fun unloadAll() {
        synchronized(lockObject) {
            loadedPlugins.forEach {
                it.unregister()
                it.onUnload()
                pluginLoaderMap[it]?.close()
            }
            loadedPlugins.clear()
        }
        KamiMod.LOG.info("Unloaded all plugins!")
    }

    fun unload(plugin: Plugin) {
        synchronized(lockObject) {
            if (loadedPlugins.remove(plugin)) {
                plugin.unregister()
                plugin.onUnload()
                pluginLoaderMap[plugin]?.close()
            }
        }
    }

}