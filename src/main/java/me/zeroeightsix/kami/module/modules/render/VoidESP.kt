package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.GeometryMasks
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent

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
        listener<SafeTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@listener
            renderer.clear()
            renderer.aFilled = if (filled.value) aFilled.value else 0
            renderer.aOutline = if (outline.value) aOutline.value else 0
            val color = ColorHolder(r.value, g.value, b.value)
            val side = if (renderMode.value != Mode.FLAT) GeometryMasks.Quad.ALL else GeometryMasks.Quad.DOWN
            val squaredDist = renderDistance.value * renderDistance.value

            for (x in -renderDistance.value..renderDistance.value) for (z in -renderDistance.value..renderDistance.value) {
                val pos = BlockPos(mc.player.posX + x, 0.0, mc.player.posZ + z)
                if (mc.player.getDistanceSqToCenter(pos) > squaredDist) continue
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
