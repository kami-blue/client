package me.zeroeightsix.kami.command

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.setting.SettingsRegister
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.util.text.TextFormatting
import org.kamiblue.command.AbstractCommandManager
import org.kamiblue.command.ExecuteEvent
import org.kamiblue.command.utils.CommandNotFoundException
import org.kamiblue.command.utils.SubCommandNotFoundException
import org.kamiblue.commons.utils.ClassUtils

object CommandManager : AbstractCommandManager<ExecuteEvent>() {

    val prefix: Setting<String> = Settings.s("commandPrefix", ";")

    fun init() {
        val stopTimer = TimerUtils.StopTimer()
        val commandClasses = ClassUtils.findClasses("me.zeroeightsix.kami.command.commands", ClientCommand::class.java)

        for (clazz in commandClasses) {
            register(ClassUtils.getInstance(clazz))
        }

        val time = stopTimer.stop()
        KamiMod.LOG.info("${getCommands().size} modules loaded, took ${time}ms")
    }

    suspend fun runCommand(string: String) {
        val args = tryParseArgument(string) ?: return

        try {
            try {
                invoke(ExecuteEvent(this, args))
            } catch (e: CommandNotFoundException) {
                handleCommandNotFoundException(args.first())
            } catch (e: SubCommandNotFoundException) {
                handleSubCommandNotFoundException(string, args, e)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            MessageSendHelper.sendChatMessage("Error occurred while running command! (${e.message}), check the log for info!")
        }
    }

    private fun tryParseArgument(string: String) = try {
        parseArguments(string)
    } catch (e: IllegalArgumentException) {
        MessageSendHelper.sendChatMessage(e.message ?: "Null")
        null
    }

    private fun handleCommandNotFoundException(command: String) {
        MessageSendHelper.sendChatMessage("Unknown command: '${TextFormatting.GRAY}$prefix$command${TextFormatting.RESET}'." +
            "Run '${TextFormatting.GRAY}${prefix}help${TextFormatting.RESET}' for a list of commands.")
    }

    private suspend fun handleSubCommandNotFoundException(string: String, args: Array<String>, e: SubCommandNotFoundException) {
        val bestCommand = e.command.finalArgs.maxByOrNull { it.countArgs(args) }

        var message = "Invalid syntax: '${TextFormatting.GRAY}$prefix$string${TextFormatting.RESET}'.\n"

        if (bestCommand != null) message += "Did you mean '${TextFormatting.GRAY}$prefix${bestCommand.printArgHelp()}${TextFormatting.RESET}'?\n"

        message += "\nRun '${TextFormatting.GRAY}${prefix}help ${e.command.name}${TextFormatting.RESET}' for a list of available arguments."

        MessageSendHelper.sendChatMessage(message)
    }

    init {
        SettingsRegister.register("commandPrefix", prefix)
    }

}