package me.zeroeightsix.kami.module.modules.misc

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.manager.managers.WaypointManager
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.movement.AutoWalk
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.math.CoordinateConverter.asString
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.defaultScope
import me.zeroeightsix.kami.util.threads.onMainThread
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.init.SoundEvents
import net.minecraft.tileentity.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashMap
import kotlin.math.roundToInt

internal object StashLogger : Module(
    name = "StashLogger",
    category = Category.MISC,
    description = "Logs storage units in render distance."
) {
    private val saveToFile by setting("SaveToFile", true)
    private val logToChat by setting("LogToChat", true)
    private val playSound by setting("PlaySound", true)
    private val logChests by setting("Chests", true)
    private val chestDensity by setting("MinChests", 5, 1..20, 1, { logChests })
    private val logShulkers by setting("Shulkers", true)
    private val shulkerDensity by setting("MinShulkers", 1, 1..20, 1, { logShulkers })
    private val logDroppers by setting("Droppers", true)
    private val dropperDensity by setting("MinDroppers", 5, 1..20, 1, { logDroppers })
    private val logDispensers by setting("Dispensers", true)
    private val dispenserDensity by setting("MinDispensers", 5, 1..20, 1, { logDispensers })
    private val logHoppers by setting("Hoppers", true)
    private val hopperDensity by setting("MinHoppers", 5, 1..20, 1, { logHoppers })
    private val disableAutoWalk by setting("DisableAutoWalk", false, description = "Disables AutoWalk when a stash is found")
    private val cancelBaritone by setting("CancelBaritone", false, description = "Cancels Baritone when a stash is found")

    private val chunkData = LinkedHashMap<Long, ChunkStats>()
    private val knownPositions = HashSet<BlockPos>()
    private val timer = TickTimer(TimeUnit.SECONDS)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END || !timer.tick(3L)) return@safeListener

            defaultScope.launch {
                coroutineScope {
                    launch {
                        world.loadedTileEntityList.toList().forEach(::logTileEntity)
                        notification()
                    }
                }
            }
        }
    }

    private fun notification() {
        var found = false

        for (chunkStats in chunkData.values) {
            if (!chunkStats.hot) continue

            chunkStats.hot = false
            val center = chunkStats.center()
            val string = chunkStats.toString()

            if (saveToFile) {
                WaypointManager.add(center, string)
            }

            if (playSound) {
                onMainThread {
                    mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                }
            }

            if (logToChat) {
                val positionString = center.asString()
                MessageSendHelper.sendChatMessage("$chatName $positionString $string")
            }

            found = found || true
        }

        if (found) {
            if (disableAutoWalk && AutoWalk.isEnabled) AutoWalk.disable()
            if (cancelBaritone && (BaritoneUtils.isPathing || BaritoneUtils.isActive)) BaritoneUtils.cancelEverything()
        }
    }

    private fun logTileEntity(tileEntity: TileEntity) {
        if (!checkTileEntityType(tileEntity)) return
        if (!knownPositions.add(tileEntity.pos)) return

        val chunk = ChunkPos.asLong(tileEntity.pos.x shr 4, tileEntity.pos.z shr 4)
        val chunkStats = chunkData.getOrPut(chunk, ::ChunkStats)

        chunkStats.add(tileEntity)
    }

    private fun checkTileEntityType(tileEntity: TileEntity) =
        logChests && tileEntity is TileEntityChest
            || logShulkers && tileEntity is TileEntityShulkerBox
            || logDroppers && tileEntity is TileEntityDropper
            || logDispensers && tileEntity is TileEntityDispenser
            || logHoppers && tileEntity is TileEntityHopper

    private class ChunkStats {
        var chests = 0; private set
        var shulkers = 0; private set
        var droppers = 0; private set
        var dispensers = 0; private set
        var hoppers = 0; private set

        var hot = false

        private val tileEntities = Collections.synchronizedList(ArrayList<TileEntity>())

        fun add(tileEntity: TileEntity) {
            when (tileEntity) {
                is TileEntityChest -> chests++
                is TileEntityShulkerBox -> shulkers++
                is TileEntityDropper -> droppers++
                is TileEntityDispenser -> dispensers++
                is TileEntityHopper -> hoppers++
                else -> return
            }

            tileEntities.add(tileEntity)

            if (chests >= chestDensity
                || shulkers >= shulkerDensity
                || droppers >= dropperDensity
                || dispensers >= dispenserDensity
                || hoppers >= hopperDensity) {
                hot = true
            }
        }

        fun center(): BlockPos {
            var x = 0.0
            var y = 0.0
            var z = 0.0
            val size = tileEntities.size

            for (tileEntity in tileEntities) {
                x += tileEntity.pos.x
                y += tileEntity.pos.y
                z += tileEntity.pos.z
            }

            x /= size
            y /= size
            z /= size

            return BlockPos(x.roundToInt(), y.roundToInt(), z.roundToInt())
        }

        override fun toString(): String {
            return "($chests chests, $shulkers shulkers, $droppers droppers, $dispensers dispensers, $hoppers hoppers)"
        }
    }
}
