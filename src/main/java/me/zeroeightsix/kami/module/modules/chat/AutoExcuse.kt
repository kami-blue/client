package me.zeroeightsix.kami.module.modules.chat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.GuiScreenEvent.Displayed
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MessageSendHelper.sendServerMessage
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

    private val modeSetting: Setting<Mode> = register(Settings.e("Mode", Mode.PVP))

    private val excusesPVP = arrayOf<String>(
            "my ping is so bad", //0
            "i was changing my config :(", //1
            "why did my autototem break", //2
            "i was desynced", //3
            "stupid hackers killed me", //4
            "wow, so many tryhards", //5
            "lagggg" //6
    )

    private val excusesAnarchy = arrayOf<String>(
            "i hate withers", //0
            "im trying to escape why did u kill me :((", //1
            "ouch i broke my legs", //2
            "can someone give me food" //3
    )

    @EventHandler
    var listener = Listener(EventHook { event: Displayed ->
        if (event.screen is GuiGameOver) {
            do {
                if (modeSetting.value == Mode.PVP) {
                    sendServerMessage(excusesPVP[rand.nextInt(6)])
                } else if (modeSetting.value == Mode.ANARCHY) {
                    sendServerMessage(excusesAnarchy[rand.nextInt(3)])
                }
                break
            } while (!(mc.player.isDead))
        }
    })

    private enum class Mode {
        PVP, ANARCHY
    }
}