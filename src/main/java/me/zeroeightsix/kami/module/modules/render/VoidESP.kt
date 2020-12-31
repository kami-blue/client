package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.GeometryMasks
import me.zeroeightsix.kami.util.math.VectorUtils.distanceTo
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener

@Module.Info(
        name = "VoidESP",
        description = "Highlights holes leading to the void",
        category = Module.Category.RENDER
)
object VoidESP : Module() {
    private val renderDistance = register(Settings.integerBuilder("RenderDistance").withValue(6).withRange(4, 32).withStep(1))
    private val filled = register(Settings.b("Filled", true))
    private val outline = register(Settings.b("Outline", true))
    private val r = register(Settings.integerBuilder("Red").withValue(148).withRange(0, 255).withStep(1))
    private val g = register(Settings.integerBuilder("Green").withValue(161).withRange(0, 255).withStep(1))
    private val b = register(Settings.integerBuilder("Blue").withValue(255).withRange(0, 255).withStep(1))
    private val aFilled = register(Settings.integerBuilder("FilledAlpha").withValue(127).withRange(0, 255).withStep(1))
    private val aOutline = register(Settings.integerBuilder("OutlineAlpha").withValue(255).withRange(0, 255).withStep(1))
    private val renderMode = register(Settings.e<Mode>("Mode", Mode.BLOCK_HOLE))

    @Suppress("UNUSED")
    private enum class Mode {
        BLOCK_HOLE, BLOCK_VOID, FLAT
    }

    private val renderer = ESPRenderer()

    init {
        listener<SafeTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@listener
            renderer.clear()
            renderer.aFilled = if (filled.value) aFilled.value else 0
            renderer.aOutline = if (outline.value) aOutline.value else 0
            val color = ColorHolder(r.value, g.value, b.value)
            val side = if (renderMode.value != Mode.FLAT) GeometryMasks.Quad.ALL else GeometryMasks.Quad.DOWN

            for (x in -renderDistance.value..renderDistance.value) for (z in -renderDistance.value..renderDistance.value) {
                val pos = BlockPos(mc.player.posX + x, 0.0, mc.player.posZ + z)
                if (mc.player.distanceTo(pos) > renderDistance.value) continue
                if (!isVoid(pos)) continue
                val renderPos = if (renderMode.value == Mode.BLOCK_VOID) pos.down() else pos
                renderer.add(renderPos, color, side)
            }
        }

        listener<RenderWorldEvent> {
            renderer.render(false)
        }
    }

    private fun isVoid(pos: BlockPos) = mc.world.isAirBlock(pos)
            && mc.world.isAirBlock(pos.up())
            && mc.world.isAirBlock(pos.up().up())

}
