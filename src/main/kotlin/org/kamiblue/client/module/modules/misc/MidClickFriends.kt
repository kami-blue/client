package org.kamiblue.client.module.modules.misc

import kotlinx.coroutines.launch
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.util.math.RayTraceResult
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.kamiblue.client.manager.managers.FriendManager
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.defaultScope
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Mouse

internal object MidClickFriends : Module(
    name = "MidClickFriends",
    category = Category.MISC,
    description = "Middle click players to friend or unfriend them",
    showOnArray = false
) {
    private val timer = TickTimer()
    private var lastPlayer: EntityOtherPlayerMP? = null

    init {
        listener<InputEvent.MouseInputEvent> {
            // 0 is left, 1 is right, 2 is middle
            if (Mouse.getEventButton() != 2 || mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != RayTraceResult.Type.ENTITY) return@listener
            val player = mc.objectMouseOver.entityHit as? EntityOtherPlayerMP ?: return@listener
            if (timer.tick(5000L) || player != lastPlayer && timer.tick(500L)) {
                if (FriendManager.isFriend(player.name)) remove(player.name)
                else add(player.name)
                lastPlayer = player
            }
        }
    }

    private fun remove(name: String) {
        if (FriendManager.removeFriend(name)) {
            MessageSendHelper.sendChatMessage("&b$name&r has been unfriended.")
        }
    }

    private fun add(name: String) {
        defaultScope.launch {
            if (FriendManager.addFriend(name)) MessageSendHelper.sendChatMessage("&b$name&r has been friended.")
            else MessageSendHelper.sendChatMessage("Failed to find UUID of $name")
        }
    }
}