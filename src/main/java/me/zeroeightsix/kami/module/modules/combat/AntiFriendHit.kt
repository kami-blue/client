package me.zeroeightsix.kami.module.modules.combat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.ClientPlayerAttackEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.Friends
import net.minecraft.client.entity.EntityOtherPlayerMP

@Module.Info(
        name = "AntiFriendHit",
        description = "Don't hit your friends",
        category = Module.Category.COMBAT
)
object AntiFriendHit : Module() {
    @EventHandler
    private val listener = Listener(EventHook { event: ClientPlayerAttackEvent ->
        if (mc.objectMouseOver == null) return@EventHook
        val e = mc.objectMouseOver.entityHit
        if (e is EntityOtherPlayerMP && Friends.isFriend(e.getName())) {
            event.cancel()
        }
    })
}