package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.Coordinate
import me.zeroeightsix.kami.util.MessageSendHelper
import me.zeroeightsix.kami.util.Waypoint
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.init.SoundEvents
import net.minecraft.tileentity.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import kotlin.math.roundToInt

/**
 * @author Nucleus
 */
@Module.Info(
        name = "StashFinder",
        category = Module.Category.MISC,
        description = "Logs storage units in render distance."
)
class StashFinder : Module() {
    private val logToChat = register(Settings.b("LogToChat"))
    private val playSound = register(Settings.b("PlaySound"))

    private val logChests = register(Settings.b("Chests"))
    private val chestDensity = register(Settings.integerBuilder("MinChests").withMinimum(1).withMaximum(20).withValue(5).withVisibility { logChests.value }.build())

    private val logShulkers = register(Settings.b("Shulkers"))
    private val shulkerDensity = register(Settings.integerBuilder("MinShulkers").withMinimum(1).withMaximum(20).withValue(1).withVisibility { logShulkers.value }.build())

    private val logDroppers = register(Settings.b("Droppers", false))
    private val dropperDensity = register(Settings.integerBuilder("MinDroppers").withMinimum(1).withMaximum(20).withValue(5).withVisibility { logDroppers.value }.build())

    private val logDispensers = register(Settings.b("Dispensers", false))
    private val dispenserDensity = register(Settings.integerBuilder("MinDispensers").withMinimum(1).withMaximum(20).withValue(5).withVisibility { logDispensers.value }.build())

    private data class ChunkStats(var chests: Int = 0, var shulkers: Int = 0, var droppers: Int = 0, var dispensers: Int = 0, var hot: Boolean = false) {
        val tileEntities = mutableListOf<TileEntity>()

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
        fun getPosition(): IntArray {
            val x = tileEntities.map { it.pos.x }.average().roundToInt()
            val y = tileEntities.map { it.pos.y }.average().roundToInt()
            val z = tileEntities.map { it.pos.z }.average().roundToInt()
            return intArrayOf(x, y, z)
        }

        fun getBlockPos(): Coordinate {
            val xyz = this.getPosition()
            return Coordinate(xyz[0], xyz[1], xyz[2])
        }

        override fun toString(): String {
            return "($chests chests, $shulkers shulkers, $droppers droppers, $dispensers dispensers)"
        }
    }

    private val chunkData = hashMapOf<Long, ChunkStats>()
    private val knownPositions = mutableListOf<BlockPos>()

    override fun onEnable() {
        super.onEnable()
        chunkData.clear()
        knownPositions.clear()
    }

    private fun logTileEntity(tileEntity: TileEntity) {
        if (knownPositions.contains(tileEntity.pos)) return

        knownPositions.add(tileEntity.pos)

        val chunk = ChunkPos.asLong(tileEntity.pos.x / 16, tileEntity.pos.z / 16)
        val chunkStats = chunkData.getOrPut(chunk, { ChunkStats() })

        chunkStats.add(tileEntity)
        if (chunkStats.chests >= chestDensity.value || chunkStats.shulkers >= shulkerDensity.value || chunkStats.droppers >= dropperDensity.value || chunkStats.dispensers >= dispenserDensity.value) {
            chunkStats.hot = true
        }
    }

    override fun onUpdate() {
        super.onUpdate()

        mc.world.loadedTileEntityList
                .filter { (it is TileEntityChest && logChests.value) || (it is TileEntityShulkerBox && logShulkers.value) || (it is TileEntityDropper && logDroppers.value) || (it is TileEntityDispenser && logDispensers.value) }
                .forEach { logTileEntity(it) }

        chunkData.values.filter { it.hot }.forEach { chunkStats ->
            chunkStats.hot = false

            // mfw int array instead of Vec3i
            Waypoint.createWaypoint(chunkStats.getBlockPos(), chunkStats.toString())

            if (playSound.value) {
                mc.getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
            }

            if (logToChat.value) {
                val positionString = chunkStats.getPosition().joinToString { "$it" }
                MessageSendHelper.sendChatMessage("$chatName $positionString $chunkStats")
            }
        }
    }
}