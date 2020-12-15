package me.zeroeightsix.kami.command

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.Wrapper
import org.kamiblue.command.AbstractArg
import org.kamiblue.command.CommandBuilder
import org.kamiblue.command.ExecuteEvent
import org.kamiblue.command.utils.BuilderBlock

abstract class ClientCommand(
    name: String,
    alias: Array<out String> = emptyArray(),
    description: String = "No description",
) : CommandBuilder<ClientExecuteEvent>(name, alias, description) {

    protected val mc = Wrapper.minecraft
    val chatLabel = "[$name]"

    @CommandBuilder
    protected fun AbstractArg<*>.module(
        name: String,
        block: BuilderBlock<Module>
    ) {
        arg(ModuleArg(name), block)
    }

    protected companion object {
        val commandScope = CoroutineScope(Dispatchers.Default + CoroutineName("KAMI Blue Command"))
    }

}