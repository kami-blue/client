package org.kamiblue.client.plugin.api

import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.command.CommandManager
import org.kamiblue.client.event.KamiEventBus
import org.kamiblue.client.gui.GuiManager
import org.kamiblue.client.gui.hudgui.KamiHudGui
import org.kamiblue.client.manager.Manager
import org.kamiblue.client.module.ModuleManager
import org.kamiblue.client.plugin.PluginInfo
import org.kamiblue.client.setting.ConfigManager
import org.kamiblue.client.setting.configs.PluginConfig
import org.kamiblue.client.util.threads.BackgroundJob
import org.kamiblue.client.util.threads.BackgroundScope
import org.kamiblue.commons.collections.CloseableList
import org.kamiblue.commons.interfaces.Nameable
import org.kamiblue.event.ListenerManager

/**
 * A plugin. All plugin main classes must extend this class.
 *
 * The methods onLoad and onUnload may be implemented by your
 * plugin in order to do stuff when the plugin is loaded and
 * unloaded, respectively.
 */
open class Plugin : Nameable {

    private lateinit var info: PluginInfo
    override val name: String get() = info.name
    val version: String get() = info.version
    val kamiVersion: String get() = info.minApiVersion
    val description: String get() = info.description
    val authors: Array<String> get() = info.authors
    val requiredPlugins: Array<String> get() = info.requiredPlugins
    val url: String get() = info.url
    val hotReload: Boolean get() = info.hotReload

    /**
     * Config for the plugin
     */
    val config by lazy { PluginConfig(name) }

    /**
     * The list of [Manager] the plugin will add.
     *
     * @sample org.kamiblue.client.manager.managers.KamiMojiManager
     */
    val managers = CloseableList<Manager>()

    /**
     * The list of [ClientCommand] the plugin will add.
     *
     * @sample org.kamiblue.client.command.commands.CreditsCommand
     */
    val commands = CloseableList<ClientCommand>()

    /**
     * The list of [PluginModule] the plugin will add.
     *
     * @sample org.kamiblue.client.module.modules.combat.KillAura
     */
    val modules = CloseableList<PluginModule>()

    /**
     * The list of [PluginHudElement] the plugin will add.
     *
     * @sample org.kamiblue.client.gui.hudgui.elements.client.ModuleList
     */
    val hudElements = CloseableList<PluginHudElement>()

    /**
     * The list of [BackgroundJob] the plugin will add.
     *
     * @sample org.kamiblue.client.module.modules.client.CommandConfig
     */
    val bgJobs = CloseableList<BackgroundJob>()

    internal fun setInfo(infoIn: PluginInfo) {
        info = infoIn
    }

    internal fun register() {
        managers.close()
        commands.close()
        modules.close()
        hudElements.close()
        bgJobs.close()

        ConfigManager.register(config)

        managers.forEach(KamiEventBus::subscribe)
        commands.forEach(CommandManager::register)
        modules.forEach(ModuleManager::register)
        hudElements.forEach(GuiManager::register)
        bgJobs.forEach(BackgroundScope::launchLooping)

        ConfigManager.load(config)

        modules.forEach {
            if (it.isEnabled) it.enable()
        }
    }

    internal fun unregister() {
        ConfigManager.save(config)
        ConfigManager.unregister(config)

        managers.forEach {
            KamiEventBus.unsubscribe(it)
            ListenerManager.unregister(it)
        }
        commands.forEach {
            CommandManager.unregister(it)
            ListenerManager.unregister(it)
        }
        modules.forEach {
            ModuleManager.unregister(it)
            ListenerManager.unregister(it)
        }
        hudElements.forEach {
            GuiManager.unregister(it)
            ListenerManager.unregister(it)
        }
        bgJobs.forEach(BackgroundScope::cancel)
    }

    /**
     * Called when the plugin is loaded. Override / implement this method to
     * do something when the plugin is loaded.
     */
    open fun onLoad() {}

    /**
     * Called when the plugin is unloaded. Override / implement this method to
     * do something when the plugin is unloaded.
     */
    open fun onUnload() {}

    override fun equals(other: Any?) = this === other
        || (other is Plugin
        && name == other.name)

    override fun hashCode() = name.hashCode()

    override fun toString() = info.toString()

}