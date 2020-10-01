package me.zeroeightsix.kami.module.modules.render

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent.Receive
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.entity.item.EntityItem
import net.minecraft.network.play.server.*
import net.minecraftforge.client.event.RenderBlockOverlayEvent

@Module.Info(
        name = "NoRender",
        category = Module.Category.RENDER,
        description = "Ignore entity spawn packets"
)
object NoRender : Module() {
    private val mob = register(Settings.b("Mob", false))
    private val sand = register(Settings.b("Sand", false))
    private val gEntity = register(Settings.b("GEntity", false))
    private val `object` = register(Settings.b("Object", false))
    private val items = register(Settings.b("Items", false))
    private val xp = register(Settings.b("XP", false))
    private val paint = register(Settings.b("Paintings", false))
    private val fire = register(Settings.b("Fire", true))
    private val explosion = register(Settings.b("Explosions", true))
    val beacon = register(Settings.b("BeaconBeams", false))
    val skylight = register(Settings.b("SkyLightUpdates", true))

    @EventHandler
    private val receiveListener = Listener(EventHook { event: Receive ->
        val packet = event.packet
        if (packet is SPacketSpawnMob && mob.value ||
                packet is SPacketSpawnGlobalEntity && gEntity.value ||
                packet is SPacketSpawnObject && `object`.value ||
                packet is SPacketSpawnExperienceOrb && xp.value ||
                packet is SPacketSpawnObject && sand.value ||
                packet is SPacketExplosion && explosion.value ||
                packet is SPacketSpawnPainting && paint.value) event.cancel()
    })

    @EventHandler
    private val blockOverlayEventListener = Listener(EventHook { event: RenderBlockOverlayEvent ->
        if (fire.value && event.overlayType == RenderBlockOverlayEvent.OverlayType.FIRE) event.isCanceled = true
    })

    override fun onUpdate() {
        if (items.value) for (entity in mc.world.loadedEntityList) {
            if (entity !is EntityItem) continue
            entity.setDead()
        }
    }
}