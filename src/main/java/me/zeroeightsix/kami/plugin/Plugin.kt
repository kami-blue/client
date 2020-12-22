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

abstract class Plugin(
    override val name: String,
    val author: String,
    val version: String
) : Nameable {
    val managers = CloseableList<Manager>()
    val commands = CloseableList<ClientCommand>()
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

    abstract fun onLoad()
    abstract fun onUnload()

    override fun equals(other: Any?) = this === other
        || (other is Plugin
        && name == other.name)

    override fun hashCode() = name.hashCode()

}