package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.manager.managers.WaypointManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.math.CoordinateConverter.asString
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.init.SoundEvents
import net.minecraft.tileentity.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.roundToInt

object StashFinder : Module(
    name = "StashFinder",
    category = Category.MISC,
    description = "Logs storage units in render distance."
) {
    private val saveToFile = setting("SaveToFile", true)
    private val logToChat = setting("LogToChat", true)
    private val playSound = setting("PlaySound", true)

    private val logChests = setting("Chests", true)
    private val chestDensity = setting("MinChests", 5, 1..20, 1, { logChests.value })

    private val logShulkers = setting("Shulkers", true)
    private val shulkerDensity = setting("MinShulkers", 1, 1..20, 1, { logShulkers.value })

    private val logDroppers = setting("Droppers", true)
    private val dropperDensity = setting("MinDroppers", 5, 1..20, 1, { logDroppers.value })

    private val logDispensers = setting("Dispensers", true)
    private val dispenserDensity = setting("MinDispensers", 5, 1..20, 1, { logDispensers.value })

    private val logHoppers = setting("Hoppers", true)
    private val hopperDensity = setting("MinHoppers", 5, 1..20, 1, { logHoppers.value })


    private val chunkData = LinkedHashMap<Long, ChunkStats>()
    private val knownPositions = LinkedHashSet<BlockPos>()

    override fun onEnable() {
        chunkData.clear()
        knownPositions.clear()
    }

    init {
        safeListener<TickEvent.ClientTickEvent> {
            world.loadedTileEntityList
                    .filter {
                        logChests.value && it is TileEntityChest
                                || logShulkers.value && it is TileEntityShulkerBox
                                || logDroppers.value && it is TileEntityDropper
                                || logDispensers.value && it is TileEntityDispenser
                                || logHoppers.value && it is TileEntityHopper
                    }
                    .forEach { logTileEntity(it) }

            chunkData.values.filter { it.hot }.forEach { chunkStats ->
                chunkStats.hot = false

                // mfw int array instead of Vec3i
                if (saveToFile.value) {
                    WaypointManager.add(chunkStats.getBlockPos(), chunkStats.toString())
                }

                if (playSound.value) {
                    mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                }

                if (logToChat.value) {
                    val positionString = chunkStats.getBlockPos().asString()
                    MessageSendHelper.sendChatMessage("$chatName $positionString $chunkStats")
                }
            }
        }
    }

    private fun logTileEntity(tileEntity: TileEntity) {
        if (knownPositions.contains(tileEntity.pos)) return

        knownPositions.add(tileEntity.pos)

        val chunk = ChunkPos.asLong(tileEntity.pos.x / 16, tileEntity.pos.z / 16)
        val chunkStats = chunkData.getOrPut(chunk, { ChunkStats() })

        chunkStats.add(tileEntity)
        if (chunkStats.chests >= chestDensity.value || chunkStats.shulkers >= shulkerDensity.value || chunkStats.droppers >= dropperDensity.value || chunkStats.dispensers >= dispenserDensity.value || chunkStats.hoppers >= hopperDensity.value) {
            chunkStats.hot = true
        }
    }

    private data class ChunkStats(var chests: Int = 0, var shulkers: Int = 0, var droppers: Int = 0, var dispensers: Int = 0, var hoppers: Int = 0, var hot: Boolean = false) {
        private val tileEntities = ArrayList<TileEntity>()

        fun add(tileEntity: TileEntity) {
            when (tileEntity) {
                is TileEntityChest -> chests++
                is TileEntityShulkerBox -> shulkers++
                is TileEntityDropper -> droppers++
                is TileEntityDispenser -> dispensers++
            }

            tileEntities.add(tileEntity)
        }

        // Averages the positions of all the tile entities
        fun getBlockPos(): BlockPos {
            val x = tileEntities.map { it.pos.x }.average().roundToInt()
            val y = tileEntities.map { it.pos.y }.average().roundToInt()
            val z = tileEntities.map { it.pos.z }.average().roundToInt()
            return BlockPos(x, y, z)
        }

        override fun toString(): String {
            return "($chests chests, $shulkers shulkers, $droppers droppers, $dispensers dispensers, $hoppers hoppers)"
        }
    }
}
