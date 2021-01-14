package me.zeroeightsix.kami.plugin

import org.kamiblue.commons.interfaces.Nameable

interface IPluginClass : Nameable {
    val pluginMain: Plugin
}