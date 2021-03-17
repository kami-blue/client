package org.kamiblue.client.plugin

import org.kamiblue.client.KamiMod
import org.kamiblue.client.gui.mc.KamiGuiPluginError
import org.kamiblue.client.util.Wrapper

internal enum class PluginError {
    HOT_RELOAD,
    DUPLICATE,
    UNSUPPORTED,
    REQUIRED_PLUGIN;

    fun handleError(loader: PluginLoader) {
        val list = latestErrors ?: ArrayList<Pair<PluginLoader, PluginError>>().also { latestErrors = it }

        when (this) {
            HOT_RELOAD -> {
                KamiMod.LOG.error("Plugin $loader cannot be hot reloaded.")
            }
            DUPLICATE -> {
                KamiMod.LOG.error("Duplicate plugin ${loader}.")
            }
            UNSUPPORTED -> {
                KamiMod.LOG.error("Unsupported plugin ${loader}. Required KAMI Blue version: ${loader.info.minApiVersion}")
            }
            REQUIRED_PLUGIN -> {
                KamiMod.LOG.error("Missing required plugin for ${loader}. Required plugins: ${loader.info.requiredPlugins.joinToString()}")
            }
        }

        list.add(loader to this)
    }

    companion object {
        private var latestErrors: ArrayList<Pair<PluginLoader, PluginError>>? = null

        fun displayErrors() {
            val errors = latestErrors
            latestErrors = null

            if (!errors.isNullOrEmpty()) {
                Wrapper.minecraft.displayGuiScreen(KamiGuiPluginError(Wrapper.minecraft.currentScreen, errors))
            }
        }
    }

}