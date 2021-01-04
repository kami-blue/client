package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.GeometryMasks
import me.zeroeightsix.kami.util.math.VectorUtils.distanceTo
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener

@Module.Info(
        name = "VoidESP",
        description = "Highlights holes leading to the void",
        category = Module.Category.RENDER
)
object VoidESP : Module() {
    private val renderDistance = setting("RenderDistance", 6, 4..32, 1)
    private val filled = setting("Filled", true)
    private val outline = setting("Outline", true)
    private val r = setting("Red", 148, 0..255, 1)
    private val g = setting("Green", 161, 0..255, 1)
    private val b = setting("Blue", 255, 0..255, 1)
    private val aFilled = setting("FilledAlpha", 127, 0..255, 1)
    private val aOutline = setting("OutlineAlpha", 255, 0..255, 1)
    private val renderMode = setting("Mode", Mode.BLOCK_HOLE)

    @Suppress("UNUSED")
    private enum class Mode {
        BLOCK_HOLE, BLOCK_VOID, FLAT
    }

    private val renderer = ESPRenderer()

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@safeListener
            renderer.clear()
            renderer.aFilled = if (filled.value) aFilled.value else 0
            renderer.aOutline = if (outline.value) aOutline.value else 0
            val color = ColorHolder(r.value, g.value, b.value)
            val side = if (renderMode.value != Mode.FLAT) GeometryMasks.Quad.ALL else GeometryMasks.Quad.DOWN

            for (x in -renderDistance.value..renderDistance.value) for (z in -renderDistance.value..renderDistance.value) {
                val pos = BlockPos(player.posX + x, 0.0, player.posZ + z)
                if (player.distanceTo(pos) > renderDistance.value) continue
                if (!isVoid(pos)) continue
                val renderPos = if (renderMode.value == Mode.BLOCK_VOID) pos.down() else pos
                renderer.add(renderPos, color, side)
            }
        }

        listener<RenderWorldEvent> {
            renderer.render(false)
        }
    }

    private fun SafeClientEvent.isVoid(pos: BlockPos) = world.isAirBlock(pos)
            && world.isAirBlock(pos.up())
            && world.isAirBlock(pos.up().up())

}
