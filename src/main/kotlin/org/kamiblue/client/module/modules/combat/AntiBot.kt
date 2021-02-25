package org.kamiblue.client.module.modules.combat

import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.ConnectionEvent
import org.kamiblue.client.event.events.PlayerAttackEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.module.modules.misc.FakePlayer
import org.kamiblue.client.util.math.Vec2d
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.event.listener.listener
import kotlin.math.abs

internal object AntiBot : Module(
    name = "AntiBot",
    description = "Avoid attacking fake players",
    category = Category.COMBAT,
    alwaysListening = true
) {
    private val tabList = setting("Tab List", true)
    private val ping = setting("Ping", true)
    private val hp = setting("HP", true)
    private val sleeping = setting("Sleeping", false)
    private val hoverOnTop = setting("Hover On Top", true)
    private val ticksExists = setting("Ticks Exists", 200, 0..500, 10)

    private val botSet = HashSet<EntityPlayer>()

    init {
        listener<ConnectionEvent.Disconnect> {
            botSet.clear()
        }

        listener<PlayerAttackEvent> {
            if (isEnabled && botSet.contains(it.entity)) it.cancel()
        }

        safeListener<TickEvent.ClientTickEvent> {
            val cacheSet = HashSet<EntityPlayer>()
            for (entity in world.loadedEntityList) {
                if (entity !is EntityPlayer) continue
                if (entity == player) continue
                if (!isBot(entity)) continue
                cacheSet.add(entity)
            }
            botSet.removeIf { !cacheSet.contains(it) }
            botSet.addAll(cacheSet)
        }
    }

    fun isBot(entity: Entity) = isEnabled && entity is EntityPlayer && botSet.contains(entity)

    private fun SafeClientEvent.isBot(entity: EntityPlayer) = entity.name == player.name
        || entity.name == FakePlayer.playerName
        || tabList.value && connection.getPlayerInfo(entity.name) == null
        || ping.value && connection.getPlayerInfo(entity.name)?.responseTime ?: -1 <= 0
        || hp.value && entity.health !in 0f..20f
        || sleeping.value && entity.isPlayerSleeping && !entity.onGround
        || hoverOnTop.value && hoverCheck(entity)
        || entity.ticksExisted < ticksExists.value

    private fun SafeClientEvent.hoverCheck(entity: EntityPlayer): Boolean {
        val distXZ = Vec2d(entity.posX, entity.posZ).minus(player.posX, player.posZ).lengthSquared()
        return distXZ < 16 && entity.posY - player.posY > 2.0 && abs(entity.posY - entity.prevPosY) < 0.1
    }
}
