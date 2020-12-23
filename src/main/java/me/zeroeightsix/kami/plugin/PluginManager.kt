package me.zeroeightsix.kami.plugin

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.mc.KamiGuiPluginError
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.mainScope
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.kamiblue.commons.collections.NameableSet
import java.io.File

internal object PluginManager {

    val loadedPlugins = NameableSet<Plugin>()
    val pluginLoaderMap = HashMap<Plugin, PluginLoader>()
    private var latestErrors: ArrayList<Pair<Plugin, PluginError>>? = null

    const val pluginPath = "${KamiMod.DIRECTORY}plugins/"

    private val lockObject = Any()
    private lateinit var deferred: Deferred<List<PluginLoader>>

    fun preInit() {
        deferred = mainScope.async {
            preLoad()
        }
    }

    fun init() {
        runBlocking {
            loadAll(deferred.await())
        }
    }

    fun preLoad(): List<PluginLoader> {
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
                KamiMod.LOG.info("${it.name} is not a valid plugin. Skipping...")
            } catch (e: Exception) {
                KamiMod.LOG.error("Failed to pre load plugin ${it.name}", e)
            }
        }

        return plugins
    }

    fun loadAll(plugins: List<PluginLoader>) {
        synchronized(lockObject) {
            plugins.forEach {
                load(it)
            }
        }

        KamiMod.LOG.info("Loaded ${loadedPlugins.size} plugins!")
    }

    fun load(loader: PluginLoader) {
        val plugin = synchronized(lockObject) {
            val plugin = loader.load()
            val list = latestErrors ?: ArrayList<Pair<Plugin, PluginError>>().also { latestErrors = it }

            if (DefaultArtifactVersion(plugin.minKamiVersion) > DefaultArtifactVersion(KamiMod.VERSION_MAJOR)) {
                KamiMod.LOG.error("The plugin ${plugin.name} is unsupported by this version of KAMI Blue (minimum version: ${plugin.minKamiVersion} current version: ${KamiMod.VERSION_MAJOR}). This plugin will not be loaded.")
                list.add(plugin to PluginError.UNSUPPORTED_KAMI)
                return
            }
            if (!loadedPlugins.containsNames(plugin.requiredPlugins.toList())) {
                KamiMod.LOG.error("The plugin ${plugin.name} is missing a required plugin dependency! This plugin will not be loaded. Make sure that these plugins are installed: ${plugin.requiredPlugins.joinToString()}")
                list.add(plugin to PluginError.REQUIRED_PLUGIN)
                return
            }

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

        KamiMod.LOG.info("Unloaded plugin ${plugin.name}")
    }

    fun displayErrors() {
        latestErrors?.takeIf { it.isNotEmpty() }?.let {
            Wrapper.minecraft.displayGuiScreen(KamiGuiPluginError(Wrapper.minecraft.currentScreen, it))
        }
        latestErrors = null
    }

}