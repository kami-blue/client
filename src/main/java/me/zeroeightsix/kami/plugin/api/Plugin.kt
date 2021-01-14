package me.zeroeightsix.kami.plugin.api

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.plugin.PluginInfo
import me.zeroeightsix.kami.setting.ConfigManager
import me.zeroeightsix.kami.setting.configs.NameableConfig
import me.zeroeightsix.kami.util.threads.BackgroundJob
import me.zeroeightsix.kami.util.threads.BackgroundScope
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
    val kamiVersion: String get() = info.kamiVersion
    val description: String get() = info.description
    val authors: Array<String> get() = info.authors
    val requiredPlugins: Array<String> get() = info.requiredPlugins
    val url: String get() = info.url
    val hotReload: Boolean get() = info.hotReload

    /**
     * Config for the plugin
     */
    val config by lazy { NameableConfig<IPluginClass>(name, "${KamiMod.DIRECTORY}config/plugins/") }

    /**
     * The list of [Manager] the plugin will add.
     *
     * @sample me.zeroeightsix.kami.manager.managers.KamiMojiManager
     */
    val managers = CloseableList<Manager>()

    /**
     * The list of [ClientCommand] the plugin will add.
     *
     * @sample me.zeroeightsix.kami.command.commands.CreditsCommand
     */
    val commands = CloseableList<ClientCommand>()

    /**
     * The list of [PluginModule] the plugin will add.
     *
     * @sample me.zeroeightsix.kami.module.modules.combat.KillAura
     */
    val modules = CloseableList<PluginModule>()

    /**
     * The list of [BackgroundJob] the plugin will add.
     *
     * @sample me.zeroeightsix.kami.module.modules.client.CommandConfig
     */
    val bgJobs = CloseableList<BackgroundJob>()

    internal fun setInfo(infoIn: PluginInfo) {
        info = infoIn
    }

    internal fun register() {
        managers.close()
        commands.close()
        modules.close()
        bgJobs.close()

        ConfigManager.register(config)

        managers.forEach(KamiEventBus::subscribe)
        commands.forEach(CommandManager::register)
        modules.forEach(ModuleManager::register)
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