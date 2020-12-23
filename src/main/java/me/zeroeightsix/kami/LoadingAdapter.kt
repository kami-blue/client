package me.zeroeightsix.kami

import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.manager.ManagerLoader
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.plugin.PluginManager

internal fun preInit() {
    ModuleManager.preLoad()
    ManagerLoader.preLoad()
    PluginManager.preInit()
}

internal fun init() {
    ModuleManager.load()
    ManagerLoader.load()
    CommandManager.init()
    PluginManager.init()
}