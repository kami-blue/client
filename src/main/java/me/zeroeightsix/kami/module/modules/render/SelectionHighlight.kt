package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.ESPRenderer
import me.zeroeightsix.kami.util.GeometryMasks
import me.zeroeightsix.kami.util.KamiTessellator
import me.zeroeightsix.kami.util.colourUtils.ColourHolder
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult.Type
import kotlin.math.floor

@Module.Info(
        name = "SelectionHighlight",
        description = "Highlights object you are looking at",
        category = Module.Category.RENDER
)
/**
 * @author Xiaro
 *
 * Created by Xiaro on 07/08/20
 * Updated by Xiaro on 24/08/20
 */
class SelectionHighlight : Module() {
    val block: Setting<Boolean> = register(Settings.b("Block", true))
    private val entity = register(Settings.b("Entity", false))
    private val hitSideOnly = register(Settings.b("HitSideOnly", false))
    private val throughBlocks = register(Settings.b("ThroughBlocks", false))
    private val filled = register(Settings.b("Filled", true))
    private val outline = register(Settings.b("Outline", true))
    private val r = register(Settings.integerBuilder("Red").withMinimum(0).withValue(155).withMaximum(255).build())
    private val g = register(Settings.integerBuilder("Green").withMinimum(0).withValue(144).withMaximum(255).build())
    private val b = register(Settings.integerBuilder("Blue").withMinimum(0).withValue(255).withMaximum(255).build())
    private val aFilled = register(Settings.integerBuilder("FilledAlpha").withValue(63).withRange(0, 255).withVisibility { filled.value }.build())
    private val aOutline = register(Settings.integerBuilder("OutlineAlpha").withValue(200).withRange(0, 255).withVisibility { outline.value }.build())
    private val thickness = register(Settings.floatBuilder("LineThickness").withValue(2.0f).withRange(0.0f, 8.0f).build())

    override fun onWorldRender(event: RenderEvent) {
        val eyePos = mc.player.getPositionEyes(KamiTessellator.pTicks())
        val eyeBlockPos = BlockPos(floor(eyePos.x), floor(eyePos.y), floor(eyePos.z))
        if (!mc.world.isAirBlock(eyeBlockPos) && !mc.player.isInLava && !mc.player.isInWater) return
        val colour = ColourHolder(r.value, g.value, b.value)
        val hitObject = mc.objectMouseOver ?: return
        val renderer = ESPRenderer()

        renderer.aFilled = if (filled.value) aFilled.value else 0
        renderer.aOutline = if (outline.value) aOutline.value else 0
        renderer.through = throughBlocks.value
        renderer.thickness = thickness.value
        renderer.fullOutline = true

        if (entity.value && hitObject.typeOfHit == Type.ENTITY) {
            val lookVec = mc.player.lookVec
            val sightEnd = eyePos.add(lookVec.scale(6.0))
            val hitSide = hitObject.entityHit.boundingBox.calculateIntercept(eyePos, sightEnd)!!.sideHit
            val side = if (hitSideOnly.value) GeometryMasks.FACEMAP[hitSide]!! else GeometryMasks.Quad.ALL
            renderer.add(hitObject.entityHit, colour, side)
        }

        if (block.value && hitObject.typeOfHit == Type.BLOCK) {
            val box = mc.world.getBlockState(hitObject.blockPos).getSelectedBoundingBox(mc.world, hitObject.blockPos)
                    ?: return
            val side = if (hitSideOnly.value) GeometryMasks.FACEMAP[hitObject.sideHit]!! else GeometryMasks.Quad.ALL
            renderer.add(box.grow(0.002), colour, side)
        }
        renderer.render(true)
    }
}