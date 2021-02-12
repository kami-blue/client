package me.zeroeightsix.kami.plugin.api

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.setting.settings.SettingRegister

abstract class PluginHudElement(
    final override val pluginMain: Plugin,
    name: String,
    alias: Array<String> = emptyArray(),
    category: Category,
    description: String,
    alwaysListening: Boolean = false,
    enabledByDefault: Boolean = false
) : HudElement(name, alias, category, description, alwaysListening, enabledByDefault),
    IPluginClass,
    SettingRegister<IPluginClass> by pluginMain.config
