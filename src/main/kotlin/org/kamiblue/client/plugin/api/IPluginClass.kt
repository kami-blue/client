package me.zeroeightsix.kami.plugin.api

import org.kamiblue.commons.interfaces.Nameable

interface IPluginClass : Nameable {
    val pluginMain: Plugin
}