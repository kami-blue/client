package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.combat.SurroundUtils
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.GeometryMasks
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import org.kamiblue.event.listener.listener

@Module.Info(
        name = "HoleESP",
        category = Module.Category.COMBAT,
        description = "Show safe holes for crystal pvp"
)
object HoleESP : Module() {
    private val range = setting("RenderDistance", 8, 4..16, 1)
    private val filled = setting("Filled", true)
    private val outline = setting("Outline", true)
    private val hideOwn = setting("HideOwn", true)
    private val r1 = setting("Red(Obby)", 208, 0..255, 1, { shouldAddObsidian() })
    private val g1 = setting("Green(Obby)", 144, 0..255, 1, { shouldAddObsidian() })
    private val b1 = setting("Blue(Obby)", 255, 0..255, 1, { shouldAddObsidian() })
    private val r2 = setting("Red(Bedrock)", 144, 0..255, 1, { shouldAddBedrock() })
    private val g2 = setting("Green(Bedrock)", 144, 0..255, 1, { shouldAddBedrock() })
    private val b2 = setting("Blue(Bedrock)", 255, 0..255, 1, { shouldAddBedrock() })
    private val aFilled = setting("FilledAlpha", 31, 0..255, 1, { filled.value })
    private val aOutline = setting("OutlineAlpha", 127, 0..255, 1, { outline.value })
    private val renderMode = setting("Mode", Mode.BLOCK_HOLE)
    private val holeType = setting("HoleType", HoleType.BOTH)

    private enum class Mode {
        BLOCK_HOLE, BLOCK_FLOOR, FLAT
    }

    private enum class HoleType {
        OBSIDIAN, BEDROCK, BOTH
    }

    private val renderer = ESPRenderer()
    private val timer = TickTimer()

    init {
        listener<RenderWorldEvent> {
            if (mc.world == null || mc.player == null) return@listener
            if (timer.tick(133L)) updateRenderer() // Avoid running this on a tick
            renderer.render(false)
        }
    }

    private fun updateRenderer() {
        renderer.clear()
        renderer.aFilled = if (filled.value) aFilled.value else 0
        renderer.aOutline = if (outline.value) aOutline.value else 0

        val colorObsidian = ColorHolder(r1.value, g1.value, b1.value)
        val colorBedrock = ColorHolder(r2.value, g2.value, b2.value)
        val playerPos = mc.player.positionVector.toBlockPos()
        val side = if (renderMode.value != Mode.FLAT) GeometryMasks.Quad.ALL
        else GeometryMasks.Quad.DOWN

        for (x in -range.value..range.value) for (y in -range.value..range.value) for (z in -range.value..range.value) {
            if (hideOwn.value && x == 0 && y == 0 && z == 0) continue
            val pos = playerPos.add(x, y, z)
            val holeType = SurroundUtils.checkHole(pos)
            if (holeType == SurroundUtils.HoleType.NONE) continue
            val renderPos = if (renderMode.value == Mode.BLOCK_FLOOR) pos.down() else pos

            if (holeType == SurroundUtils.HoleType.OBBY && shouldAddObsidian()) {
                renderer.add(renderPos, colorObsidian, side)
            }

            if (holeType == SurroundUtils.HoleType.BEDROCK && shouldAddBedrock()) {
                renderer.add(renderPos, colorBedrock, side)
            }
        }
    }

    private fun shouldAddObsidian() = holeType.value == HoleType.OBSIDIAN || holeType.value == HoleType.BOTH

    private fun shouldAddBedrock() = holeType.value == HoleType.BEDROCK || holeType.value == HoleType.BOTH

}