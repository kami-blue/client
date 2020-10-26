package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.ChunkEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.block.BlockSnow
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.*
import net.minecraft.util.math.BlockPos
import net.minecraftforge.client.event.RenderBlockOverlayEvent

@Module.Info(
        name = "NoRender",
        category = Module.Category.RENDER,
        description = "Ignore entity spawn packets"
)
object NoRender : Module() {
    private val mob = setting("Mob", false)
    private val gEntity = setting("GEntity", false)
    private val `object` = setting("Object", false)
    private val items = setting("Items", false)
    private val xp = setting("XP", false)
    private val paint = setting("Paintings", false)
    private val fire = setting("Fire", true)
    private val explosion = setting("Explosions", true)
    val beacon = setting("BeaconBeams", false)
    val skylight = setting("SkyLightUpdates", true)
    private val particles = setting("Particles", false)
    val enchantingTable = setting("EnchantingBooks", true)
    private val enchantingTableSnow = setting("EnchantBookSnow", false)
    val signText = setting("SignText", false)

    init {
        listener<PacketEvent.Receive> {
            if (it.packet is SPacketSpawnMob && mob.value ||
                    it.packet is SPacketSpawnGlobalEntity && gEntity.value ||
                    it.packet is SPacketSpawnObject && `object`.value ||
                    it.packet is SPacketSpawnExperienceOrb && xp.value ||
                    it.packet is SPacketExplosion && explosion.value ||
                    it.packet is SPacketSpawnPainting && paint.value ||
                    it.packet is SPacketParticles && particles.value) it.cancel()
        }

        listener<ChunkEvent> {
            if (enchantingTableSnow.value) { // replaces enchanting tables with snow
                val chunk = it.chunk
                val layer = Blocks.SNOW_LAYER.defaultState.withProperty(BlockSnow.LAYERS, Integer.valueOf(7))
                val xRange = IntRange(chunk.x * 16, chunk.x * 16 + 15)
                val zRange = IntRange(chunk.z * 16, chunk.z * 16 + 15)

                for (y in 0..256) for (x in xRange) for (z in zRange) {
                    if (chunk.getBlockState(BlockPos(chunk.x * 16 + x, y, chunk.z * 16 + z)).block == Blocks.ENCHANTING_TABLE) {
                        chunk.setBlockState(BlockPos(chunk.x * 16 + x, y, chunk.z * 16 + z), layer)
                    }
                }
            }
        }

        listener<RenderBlockOverlayEvent> {
            if (it.overlayType == RenderBlockOverlayEvent.OverlayType.FIRE && fire.value) it.isCanceled = true
        }

        listener<SafeTickEvent> {
            if (items.value) for (entity in mc.world.loadedEntityList) {
                if (entity !is EntityItem) continue
                entity.setDead()
            }
        }
    }
}
