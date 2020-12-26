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
import java.io.FileNotFoundException

internal object PluginManager {

    val loadedPlugins = NameableSet<Plugin>()
    val pluginLoaderMap = HashMap<Plugin, PluginLoader>()
    private var latestErrors: ArrayList<Pair<PluginLoader, PluginError>>? = null

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
            } catch (e: FileNotFoundException) {
                KamiMod.LOG.info("${it.name} is not a valid plugin. Skipping...")
            } catch (e: PluginInfoMissingException) {
                KamiMod.LOG.warn("${it.name} is missing a required info ${e.infoName}. Skipping...", e)
            } catch (e: Exception) {
                KamiMod.LOG.error("Failed to pre load plugin ${it.name}", e)
            }
        }

        return plugins
    }

    fun loadAll(loaders: List<PluginLoader>) {
        val validLoaders = checkPluginLoaders(loaders)

        synchronized(lockObject) {
            validLoaders.forEach(::loadWithoutCheck)
        }

        KamiMod.LOG.info("Loaded ${loadedPlugins.size} plugins!")
    }

    private fun checkPluginLoaders(loaders: List<PluginLoader>): List<PluginLoader> {
        val loaderSet = NameableSet<PluginLoader>()
        val invalids = HashSet<PluginLoader>()

        for (loader in loaders) {
            // Hot reload check, the error shouldn't be show when reload in game
            if (KamiMod.isReady() && !loader.info.hotReload) {
                invalids.add(loader)
            }

            // Unsupported check
            if (DefaultArtifactVersion(loader.info.kamiVersion) > kamiVersion) {
                handleError(loader, PluginError.UNSUPPORTED)
                invalids.add(loader)
            }

            // Duplicate check
            loaderSet[loader.name]?.let {
                handleError(loader, PluginError.DUPLICATE)
                invalids.add(loader)
                handleError(it, PluginError.DUPLICATE)
                invalids.add(it)
            } ?: run {
                loaderSet.add(loader)
            }
        }

        for (loader in loaders) {
            // Required plugin check
            if (!loadedPlugins.containsNames(loader.info.requiredPlugins)
                && !loaderSet.containsNames(loader.info.requiredPlugins)) {
                handleError(loader, PluginError.REQUIRED_PLUGIN)
                invalids.add(loader)
            }
        }

        return loaders.filter { !invalids.contains(it) }
    }

    fun load(loader: PluginLoader) {
        synchronized(lockObject) {
            val hotReload = KamiMod.isReady() && !loader.info.hotReload
            val duplicate = loadedPlugins.containsName(loader.name)
            val unsupported = DefaultArtifactVersion(loader.info.kamiVersion) > kamiVersion
            val missing = !loadedPlugins.containsNames(loader.info.requiredPlugins)

            if (hotReload) handleError(loader, PluginError.HOT_RELOAD)
            if (duplicate) handleError(loader, PluginError.DUPLICATE)
            if (unsupported) handleError(loader, PluginError.UNSUPPORTED)
            if (missing) handleError(loader, PluginError.REQUIRED_PLUGIN)

            if (hotReload || duplicate || unsupported || missing) return

            loadWithoutCheck(loader)
        }
    }

    private fun handleError(loader: PluginLoader, error: PluginError) {
        val list = latestErrors ?: ArrayList<Pair<PluginLoader, PluginError>>().also { latestErrors = it }

        when (error) {
            PluginError.HOT_RELOAD -> {
                KamiMod.LOG.error("Plugin $loader cannot be hot reloaded.")
            }
            PluginError.DUPLICATE -> {
                KamiMod.LOG.error("Duplicate plugin ${loader}.")
            }
            PluginError.UNSUPPORTED -> {
                KamiMod.LOG.error("Unsupported plugin ${loader}. Required version: ${loader.info.kamiVersion}")
            }
            PluginError.REQUIRED_PLUGIN -> {
                KamiMod.LOG.error("Missing required plugin for ${loader}. Required plugins: ${loader.info.requiredPlugins.joinToString()}")
            }
        }

        list.add(loader to error)
    }

    private fun loadWithoutCheck(loader: PluginLoader) {
        val plugin = synchronized(lockObject) {
            val plugin = runCatching(loader::load).getOrElse {
                when (it) {
                    is ClassNotFoundException -> {
                        KamiMod.LOG.warn("Main class not found in plugin $loader", it)
                    }
                    is IllegalAccessException -> {
                        KamiMod.LOG.warn(it.message, it)
                    }
                    else -> {
                        KamiMod.LOG.error("Failed to load plugin $loader", it)
                    }
                }
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
        loadedPlugins.filter { it.hotReload }.forEach(::unload)

        KamiMod.LOG.info("Unloaded all plugins!")
    }

    fun unload(plugin: Plugin) {
        if (!plugin.hotReload) throw IllegalAccessException("Plugin $plugin cannot be hot reloaded!")

        unloadWithoutCheck(plugin)
    }

    private fun unloadWithoutCheck(plugin: Plugin) {
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
        val errors = latestErrors
        latestErrors = null

        if (!errors.isNullOrEmpty()) {
            Wrapper.minecraft.displayGuiScreen(KamiGuiPluginError(Wrapper.minecraft.currentScreen, errors))
        }
    }

}