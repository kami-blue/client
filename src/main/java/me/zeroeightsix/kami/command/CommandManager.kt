package me.zeroeightsix.kami.command

import kotlinx.coroutines.*
import me.zeroeightsix.kami.AsyncLoader
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.ClientExecuteEvent
import me.zeroeightsix.kami.module.modules.client.CommandConfig
import me.zeroeightsix.kami.util.StopTimer
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.formatValue
import me.zeroeightsix.kami.util.threads.defaultScope
import me.zeroeightsix.kami.util.threads.onMainThread
import org.kamiblue.command.AbstractCommandManager
import org.kamiblue.command.utils.CommandNotFoundException
import org.kamiblue.command.utils.SubCommandNotFoundException
import org.kamiblue.commons.utils.ClassUtils

object CommandManager : AbstractCommandManager<ClientExecuteEvent>(), AsyncLoader<List<Class<out ClientCommand>>> {
    override var deferred: Deferred<List<Class<out ClientCommand>>>? = null
    val prefix: String get() = CommandConfig.prefix.value

    override fun preLoad0(): List<Class<out ClientCommand>> {
        val stopTimer = StopTimer()

        val list = ClassUtils.findClasses("me.zeroeightsix.kami.command.commands", ClientCommand::class.java)
        val time = stopTimer.stop()

        KamiMod.LOG.info("${list.size} commands found, took ${time}ms")
        return list
    }

    override fun load0(input: List<Class<out ClientCommand>>) {
        val stopTimer = StopTimer()

        for (clazz in input) {
            register(ClassUtils.getInstance(clazz))
        }

        val time = stopTimer.stop()
        KamiMod.LOG.info("${input.size} commands loaded, took ${time}ms")
    }

    fun runCommand(string: String) {
        defaultScope.launch {
            val args = tryParseArgument(string) ?: return@launch
            KamiMod.LOG.debug("Running command with args: [${args.joinToString()}]")

            try {
                try {
                    invoke(ClientExecuteEvent(args))
                } catch (e: CommandNotFoundException) {
                    handleCommandNotFoundException(args.first())
                } catch (e: SubCommandNotFoundException) {
                    handleSubCommandNotFoundException(string, args, e)
                }
            } catch (e: Exception) {
                MessageSendHelper.sendChatMessage("Error occurred while running command! (${e.message}), check the log for info!")
                KamiMod.LOG.warn("Error occurred while running command!", e)
            }
        }
    }

    fun tryParseArgument(string: String) = try {
        parseArguments(string)
    } catch (e: IllegalArgumentException) {
        MessageSendHelper.sendChatMessage(e.message.toString())
        null
    }

    override suspend fun invoke(event: ClientExecuteEvent) {
        val name = event.args.getOrNull(0) ?: throw IllegalArgumentException("Arguments can not be empty!")
        val command = getCommand(name)
        val finalArg = command.finalArgs.firstOrNull { it.checkArgs(event.args) }
            ?: throw SubCommandNotFoundException(event.args, command)

        onMainThread {
            runBlocking {
                finalArg.invoke(event)
            }
        }
    }

    private fun handleCommandNotFoundException(command: String) {
        MessageSendHelper.sendChatMessage("Unknown command: ${formatValue("$prefix$command")}." +
            "Run ${formatValue("${prefix}help")} for a list of commands.")
    }

    private suspend fun handleSubCommandNotFoundException(string: String, args: Array<String>, e: SubCommandNotFoundException) {
        val bestCommand = e.command.finalArgs.maxByOrNull { it.countArgs(args) }

        var message = "Invalid syntax: ${formatValue("$prefix$string")}\n"

        if (bestCommand != null) message += "Did you mean ${formatValue("$prefix${bestCommand.printArgHelp()}")}?\n"

        message += "\nRun ${formatValue("${prefix}help ${e.command.name}")} for a list of available arguments."

        MessageSendHelper.sendChatMessage(message)
    }

}