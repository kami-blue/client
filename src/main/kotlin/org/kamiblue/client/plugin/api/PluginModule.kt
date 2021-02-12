package me.zeroeightsix.kami.plugin.api

import me.zeroeightsix.kami.module.AbstractModule
import me.zeroeightsix.kami.module.Category

abstract class PluginModule(
    final override val pluginMain: Plugin,
    name: String,
    alias: Array<String> = emptyArray(),
    category: Category,
    description: String,
    modulePriority: Int = -1,
    alwaysListening: Boolean = false,
    showOnArray: Boolean = true,
    alwaysEnabled: Boolean = false,
    enabledByDefault: Boolean = false
) : IPluginClass, AbstractModule(
    name,
    alias,
    category,
    description,
    modulePriority,
    alwaysListening,
    showOnArray,
    alwaysEnabled,
    enabledByDefault,
    pluginMain.config
)