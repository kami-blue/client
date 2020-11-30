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
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.registries.GameData

@Module.Info(
    name = "NoRender",
    category = Module.Category.RENDER,
    description = "Ignore entity spawn packets"
)
object NoRender : Module() {

    private val packets = register(Settings.b("CancelPackets", true))
    private val page = register(Settings.e<Page>("Page", Page.OTHER))

    // Entities
    private val paint = register(Settings.booleanBuilder("Paintings").withValue(false).withVisibility { page.value == Page.ENTITIES })
    private val animals = register(Settings.booleanBuilder("Animals").withValue(false).withVisibility { page.value == Page.ENTITIES })
    private val mobs = register(Settings.booleanBuilder("Mobs").withValue(false).withVisibility { page.value == Page.ENTITIES })
    private val player = register(Settings.booleanBuilder("Players").withValue(false).withVisibility { page.value == Page.ENTITIES })
    private val sign = register(Settings.booleanBuilder("Signs").withValue(false).withVisibility { page.value == Page.ENTITIES })
    private val skull = register(Settings.booleanBuilder("Heads").withValue(false).withVisibility { page.value == Page.ENTITIES })
    private val armorStand = register(Settings.booleanBuilder("ArmorStands").withValue(false).withVisibility { page.value == Page.ENTITIES })
    private val endPortal = register(Settings.booleanBuilder("EndPortals").withValue(false).withVisibility { page.value == Page.ENTITIES })
    private val banner = register(Settings.booleanBuilder("Banners").withValue(false).withVisibility { page.value == Page.ENTITIES })
    private val itemFrame = register(Settings.booleanBuilder("ItemFrames").withValue(false).withVisibility { page.value == Page.ENTITIES })
    private val xp = register(Settings.booleanBuilder("XP").withValue(false).withVisibility { page.value == Page.ENTITIES })
    private val items = register(Settings.booleanBuilder("Items").withValue(false).withVisibility { page.value == Page.ENTITIES })
    private val crystal = register(Settings.booleanBuilder("Crystals").withValue(false).withVisibility { page.value == Page.ENTITIES })

    // Others
    val map = register(Settings.booleanBuilder("Maps").withValue(false).withVisibility { page.value == Page.OTHER })
    private val explosion = register(Settings.booleanBuilder("Explosions").withValue(true).withVisibility { page.value == Page.OTHER })
    val signText = register(Settings.booleanBuilder("SignText").withValue(false).withVisibility { page.value == Page.OTHER })
    val particles = register(Settings.booleanBuilder("Particles").withValue(true).withVisibility { page.value == Page.OTHER })
    private val falling = register(Settings.booleanBuilder("FallingBlocks").withValue(true).withVisibility { page.value == Page.OTHER })
    val beacon = register(Settings.booleanBuilder("BeaconBeams").withValue(true).withVisibility { page.value == Page.OTHER })
    val skylight = register(Settings.booleanBuilder("SkyLightUpdates").withValue(true).withVisibility { page.value == Page.OTHER })
    private val enchantingTable = register(Settings.booleanBuilder("EnchantingBooks").withValue(true).withVisibility { page.value == Page.OTHER })
    private val enchantingTableSnow = register(Settings.booleanBuilder("EnchantTableSnow").withValue(false).withVisibility { page.value == Page.OTHER })
    private val projectiles = register(Settings.booleanBuilder("Projectiles").withValue(false).withVisibility { page.value == Page.OTHER })
    private val lightning = register(Settings.booleanBuilder("Lightning").withValue(true).withVisibility { page.value == Page.OTHER })

    private enum class Page {
        OTHER, ENTITIES
    }

    private val settingMap = mapOf(
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

    var entityList = HashSet<Class<*>>(); private set

    init {
        listener<PacketEvent.Receive> {
            if (lightning.value && it.packet is SPacketSpawnGlobalEntity ||
                explosion.value && it.packet is SPacketExplosion ||
                particles.value && it.packet is SPacketParticles ||
                packets.value && xp.value && it.packet is SPacketSpawnExperienceOrb ||
                packets.value && paint.value && it.packet is SPacketSpawnPainting
            ) it.cancel()

            if (it.packet is SPacketSpawnObject) {
                it.isCancelled = when (it.packet.type) {
                    71 -> packets.value && itemFrame.value
                    78 -> packets.value && armorStand.value
                    51 -> packets.value && crystal.value
                    2 -> packets.value && items.value
                    70 -> packets.value && falling.value
                    else -> projectiles.value
                }
            }

            if (packets.value && it.packet is SPacketSpawnMob) {
                val entityClass = GameData.getEntityRegistry().getValue(it.packet.entityType).entityClass
                if (EntityMob::class.java.isAssignableFrom(entityClass)) {
                    if (mobs.value) it.cancel()
                } else if (IAnimals::class.java.isAssignableFrom(entityClass)) {
                    if (animals.value) it.cancel()
                }
            }
        }

        listener<RenderEntityEvent.Pre> {
            if (it.entity != null && entityList.contains(it.entity::class.java) ||
                animals.value && it.entity !is EntityMob && it.entity is IAnimals ||
                mobs.value && it.entity is EntityMob) {
                it.cancel()
            }
        }

        listener<ChunkEvent> {
            if (enchantingTableSnow.value) { // replaces enchanting tables with snow
                val blockState = Blocks.SNOW_LAYER.defaultState.withProperty(BlockSnow.LAYERS, 7)
                val xRange = it.chunk.x * 16..it.chunk.x * 16 + 15
                val zRange = it.chunk.z * 16..it.chunk.z * 16 + 15

                for (y in 0..256) for (x in xRange) for (z in zRange) {
                    val blockPos = BlockPos(it.chunk.x * 16 + x, y, it.chunk.z * 16 + z)
                    if (it.chunk.getBlockState(blockPos).block == Blocks.ENCHANTING_TABLE) {
                        it.chunk.setBlockState(blockPos, blockState)
                    }
                }
            }
        }

        listener<SafeTickEvent> {
            if (it.phase == TickEvent.Phase.END && items.value) {
                for (entity in mc.world.loadedEntityList) {
                    if (entity !is EntityItem) continue
                    entity.setDead()
                }
            }
        }
    }

    override fun onEnable() {
        updateList()
    }

    private fun updateList() {
        entityList = HashSet<Class<*>>().apply {
            settingMap.forEach {
                if (it.key.value) add(it.value)
            }
            // needed because there are 2 entities, the gateway and the portal
            if (endPortal.value) {
                add(TileEntityEndGateway::class.java)
            }
        }
    }

    init {
        val listener = SettingListeners { updateList() }
        settingList.forEach { it.settingListener = listener }
    }

}