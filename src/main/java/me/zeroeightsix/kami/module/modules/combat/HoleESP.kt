package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.combat.SurroundUtils
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
    private val renderDistance = register(Settings.floatBuilder("RenderDistance").withValue(8.0f).withRange(0.0f, 32.0f).build())
    private val filled = register(Settings.b("Filled", true))
    private val outline = register(Settings.b("Outline", true))
    private val r1 = register(Settings.integerBuilder("Red(Obby)").withMinimum(0).withValue(208).withMaximum(255).withVisibility { shouldAddObby() }.build())
    private val g1 = register(Settings.integerBuilder("Green(Obby)").withMinimum(0).withValue(144).withMaximum(255).withVisibility { shouldAddObby() }.build())
    private val b1 = register(Settings.integerBuilder("Blue(Obby)").withMinimum(0).withValue(255).withMaximum(255).withVisibility { shouldAddObby() }.build())
    private val r2 = register(Settings.integerBuilder("Red(Bedrock)").withMinimum(0).withValue(144).withMaximum(255).withVisibility { shouldAddBedrock() }.build())
    private val g2 = register(Settings.integerBuilder("Green(Bedrock)").withMinimum(0).withValue(144).withMaximum(255).withVisibility { shouldAddBedrock() }.build())
    private val b2 = register(Settings.integerBuilder("Blue(Bedrock)").withMinimum(0).withValue(255).withMaximum(255).withVisibility { shouldAddBedrock() }.build())
    private val aFilled = register(Settings.integerBuilder("FilledAlpha").withMinimum(0).withValue(31).withMaximum(255).withVisibility { filled.value }.build())
    private val aOutline = register(Settings.integerBuilder("OutlineAlpha").withMinimum(0).withValue(127).withMaximum(255).withVisibility { outline.value }.build())
    private val renderMode = register(Settings.e<Mode>("Mode", Mode.BLOCK_HOLE))
    private val holeType = register(Settings.e<HoleType>("HoleType", HoleType.BOTH))

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

    override fun onUpdate(event: SafeTickEvent) {
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

    override fun onWorldRender(event: RenderWorldEvent) {
        if (mc.player == null || safeHoles.isEmpty()) return
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