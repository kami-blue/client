package me.zeroeightsix.kami.module.modules.render

import kotlinx.coroutines.launch
import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.GeometryMasks
import me.zeroeightsix.kami.util.math.VectorUtils.distanceTo
import me.zeroeightsix.kami.util.threads.defaultScope
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos

object VoidESP : Module(
    name = "VoidESP",
    description = "Highlights holes leading to the void",
    category = Category.RENDER
) {
    private val filled by setting("Filled", true)
    private val outline by setting("Outline", true)
    private val color by setting("Color", ColorHolder(148, 161, 255), false)
    private val aFilled by setting("FilledAlpha", 127, 0..255, 1)
    private val aOutline by setting("OutlineAlpha", 255, 0..255, 1)
    private val renderMode by setting("Mode", Mode.BLOCK_HOLE)
    private val range by setting("Range", 8, 4..32, 1)

    @Suppress("UNUSED")
    private enum class Mode {
        BLOCK_HOLE, BLOCK_VOID, FLAT
    }

    private val renderer = ESPRenderer()
    private val timer = TickTimer()

    init {
        safeListener<RenderWorldEvent> {
            if (timer.tick(133L)) { // Avoid running this on a tick
                updateRenderer()
            }
            renderer.render(false)
        }
    }

    private fun SafeClientEvent.updateRenderer() {
        renderer.aFilled = if (filled) aFilled else 0
        renderer.aOutline = if (outline) aOutline else 0

        val color = color.clone()
        val side = if (renderMode != Mode.FLAT) GeometryMasks.Quad.ALL else GeometryMasks.Quad.DOWN

        defaultScope.launch {
            val cached = ArrayList<Triple<AxisAlignedBB, ColorHolder, Int>>()

            for (x in -range..range) for (z in -range..range) {
                val pos = BlockPos(player.posX + x, 0.0, player.posZ + z)
                if (player.distanceTo(pos) > range) continue
                if (!isVoid(pos)) continue

                val renderPos = if (renderMode == Mode.BLOCK_VOID) pos.down() else pos
                cached.add(Triple(AxisAlignedBB(renderPos), color, side))
            }

            renderer.replaceAll(cached)
        }
    }

    private fun SafeClientEvent.isVoid(pos: BlockPos) = world.isAirBlock(pos)
        && world.isAirBlock(pos.up())
        && world.isAirBlock(pos.up().up())

}
