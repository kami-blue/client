package me.zeroeightsix.kami.module

import me.zeroeightsix.kami.setting.ModuleConfig

internal abstract class Module(
    name: String,
    alias: Array<String> = emptyArray(),
    category: Category,
    description: String,
    modulePriority: Int = -1,
    alwaysListening: Boolean = false,
    showOnArray: Boolean = true,
    alwaysEnabled: Boolean = false,
    enabledByDefault: Boolean = false
) : AbstractModule(
    name,
    alias,
    category,
    description,
    modulePriority,
    alwaysListening,
    showOnArray,
    alwaysEnabled,
    enabledByDefault,
    ModuleConfig
)