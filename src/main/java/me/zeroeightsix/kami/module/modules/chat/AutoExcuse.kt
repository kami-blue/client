package me.zeroeightsix.kami.module.modules.chat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.GuiScreenEvent.Displayed
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MessageSendHelper
import net.minecraft.client.gui.GuiGameOver
import java.util.*

/**
 * @author sourTaste000
 * @since 7/8/2020
 * most :smoothbrain: code ever
 */
@Module.Info(
        name = "AutoExcuse",
        description = "Makes an excuse for you when you die",
        category = Module.Category.CHAT
)
class AutoExcuse : Module() {

    private val rand = Random()

    private val modeSetting : Setting<Mode> = register(Settings.e("Mode", Mode.CRYSTAL))

    @EventHandler
    var listener = Listener(EventHook { event: Displayed ->
        if (event.screen is GuiGameOver) {
            //can't send message when player died
            mc.player.respawnPlayer()

            //just change the bound to add more
            when (modeSetting.value) {
                Mode.CRYSTAL -> when (rand.nextInt(3)) {
                    0 -> MessageSendHelper.sendServerMessage("my ping is so bad")
                    1 -> MessageSendHelper.sendServerMessage("i was changing my config :(")
                    2 -> MessageSendHelper.sendServerMessage("why did my autototem break")
                    3 -> MessageSendHelper.sendServerMessage("i was desynced")
                }

                Mode.ANARCHY -> when (rand.nextInt(3)) {
                    0 -> MessageSendHelper.sendServerMessage("i hate withers")
                    1 -> MessageSendHelper.sendServerMessage("im trying to escape why did u kill me :((")
                    2 -> MessageSendHelper.sendServerMessage("can someone give me food")
                    3 -> MessageSendHelper.sendServerMessage("ouch i broke my legs")
                }

            }
        }
    })

    private enum class Mode {
        CRYSTAL, ANARCHY
    }
}