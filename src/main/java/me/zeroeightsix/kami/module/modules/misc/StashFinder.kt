package me.zeroeightsix.kami.module.modules.misc

import kotlin.math.roundToInt
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.CoordUtil
import me.zeroeightsix.kami.util.CoordUtil.coordsLogFilename
import me.zeroeightsix.kami.util.Coordinate
import me.zeroeightsix.kami.util.MessageSendHelper
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.init.SoundEvents
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.tileentity.TileEntityShulkerBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos

/**
 * @author Nucleus
 */
@Module.Info(
        name = "StashFinder",
        category = Module.Category.MISC,
        description = "Logs chests and shulkers around you."
)
class StashFinder : Module() {
    private val logChests = register(Settings.b("Chests"))
    private val chestDensity = register(Settings.integerBuilder("Min Chests").withMinimum(1).withMaximum(20).withValue(5).build())

    private val logShulkers = register(Settings.b("Shulkers"))
    private val shulkerDensity = register(Settings.integerBuilder("Min Shulkers").withMinimum(1).withMaximum(20).withValue(1).build())

    private val logToChat = register(Settings.b("Log To Chat"))
    private val playSound = register(Settings.b("Play Sound"))

    private data class ChunkStats(var chests: Int = 0, var shulkers: Int = 0, var hot: Boolean = false) {
        val tileEntities = mutableListOf<TileEntity>()

        fun add(tileEntity: TileEntity) {
            when (tileEntity) {
                is TileEntityChest -> chests++
                is TileEntityShulkerBox -> shulkers++
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
            return "($chests chests, $shulkers shulkers)"
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
        if (chunkStats.chests >= chestDensity.value || chunkStats.shulkers >= shulkerDensity.value) {
            chunkStats.hot = true
        }
    }

    override fun onUpdate() {
        super.onUpdate()

        mc.world.loadedTileEntityList
                .filter { (it is TileEntityChest && logChests.value) || (it is TileEntityShulkerBox && logShulkers.value) }
                .forEach { logTileEntity(it) }

        chunkData.values.filter { it.hot }.forEach { chunkStats ->
            chunkStats.hot = false

            // mfw int array instead of Vec3i
            CoordUtil.writeCoords(chunkStats.getBlockPos(), chunkStats.toString(), coordsLogFilename)

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