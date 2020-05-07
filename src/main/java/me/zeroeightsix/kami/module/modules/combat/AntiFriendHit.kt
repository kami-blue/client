package me.zeroeightsix.kami.module.modules.combat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.ClientPlayerAttackEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.Friends
import net.minecraft.client.entity.EntityOtherPlayerMP

/**
 * @author Sasha
 */
@Module.Info(
        name = "AntiFriendHit",
        description = "Don't hit your friends",
        category = Module.Category.COMBAT,
        alwaysListening = true
)
class AntiFriendHit : Module() {
    @EventHandler
    var listener = Listener(EventHook { event: ClientPlayerAttackEvent ->
        if (isDisabled) return@EventHook
        val e = mc.objectMouseOver.entityHit
        if (e is EntityOtherPlayerMP && Friends.isFriend(e.getName())) {
            event.cancel()
        }
    })
}