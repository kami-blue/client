package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.ChunkEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting.SettingListeners
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.block.BlockSnow
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.item.*
import net.minecraft.entity.monster.EntityGolem
import net.minecraft.entity.passive.*
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.*
import net.minecraft.tileentity.*
import net.minecraft.util.math.BlockPos
import net.minecraftforge.client.event.RenderBlockOverlayEvent

@Module.Info(
        name = "NoRender",
        category = Module.Category.RENDER,
        description = "Ignore entity spawn packets"
)
object NoRender : Module() {

    private val page = register(Settings.e<Page>("Page", Page.ENTITIES))

    //Entities
    private val paint = register(Settings.booleanBuilder("Paintings").withValue(false).withVisibility { page.value == Page.ENTITIES }.build())
    private val mob = register(Settings.booleanBuilder("Mobs").withValue(false).withVisibility { page.value == Page.ENTITIES }.build())
    private val player = register(Settings.booleanBuilder("Players").withValue(false).withVisibility { page.value == Page.ENTITIES }.build())
    private val sign = register(Settings.booleanBuilder("Signs").withValue(false).withVisibility { page.value == Page.ENTITIES }.build())
    private val skull = register(Settings.booleanBuilder("Heads").withValue(false).withVisibility { page.value == Page.ENTITIES }.build())
    private val armorStand = register(Settings.booleanBuilder("ArmorStands").withValue(false).withVisibility { page.value == Page.ENTITIES }.build())
    private val endPortal = register(Settings.booleanBuilder("EndPortals").withValue(false).withVisibility { page.value == Page.ENTITIES }.build())
    private val banner = register(Settings.booleanBuilder("Banners").withValue(false).withVisibility { page.value == Page.ENTITIES }.build())
    private val itemFrame = register(Settings.booleanBuilder("ItemFrames").withValue(false).withVisibility { page.value == Page.ENTITIES }.build())
    private val xp = register(Settings.booleanBuilder("XP").withValue(false).withVisibility { page.value == Page.ENTITIES }.build())
    private val items = register(Settings.booleanBuilder("Items").withValue(false).withVisibility { page.value == Page.ENTITIES }.build())
    private val crystal = register(Settings.booleanBuilder("Crystals").withValue(false).withVisibility { page.value == Page.ENTITIES }.build())

    //Others
    val map = register(Settings.booleanBuilder("Maps").withValue(false).withVisibility { page.value == Page.OTHER }.build())
    private val fire = register(Settings.booleanBuilder("Fire").withValue(true).withVisibility { page.value == Page.OTHER }.build())
    private val explosion = register(Settings.booleanBuilder("Explosions").withValue(true).withVisibility { page.value == Page.OTHER }.build())
    val signText = register(Settings.booleanBuilder("SignText").withValue(false).withVisibility { page.value == Page.OTHER }.build())
    val particles = register(Settings.booleanBuilder("Particles").withValue(true).withVisibility { page.value == Page.OTHER }.build())
    private val falling = register(Settings.booleanBuilder("Falling Blocks").withValue(true).withVisibility { page.value == Page.OTHER }.build())
    val beacon = register(Settings.booleanBuilder("BeaconBeams").withValue(true).withVisibility { page.value == Page.OTHER }.build())
    val skylight = register(Settings.booleanBuilder("SkyLightUpdates").withValue(true).withVisibility { page.value == Page.OTHER }.build())
    private val enchantingTable = register(Settings.booleanBuilder("EnchantingBooks").withValue(true).withVisibility { page.value == Page.OTHER }.build())
    private val enchantingTableSnow = register(Settings.booleanBuilder("EnchantBookSnow").withValue(false).withVisibility { page.value == Page.OTHER }.build())
    private val projectiles = register(Settings.booleanBuilder("Projectiles etc").withValue(false).withVisibility { page.value == Page.OTHER }.build())
    private val lightning = register(Settings.booleanBuilder("Lightning").withValue(true).withVisibility { page.value == Page.OTHER }.build())

    //Generic
    private val packets = register(Settings.booleanBuilder("Cancel Packets").withValue(true).withVisibility { page.value == Page.GENERIC }.build())

    var entityList = HashSet<Class<*>>()

    private enum class Page {
        ENTITIES, OTHER, GENERIC
    }

    init {
        listener<PacketEvent.Receive> {
            if (it.packet is SPacketSpawnMob && mob.value && packets.value ||
                    it.packet is SPacketSpawnGlobalEntity && lightning.value ||
                    it.packet is SPacketSpawnExperienceOrb && xp.value && packets.value ||
                    it.packet is SPacketExplosion && explosion.value ||
                    it.packet is SPacketSpawnPainting && paint.value && packets.value ||
                    it.packet is SPacketParticles && particles.value
            ) it.cancel()
            if (it.packet is SPacketSpawnObject) {
                if (packets.value) {
                    when (it.packet.type) {
                        71 -> if (itemFrame.value) it.cancel()
                        78 -> if (armorStand.value) it.cancel()
                        51 -> if (crystal.value) it.cancel()
                        2 -> if (items.value) it.cancel()
                        70 -> if (falling.value) it.cancel()
                    }
                }
                if (projectiles.value) it.cancel()
            }
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

        val listener = SettingListeners { updatelist() }
        lightning.settingListener = listener
        mob.settingListener = listener
        lightning.settingListener = listener
        items.settingListener = listener
        xp.settingListener = listener
        paint.settingListener = listener
        fire.settingListener = listener
        explosion.settingListener = listener
        beacon.settingListener = listener
        skylight.settingListener = listener
        enchantingTable.settingListener = listener
        sign.settingListener = listener
        skull.settingListener = listener
        falling.settingListener = listener
        armorStand.settingListener = listener
        endPortal.settingListener = listener
        banner.settingListener = listener
        itemFrame.settingListener = listener
        player.settingListener = listener

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

    private fun updatelist() {
        entityList = HashSet()
        if (mob.value) {
            entityList.add(EntityAnimal::class.java)
            entityList.add(EntityCreature::class.java)
            entityList.add(EntityAmbientCreature::class.java)
            entityList.add(EntityTameable::class.java)
            entityList.add(AbstractHorse::class.java)
            entityList.add(AbstractChestHorse::class.java)
            entityList.add(EntityGolem::class.java)
        }
        if (player.value) entityList.add(AbstractClientPlayer::class.java)
        if (xp.value) entityList.add(EntityXPOrb::class.java)
        if (paint.value) entityList.add(EntityPainting::class.java)
        if (enchantingTable.value) entityList.add(TileEntityEnchantmentTable::class.java)
        if (sign.value) entityList.add(TileEntitySign::class.java)
        if (skull.value) entityList.add(TileEntitySkull::class.java)
        if (falling.value) entityList.add(EntityFallingBlock::class.java)
        if (armorStand.value) entityList.add(EntityArmorStand::class.java)
        if (endPortal.value) entityList.add(TileEntityEndPortal::class.java)
        if (banner.value) entityList.add(TileEntityBanner::class.java)
        if (itemFrame.value) entityList.add(EntityItemFrame::class.java)
        if (items.value) entityList.add(EntityItem::class.java)
        if (crystal.value) entityList.add(EntityEnderCrystal::class.java)
    }

    override fun onEnable() {
        updatelist()
    }

}


