package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TimerUtils
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
    private val range = register(Settings.integerBuilder("Range").withValue(8).withRange(4, 16).withStep(1))
    private val filled = register(Settings.b("Filled", true))
    private val outline = register(Settings.b("Outline", true))
    private val hideOwn = register(Settings.b("HideOwn", true))
    private val r1 = register(Settings.integerBuilder("Red(Obby)").withValue(208).withRange(0, 255).withStep(1).withVisibility { shouldAddObsidian() })
    private val g1 = register(Settings.integerBuilder("Green(Obby)").withValue(144).withRange(0, 255).withStep(1).withVisibility { shouldAddObsidian() })
    private val b1 = register(Settings.integerBuilder("Blue(Obby)").withValue(255).withRange(0, 255).withStep(1).withVisibility { shouldAddObsidian() })
    private val r2 = register(Settings.integerBuilder("Red(Bedrock)").withValue(144).withRange(0, 255).withStep(1).withVisibility { shouldAddBedrock() })
    private val g2 = register(Settings.integerBuilder("Green(Bedrock)").withValue(144).withRange(0, 255).withStep(1).withVisibility { shouldAddBedrock() })
    private val b2 = register(Settings.integerBuilder("Blue(Bedrock)").withValue(255).withRange(0, 255).withStep(1).withVisibility { shouldAddBedrock() })
    private val aFilled = register(Settings.integerBuilder("FilledAlpha").withValue(31).withRange(0, 255).withStep(1).withVisibility { filled.value })
    private val aOutline = register(Settings.integerBuilder("OutlineAlpha").withValue(127).withRange(0, 255).withStep(1).withVisibility { outline.value })
    private val renderMode = register(Settings.e<Mode>("Mode", Mode.BLOCK_HOLE))
    private val holeType = register(Settings.e<HoleType>("HoleType", HoleType.BOTH))

    private enum class Mode {
        BLOCK_HOLE, BLOCK_FLOOR, FLAT
    }

    private enum class HoleType {
        OBSIDIAN, BEDROCK, BOTH
    }

    private val renderer = ESPRenderer()
    private val timer = TimerUtils.TickTimer()

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