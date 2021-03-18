package org.kamiblue.client.plugin.api

import org.kamiblue.commons.interfaces.Nameable

interface IPluginClass : Nameable {
    val pluginMain: Plugin
}