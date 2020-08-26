package me.zeroeightsix.kami.module.modules.misc

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.manager.mangers.FileInstanceManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.Friends
import me.zeroeightsix.kami.util.Friends.Friend
import me.zeroeightsix.kami.util.Friends.addFriend
import me.zeroeightsix.kami.util.Friends.getFriendByName
import me.zeroeightsix.kami.util.Friends.removeFriend
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.util.math.RayTraceResult
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Mouse

/**
 * @author Indrit
 *
 * Updated by Indrit on 02/03/20
 * Updated by Xiaro on 24/08/20
 */
@Module.Info(
        name = "MidClickFriends",
        category = Module.Category.MISC,
        description = "Middle click players to friend or unfriend them",
        showOnArray = Module.ShowOnArray.OFF
)
class MidClickFriends : Module() {
    private var delay = 0

    override fun onUpdate() {
        if (delay > 0) {
            delay--
        }
    }

    @EventHandler
    var mouseListener = Listener(EventHook<InputEvent.MouseInputEvent> { event: InputEvent.MouseInputEvent? ->
        if (delay == 0 && Mouse.getEventButton() == 2 && mc.objectMouseOver != null) { // 0 is left, 1 is right, 2 is middle
            if (mc.objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY) {
                val lookedAtEntity = mc.objectMouseOver.entityHit as? EntityOtherPlayerMP
                        ?: return@EventHook
                if (Friends.isFriend(lookedAtEntity.name)) {
                    remove(lookedAtEntity.name)
                } else {
                    add(lookedAtEntity.name)
                }
            }
        }
    })

    private fun remove(name: String) {
        delay = 20
        if (removeFriend(name)) {
            MessageSendHelper.sendChatMessage("&b$name&r has been unfriended.")
        }
    }

    private fun add(name: String) {
        delay = 20
        Thread {
            if (addFriend(name)) {
                MessageSendHelper.sendChatMessage("Failed to find UUID of $name")
            } else {
                MessageSendHelper.sendChatMessage("&b$name&r has been friended.")
            }
        }.start()
    }
}