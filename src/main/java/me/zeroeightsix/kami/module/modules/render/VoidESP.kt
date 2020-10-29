package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.GeometryMasks
import me.zeroeightsix.kami.util.math.VectorUtils
import net.minecraft.util.math.BlockPos
import java.util.concurrent.ConcurrentHashMap

@Module.Info(
        name = "VoidESP",
        description = "Highlights holes leading to the void",
        category = Module.Category.RENDER
)
object VoidESP : Module() {
    private val renderDistance = register(Settings.floatBuilder("RenderDistance").withValue(8.0f).withRange(0.0f, 32.0f).build())
    private val filled = register(Settings.b("Filled", true))
    private val outline = register(Settings.b("Outline", true))
    private val r = register(Settings.integerBuilder("Red").withValue(255).withRange(0, 255).withStep(1))
    private val g = register(Settings.integerBuilder("Green").withValue(127).withRange(0, 255).withStep(1))
    private val b = register(Settings.integerBuilder("Blue").withValue(127).withRange(0, 255).withStep(1))
    private val aFilled = register(Settings.integerBuilder("FilledAlpha").withValue(127).withRange(0, 255).withStep(1))
    private val aOutline = register(Settings.integerBuilder("OutlineAlpha").withValue(255).withRange(0, 255).withStep(1))
    private val renderMode = register(Settings.e<Mode>("Mode", Mode.BLOCK_HOLE))

    private val voidHoles = ConcurrentHashMap<BlockPos, ColorHolder>()

    private enum class Mode {
        BLOCK_HOLE, BLOCK_VOID, FLAT
    }

    init {
        listener<SafeTickEvent> {
            voidHoles.clear()
            val blockPosList = VectorUtils.getBlockPosInSphere(mc.player.positionVector, renderDistance.value)
            for (pos in blockPosList) {
                val isVoid = pos.y == 0
                if (isVoid && mc.world.isAirBlock(pos) && mc.world.isAirBlock(pos.up()) && mc.world.isAirBlock(pos.up().up())) {
                    voidHoles[pos] = ColorHolder(r.value, g.value, b.value)
                }
            }
        }

        listener<RenderWorldEvent> {
            if (mc.player == null || voidHoles.isEmpty()) return@listener
            val side = if (renderMode.value != Mode.FLAT) GeometryMasks.Quad.ALL
            else GeometryMasks.Quad.DOWN
            val renderer = ESPRenderer()
            renderer.aFilled = if (filled.value) aFilled.value else 0
            renderer.aOutline = if (outline.value) aOutline.value else 0
            for ((pos, colour) in voidHoles) {
                val renderPos = if (renderMode.value == Mode.BLOCK_VOID) pos.down() else pos
                renderer.add(renderPos, colour, side)
            }
            renderer.render(true)
        }
    }
}
