package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.ColourHolder
import me.zeroeightsix.kami.util.ESPRenderer
import net.minecraft.util.math.RayTraceResult.Type

@Module.Info(
        name = "SelectionHighlight",
        description = "Highlights object you are looking at",
        category = Module.Category.RENDER
)
class SelectionHighlight : Module() {
    val block: Setting<Boolean> = register(Settings.b("Block", true))
    private val entity = register(Settings.b("Entity", false))
    private val filled = register(Settings.b("Filled", true))
    private val outline = register(Settings.b("Outline", true))
    private val throughBlocks = register(Settings.b("ThroughBlocks", false))
    private val r = register(Settings.integerBuilder("Red").withMinimum(0).withValue(155).withMaximum(255).build())
    private val g = register(Settings.integerBuilder("Green").withMinimum(0).withValue(144).withMaximum(255).build())
    private val b = register(Settings.integerBuilder("Blue").withMinimum(0).withValue(255).withMaximum(255).build())
    private val aFilled = register(Settings.integerBuilder("FilledAlpha").withValue(63).withRange(0, 255).withVisibility { filled.value }.build())
    private val aOutline = register(Settings.integerBuilder("OutlineAlpha").withValue(200).withRange(0, 255).withVisibility { outline.value }.build())
    private val thickness = register(Settings.floatBuilder("LineThickness").withValue(2.0f).withRange(0.0f, 8.0f).build())

    override fun onWorldRender(event: RenderEvent) {
        val colour = ColourHolder(r.value, g.value, b.value)
        val renderer = ESPRenderer(event.partialTicks)
        renderer.aFilled = if (filled.value) aFilled.value else 0
        renderer.aOutline = if (outline.value) aOutline.value else 0
        renderer.through = throughBlocks.value
        renderer.thickness = thickness.value
        if (entity.value && mc.objectMouseOver.typeOfHit == Type.ENTITY) {
            renderer.add(mc.objectMouseOver.entityHit, colour)
        }
        if (block.value && mc.objectMouseOver.typeOfHit == Type.BLOCK) {
            val box = mc.world.getBlockState(mc.objectMouseOver.blockPos).getSelectedBoundingBox(mc.world, mc.objectMouseOver.blockPos)
                    ?: return
            renderer.add(box.grow(0.002), colour)
        }
        renderer.render()
    }
}