package me.zeroeightsix.kami.command

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.util.CommandUtil.runAliases
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import org.kamiblue.commons.utils.ReflectionUtils
import java.util.*

class CommandManager {
    val commands = ArrayList<Command>()
    private val commandRegex = " (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()

    init {
        val stopTimer = TimerUtils.StopTimer()
        val classes = ReflectionUtils.getSubclassOfFast<Command>("me.zeroeightsix.kami.command.commands")
        for (clazz in classes) {
            try {
                commands.add(clazz.getConstructor().newInstance())
            } catch (e: Exception) {
                KamiMod.LOG.error("Couldn't initiate command " + clazz.simpleName, e)
            }
        }
        val time = stopTimer.stop()
        KamiMod.LOG.info("${commands.size} commands loaded, took ${time}ms")
    }

    fun callCommand(string: String) {
        val args = string.split(commandRegex).toTypedArray() // Split by every space if it isn't surrounded by quotes
        val label = if (args[0].contains(" ")) args[0].substring(args[0].indexOf(" ")).substring(1) else args[0].substring(1)

        for (i in 1 until args.size) {
            val arg = args[i]
            if (arg.isBlank()) continue
            args[i] = strip(args[i])
        }

        for (command in commands) {
            if (command.label.equals(label, ignoreCase = true)) {
                command.call(args)
                runAliases(command)
                return
            } else if (command.getAliases().any { it.equals(label, ignoreCase = true) }) {
                command.call(args)
                return
            }
        }

        MessageSendHelper.sendChatMessage("&7Unknown command. try '&f" + Command.getCommandPrefix() + "cmds&7' for a list of commands.")
    }

    companion object {

        private fun strip(str: String): String {
            return if (str.startsWith("\"") && str.endsWith("\"")) {
                str.substring(1, str.length - 1)
            } else {
                str
            }
        }
    }
}