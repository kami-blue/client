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
    private var latestErrors: ArrayList<Pair<PluginInfo, PluginError>>? = null

    const val pluginPath = "${KamiMod.DIRECTORY}plugins/"

    private val lockObject = Any()
    private val kamiVersion = DefaultArtifactVersion(KamiMod.VERSION_MAJOR)
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
            val list = latestErrors ?: ArrayList<Pair<PluginInfo, PluginError>>().also { latestErrors = it }

            val unsupported = DefaultArtifactVersion(loader.info.kamiVersion) > kamiVersion
            val missing = !loadedPlugins.containsNames(loader.info.requiredPlugins.toList())

            if (unsupported) {
                KamiMod.LOG.error("Unsupported plugin ${loader.info.name}. Required version: ${loader.info.kamiVersion}")
                list.add(loader.info to PluginError.UNSUPPORTED_KAMI)
            }
            if (missing) {
                KamiMod.LOG.error("Missing required plugin for ${loader.info.name}. Required plugins: ${loader.info.requiredPlugins.joinToString()}")
                list.add(loader.info to PluginError.REQUIRED_PLUGIN)
            }

            if (unsupported || missing) return

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

        KamiMod.LOG.info("Unloaded plugin ${plugin.name}")
    }

    fun displayErrors() {
        latestErrors?.takeIf { it.isNotEmpty() }?.let {
            Wrapper.minecraft.displayGuiScreen(KamiGuiPluginError(Wrapper.minecraft.currentScreen, it))
        }
        latestErrors = null
    }

}