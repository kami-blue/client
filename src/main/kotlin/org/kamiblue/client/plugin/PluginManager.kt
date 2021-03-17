package org.kamiblue.client.plugin

import kotlinx.coroutines.Deferred
import org.kamiblue.client.plugin.api.Plugin
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.kamiblue.client.AsyncLoader
import org.kamiblue.client.KamiMod
import org.kamiblue.commons.collections.NameableSet
import java.io.File
import java.io.FileNotFoundException

internal object PluginManager : AsyncLoader<List<PluginLoader>> {
    override var deferred: Deferred<List<PluginLoader>>? = null

    val loadedPlugins = NameableSet<Plugin>()
    val loadedPluginLoader = NameableSet<PluginLoader>()

    const val pluginPath = "${KamiMod.DIRECTORY}plugins/"

    private val kamiVersion = DefaultArtifactVersion(KamiMod.VERSION_MAJOR)

    override fun preLoad0() = getLoaders()

    override fun load0(input: List<PluginLoader>) {
        loadAll(input)
    }

    fun getLoaders(): List<PluginLoader> {
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

        synchronized(this) {
            validLoaders.forEach(PluginManager::loadWithoutCheck)
        }

        KamiMod.LOG.info("Loaded ${loadedPlugins.size} plugins!")
    }

    private fun checkPluginLoaders(loaders: List<PluginLoader>): List<PluginLoader> {
        val loaderSet = NameableSet<PluginLoader>()
        val invalids = HashSet<PluginLoader>()

        for (loader in loaders) {
            // Hot reload check, the error shouldn't be show when reload in game
            if (KamiMod.ready && !loader.info.hotReload) {
                invalids.add(loader)
            }

            // Unsupported check
            if (DefaultArtifactVersion(loader.info.minApiVersion) > kamiVersion) {
                PluginError.UNSUPPORTED.handleError(loader)
                invalids.add(loader)
            }

            // Duplicate check
            if (loadedPluginLoader.contains(loader)) {
                PluginError.DUPLICATE.handleError(loader)
                invalids.add(loader)
            } else {
                loaderSet[loader.name]?.let {
                    PluginError.DUPLICATE.handleError(loader)
                    invalids.add(loader)
                    PluginError.DUPLICATE.handleError(it)
                    invalids.add(it)
                } ?: run {
                    loaderSet.add(loader)
                }
            }
        }

        for (loader in loaders) {
            // Required plugin check
            if (!loadedPlugins.containsNames(loader.info.requiredPlugins)
                && !loaderSet.containsNames(loader.info.requiredPlugins)) {
                PluginError.REQUIRED_PLUGIN.handleError(loader)
                invalids.add(loader)
            }
        }

        return loaders.filter { !invalids.contains(it) }
    }

    fun load(loader: PluginLoader) {
        synchronized(this) {
            val hotReload = KamiMod.ready && !loader.info.hotReload
            val duplicate = loadedPlugins.containsName(loader.name)
            val unsupported = DefaultArtifactVersion(loader.info.minApiVersion) > kamiVersion
            val missing = !loadedPlugins.containsNames(loader.info.requiredPlugins)

            if (hotReload) PluginError.HOT_RELOAD.handleError(loader)
            if (duplicate) PluginError.DUPLICATE.handleError(loader)
            if (unsupported) PluginError.UNSUPPORTED.handleError(loader)
            if (missing) PluginError.REQUIRED_PLUGIN.handleError(loader)

            if (hotReload || duplicate || unsupported || missing) return

            loadWithoutCheck(loader)
        }
    }

    private fun loadWithoutCheck(loader: PluginLoader) {
        val plugin = synchronized(this) {
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
            loadedPluginLoader.add(loader)
            plugin
        }

        KamiMod.LOG.info("Loaded plugin ${plugin.name}")
    }

    fun unloadAll() {
        loadedPlugins.filter { it.hotReload }.forEach(PluginManager::unloadWithoutCheck)

        KamiMod.LOG.info("Unloaded all plugins!")
    }

    fun unload(plugin: Plugin) {
        if (loadedPlugins.any { it.requiredPlugins.contains(plugin.name) }) {
            throw IllegalArgumentException("Plugin $plugin is required by another plugin!")
        }

        unloadWithoutCheck(plugin)
    }

    private fun unloadWithoutCheck(plugin: Plugin) {
        if (!plugin.hotReload) {
            throw IllegalArgumentException("Plugin $plugin cannot be hot reloaded!")
        }

        synchronized(this) {
            if (loadedPlugins.remove(plugin)) {
                plugin.unregister()
                plugin.onUnload()
                loadedPluginLoader[plugin.name]?.let {
                    it.close()
                    loadedPluginLoader.remove(it)
                }
            }
        }

        KamiMod.LOG.info("Unloaded plugin ${plugin.name}")
    }

}