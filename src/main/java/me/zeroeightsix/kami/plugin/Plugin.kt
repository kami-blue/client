package me.zeroeightsix.kami.plugin

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
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
open class Plugin(
    /**
     * The name of the plugin; will be used as both an identifier and a display name.
     */
    override val name: String,

    /**
     * The plugin's version.
     */
    val version: String,

    /**
     * The minimum version of KAMI Blue required for the plugin to run.
     */
    val minKamiVersion: String,

    /**
     * A short description of the plugin.
     */
    val description: String = "No Description",

    /**
     * A list of the names of the plugin's authors.
     */
    val authors: Array<String> = emptyArray(),

    /**
     * Other plugins that must be installed in order for this plugin to work correctly.
     */
    val requiredPlugins: Array<String> = emptyArray(),

    /**
     * A link to the plugin's website.
     */
    val url: String = "https://github.com/kami-blue/client"
) : Nameable {

    /**
     * The list of managers the plugin will add.
     *
     * @sample me.zeroeightsix.kami.manager.managers.KamiMojiManager
     */
    val managers = CloseableList<Manager>()

    /**
     * The list of commands the plugin will add.
     *
     * @sample me.zeroeightsix.kami.command.commands.CreditsCommand
     */
    val commands = CloseableList<ClientCommand>()

    /**
     * The list of modules the plugin will add.
     *
     * @sample me.zeroeightsix.kami.module.modules.combat.KillAura
     */
    val modules = CloseableList<Module>()

    internal fun register() {
        managers.close()
        commands.close()
        modules.close()

        managers.forEach {
            KamiEventBus.subscribe(it)
        }
        commands.forEach {
            CommandManager.register(it)
        }
        modules.forEach {
            ModuleManager.register(it)
        }

        // TODO: Loads config here (After GUI PR)

        modules.forEach {
            if (it.isEnabled) it.enable()
        }
    }

    internal fun unregister() {
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
        }
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

    override fun equals(other: Any?) =
        this === other
        || (other is Plugin
        && name == other.name)

    override fun hashCode() = name.hashCode()
}