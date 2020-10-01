package me.zeroeightsix.kami.module.modules.chat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.GuiScreenEvent.Displayed
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendServerMessage
import net.minecraft.client.gui.GuiGameOver
import java.util.*

/**
 * most :smoothbrain: code ever
 */
@Module.Info(
        name = "AutoExcuse",
        description = "Makes an excuse for you when you die",
        category = Module.Category.CHAT
)
object AutoExcuse : Module() {

    private val rand = Random()

    private val excuses = arrayOf(
            "sorry, im using ",
            "my ping is so bad",
            "i was changing my config :(",
            "why did my autototem break",
            "i was desynced",
            "stupid hackers killed me",
            "wow, so many tryhards",
            "lagggg",
            "there was an optfine error",
            "i wasnt trying",
            "im not using lion client"
    )

    private val clients = arrayOf(
            "Future",
            "Salhack",
            "Pyro",
            "Impact"
    )

    private fun getExcuse(): String {
        val excuse = rand.nextInt(excuses.size)
        return if (excuse == 0) {
            excuses[0] + clients.random()
        } else {
            excuses[excuse]
        }
    }

    /* it's not actually unreachable, thanks intellij */
    @Suppress("UNREACHABLE_CODE")
    @EventHandler
    private val listener = Listener(EventHook { event: Displayed ->
        if (event.screen is GuiGameOver) {
            do {
                sendServerMessage(getExcuse())
                break
            } while (!(mc.player.isDead))
        }
    })
}
