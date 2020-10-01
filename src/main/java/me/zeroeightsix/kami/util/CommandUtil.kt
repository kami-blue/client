package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.module.modules.client.CommandConfig
import me.zeroeightsix.kami.util.text.MessageSendHelper

/**
 * @author l1ving
 *
 * Updated by Xiaro on 08/18/20
 */
object CommandUtil {
    @JvmStatic
    fun runAliases(command: Command) {
        if (!CommandConfig.aliasInfo.value) return
        val amount = command.aliases.size
        if (amount > 0) {
            MessageSendHelper.sendChatMessage("'" + command.label + "' has " + grammar1(amount) + "alias" + grammar2(amount))
            MessageSendHelper.sendChatMessage(command.aliases.toString())
        }
    }

    private fun grammar1(amount: Int): String {
        return if (amount == 1) "an " else "$amount "
    }

    private fun grammar2(amount: Int): String {
        return if (amount == 1) "!" else "es!"
    }
}