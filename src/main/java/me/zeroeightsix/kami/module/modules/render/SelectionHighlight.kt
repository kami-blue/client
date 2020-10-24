package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.GeometryMasks
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import net.minecraft.util.math.RayTraceResult.Type

@Module.Info(
        name = "SelectionHighlight",
        description = "Highlights object you are looking at",
        category = Module.Category.RENDER
)
object SelectionHighlight : Module() {
    val block = register(Settings.b("Block", true))
    private val entity = register(Settings.b("Entity", false))
    private val hitSideOnly = register(Settings.b("HitSideOnly", false))
    private val throughBlocks = register(Settings.b("ThroughBlocks", false))
    private val filled = register(Settings.b("Filled", true))
    private val outline = register(Settings.b("Outline", true))
    private val r = register(Settings.integerBuilder("Red").withValue(155).withRange(0, 255).withStep(1))
    private val g = register(Settings.integerBuilder("Green").withValue(144).withRange(0, 255).withStep(1))
    private val b = register(Settings.integerBuilder("Blue").withValue(255).withRange(0, 255).withStep(1))
    private val aFilled = register(Settings.integerBuilder("FilledAlpha").withValue(63).withRange(0, 255).withStep(1).withVisibility { filled.value })
    private val aOutline = register(Settings.integerBuilder("OutlineAlpha").withValue(200).withRange(0, 255).withStep(1).withVisibility { outline.value })
    private val thickness = register(Settings.floatBuilder("LineThickness").withValue(2.0f).withRange(0.25f, 5.0f).withStep(0.25f))

    private val renderer = ESPRenderer()

    init {
        listener<RenderWorldEvent> {
            val viewEntity = mc.renderViewEntity ?: mc.player ?: return@listener
            val eyePos = viewEntity.getPositionEyes(KamiTessellator.pTicks())
            if (!mc.world.isAirBlock(eyePos.toBlockPos())) return@listener
            val color = ColorHolder(r.value, g.value, b.value)
            val hitObject = mc.objectMouseOver ?: return@listener

            if (entity.value && hitObject.typeOfHit == Type.ENTITY) {
                val lookVec = viewEntity.lookVec
                val sightEnd = eyePos.add(lookVec.scale(6.0))
                val hitSide = hitObject.entityHit?.boundingBox?.calculateIntercept(eyePos, sightEnd)?.sideHit
                val side = (if (hitSideOnly.value) GeometryMasks.FACEMAP[hitSide] else GeometryMasks.Quad.ALL)?: return@listener
                renderer.add(hitObject.entityHit, color, side)
            }

            if (block.value && hitObject.typeOfHit == Type.BLOCK) {
                val blockState = mc.world.getBlockState(hitObject.blockPos)
                val box = blockState.getSelectedBoundingBox(mc.world, hitObject.blockPos) ?: return@listener
                val side = (if (hitSideOnly.value) GeometryMasks.FACEMAP[hitObject.sideHit] else GeometryMasks.Quad.ALL)?: return@listener
                renderer.add(box.grow(0.002), color, side)
            }
            renderer.render(true)
        }

        listener<SafeTickEvent> {
            renderer.aFilled = if (filled.value) aFilled.value else 0
            renderer.aOutline = if (outline.value) aOutline.value else 0
            renderer.through = throughBlocks.value
            renderer.thickness = thickness.value
            renderer.fullOutline = true
        }
    }
}