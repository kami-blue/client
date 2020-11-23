package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.ChunkEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.RenderEntityEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting.SettingListeners
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.block.BlockSnow
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.item.*
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.IAnimals
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


    private val packets = register(Settings.b("CancelPackets", true))
    private val page = register(Settings.e<Page>("Page", Page.OTHER))

    // Entities
    private val paint = register(Settings.booleanBuilder("Paintings").withValue(false).withVisibility { page.value == Page.ENTITIES }.build())
    private val animals = register(Settings.booleanBuilder("Animals").withValue(false).withVisibility { page.value == Page.ENTITIES }.build())
    private val mobs = register(Settings.booleanBuilder("Mobs").withValue(false).withVisibility { page.value == Page.ENTITIES }.build())
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

    // Others
    val map = register(Settings.booleanBuilder("Maps").withValue(false).withVisibility { page.value == Page.OTHER }.build())
    private val fire = register(Settings.booleanBuilder("Fire").withValue(true).withVisibility { page.value == Page.OTHER }.build())
    private val explosion = register(Settings.booleanBuilder("Explosions").withValue(true).withVisibility { page.value == Page.OTHER }.build())
    val signText = register(Settings.booleanBuilder("SignText").withValue(false).withVisibility { page.value == Page.OTHER }.build())
    val particles = register(Settings.booleanBuilder("Particles").withValue(true).withVisibility { page.value == Page.OTHER }.build())
    private val falling = register(Settings.booleanBuilder("FallingBlocks").withValue(true).withVisibility { page.value == Page.OTHER }.build())
    val beacon = register(Settings.booleanBuilder("BeaconBeams").withValue(true).withVisibility { page.value == Page.OTHER }.build())
    val skylight = register(Settings.booleanBuilder("SkyLightUpdates").withValue(true).withVisibility { page.value == Page.OTHER }.build())
    private val enchantingTable = register(Settings.booleanBuilder("EnchantingBooks").withValue(true).withVisibility { page.value == Page.OTHER }.build())
    private val enchantingTableSnow = register(Settings.booleanBuilder("EnchantBookSnow").withValue(false).withVisibility { page.value == Page.OTHER }.build())
    private val projectiles = register(Settings.booleanBuilder("Projectiles").withValue(false).withVisibility { page.value == Page.OTHER }.build())
    private val lightning = register(Settings.booleanBuilder("Lightning").withValue(true).withVisibility { page.value == Page.OTHER }.build())


    private val filteredSettingList = mapOf(
            player to EntityOtherPlayerMP::class.java,
            xp to EntityXPOrb::class.java,
            paint to EntityPainting::class.java,
            enchantingTable to TileEntityEnchantmentTable::class.java,
            sign to TileEntitySign::class.java,
            skull to TileEntitySkull::class.java,
            falling to EntityFallingBlock::class.java,
            armorStand to EntityArmorStand::class.java,
            endPortal to TileEntityEndPortal::class.java,
            banner to TileEntityBanner::class.java,
            itemFrame to EntityItemFrame::class.java,
            items to EntityItem::class.java,
            crystal to EntityEnderCrystal::class.java
    )

    var entityList = HashSet<Class<*>>()

    private enum class Page {
        OTHER, ENTITIES
    }

    init {
        listener<PacketEvent.Receive> {
            if (it.packet is SPacketSpawnGlobalEntity && lightning.value ||
                    it.packet is SPacketSpawnExperienceOrb && xp.value && packets.value ||
                    it.packet is SPacketExplosion && explosion.value ||
                    it.packet is SPacketSpawnPainting && paint.value && packets.value ||
                    it.packet is SPacketParticles && particles.value
            ) it.cancel()

            if (it.packet is SPacketSpawnObject) {
                when (it.packet.type) {
                    71 -> if (itemFrame.value && packets.value) it.cancel()
                    78 -> if (armorStand.value && packets.value) it.cancel()
                    51 -> if (crystal.value && packets.value) it.cancel()
                    2 -> if (items.value && packets.value) it.cancel()
                    70 -> if (falling.value && packets.value) it.cancel()
                    else -> if (projectiles.value) it.cancel()
                }
            }

            if (it.packet is SPacketSpawnMob && packets.value) {
                if (EntityMob::class.java.isAssignableFrom(net.minecraftforge.registries.GameData.getEntityRegistry().getValue(it.packet.entityType).entityClass)) {
                    if (mobs.value) it.cancel()
                } else {
                    if (animals.value && IAnimals::class.java.isAssignableFrom(net.minecraftforge.registries.GameData.getEntityRegistry().getValue(it.packet.entityType).entityClass))
                        it.cancel()
                }
            }
        }

        listener<RenderEntityEvent.Pre> {
            if (it.entity != null && entityList.contains(it.entity::class.java) ||
                    (animals.value && it.entity is IAnimals && it.entity !is EntityMob) ||
                    (mobs.value && it.entity is EntityMob)) {
                it.cancel()
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
        val updater = SettingListeners { updatelist() }
        settingList.forEach { it.settingListener = updater }

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
        filteredSettingList.forEach { if (it.key.value == true) entityList.add(it.value) }
        // needed because there are 2 entities, the gateway and the portal
        if (endPortal.value) entityList.add(TileEntityEndGateway::class.java)
    }

    override fun onEnable() {
        updatelist()
    }

}


