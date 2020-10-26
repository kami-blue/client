package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.combat.SurroundUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.GeometryMasks
import me.zeroeightsix.kami.util.math.VectorUtils
import net.minecraft.util.math.BlockPos
import java.util.concurrent.ConcurrentHashMap

@Module.Info(
        name = "HoleESP",
        category = Module.Category.COMBAT,
        description = "Show safe holes for crystal pvp"
)
object HoleESP : Module() {
    private val renderDistance = setting("RenderDistance", 8.0f, 0.0f..32.0f, 0.5f)
    private val filled = setting("Filled", true)
    private val outline = setting("Outline", true)
    private val r1 = setting("Red(Obby)", 208, 0..255, 1, { shouldAddObby() })
    private val g1 = setting("Green(Obby)", 144, 0..255, 1, { shouldAddObby() })
    private val b1 = setting("Blue(Obby)", 255, 0..255, 1, { shouldAddObby() })
    private val r2 = setting("Red(Bedrock)", 144, 0..255, 1, { shouldAddBedrock() })
    private val g2 = setting("Green(Bedrock)", 144, 0..255, 1, { shouldAddBedrock() })
    private val b2 = setting("Blue(Bedrock)", 255, 0..255, 1, { shouldAddBedrock() })
    private val aFilled = setting("FilledAlpha", 31, 0..255, 1, { filled.value })
    private val aOutline = setting("OutlineAlpha", 127, 0..255, 1, { outline.value })
    private val renderMode = setting("Mode", Mode.BLOCK_HOLE)
    private val holeType = setting("HoleType", HoleType.BOTH)

    private val safeHoles = ConcurrentHashMap<BlockPos, ColorHolder>()

    private enum class Mode {
        BLOCK_HOLE, BLOCK_FLOOR, FLAT
    }

    private enum class HoleType {
        OBBY, BEDROCK, BOTH
    }

    private fun shouldAddObby(): Boolean {
        return holeType.value == HoleType.OBBY || holeType.value == HoleType.BOTH
    }

    private fun shouldAddBedrock(): Boolean {
        return holeType.value == HoleType.BEDROCK || holeType.value == HoleType.BOTH
    }

    init {
        listener<SafeTickEvent> {
            safeHoles.clear()
            val blockPosList = VectorUtils.getBlockPosInSphere(mc.player.positionVector, renderDistance.value)
            for (pos in blockPosList) {
                val holeType = SurroundUtils.checkHole(pos)
                if (holeType == SurroundUtils.HoleType.NONE) continue

                if (holeType == SurroundUtils.HoleType.OBBY && shouldAddObby()) {
                    safeHoles[pos] = ColorHolder(r1.value, g1.value, b1.value)
                }
                if (holeType == SurroundUtils.HoleType.BEDROCK && shouldAddBedrock()) {
                    safeHoles[pos] = ColorHolder(r2.value, g2.value, b2.value)
                }
            }
        }

        listener<RenderWorldEvent> {
            if (mc.player == null || safeHoles.isEmpty()) return@listener
            val side = if (renderMode.value != Mode.FLAT) GeometryMasks.Quad.ALL
            else GeometryMasks.Quad.DOWN
            val renderer = ESPRenderer()
            renderer.aFilled = if (filled.value) aFilled.value else 0
            renderer.aOutline = if (outline.value) aOutline.value else 0
            for ((pos, colour) in safeHoles) {
                val renderPos = if (renderMode.value == Mode.BLOCK_FLOOR) pos.down() else pos
                renderer.add(renderPos, colour, side)
            }
            renderer.render(true)
        }
    }
}