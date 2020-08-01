package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.ColourHolder
import me.zeroeightsix.kami.util.EntityUtils.getTargetList
import me.zeroeightsix.kami.util.GeometryMasks
import me.zeroeightsix.kami.util.KamiTessellator
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.RayTraceResult
import org.lwjgl.opengl.GL11.*

/**
 * @author 086
 * Updated by Xiaro on 31/07/20
 */
@Module.Info(
        name = "EyeFinder",
        description = "Draw lines from entity's heads to where they are looking",
        category = Module.Category.RENDER
)
class EyeFinder : Module() {
    private val page = register(Settings.e<Page>("Page", Page.ENTITY_TYPE))

    /* Entity type settings */
    private val players = register(Settings.booleanBuilder("Players").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE }.build())
    private val friends = register(Settings.booleanBuilder("Friends").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && players.value }.build())
    private val sleeping = register(Settings.booleanBuilder("Sleeping").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && players.value }.build())
    private val mobs = register(Settings.booleanBuilder("Mobs").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE }.build())
    private val passive = register(Settings.booleanBuilder("PassiveMobs").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && mobs.value }.build())
    private val neutral = register(Settings.booleanBuilder("NeutralMobs").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && mobs.value }.build())
    private val hostile = register(Settings.booleanBuilder("HostileMobs").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && mobs.value }.build())
    private val range = register(Settings.integerBuilder("Range").withValue(64).withRange(1, 128).withVisibility { page.value == Page.ENTITY_TYPE }.build())

    /* Rendering settings */
    private val r = register(Settings.integerBuilder("Red").withValue(155).withRange(0, 255).withVisibility { page.value == Page.RENDERING }.build())
    private val g = register(Settings.integerBuilder("Green").withValue(144).withRange(0, 255).withVisibility { page.value == Page.RENDERING }.build())
    private val b = register(Settings.integerBuilder("Blue").withValue(255).withRange(0, 255).withVisibility { page.value == Page.RENDERING }.build())
    private val a = register(Settings.integerBuilder("Alpha").withValue(200).withRange(0, 255).withVisibility { page.value == Page.RENDERING }.build())
    private val thickness = register(Settings.floatBuilder("Thickness").withValue(2.0f).withRange(0.0f, 8.0f).withVisibility { page.value == Page.RENDERING }.build())


    private enum class Page {
        ENTITY_TYPE, RENDERING
    }

    private var entityList: Array<Entity>? = null

    override fun onWorldRender(event: RenderEvent) {
        if (entityList.isNullOrEmpty()) return
        for (entity in entityList!!) {
            drawLine(entity as EntityLivingBase)
        }
    }

    override fun onUpdate() {
        val player = arrayOf(players.value, friends.value, sleeping.value)
        val mob = arrayOf(mobs.value, passive.value, neutral.value, hostile.value)
        entityList = getTargetList(player, mob, true, false, range.value.toFloat())
    }

    private fun drawLine(entity: Entity) {
        val result = entity.rayTrace(6.0, Minecraft.getMinecraft().renderPartialTicks) ?: return
        val eyes = entity.getPositionEyes(mc.renderPartialTicks)
        val pos1 = eyes.subtract(mc.renderManager.renderPosX, mc.renderManager.renderPosY, mc.renderManager.renderPosZ)
        val pos2 = result.hitVec.subtract(mc.renderManager.renderPosX, mc.renderManager.renderPosY, mc.renderManager.renderPosZ)
        val colour = ColourHolder(r.value, g.value, b.value)

        /* Render line */
        val buffer = KamiTessellator.buffer
        GlStateManager.glLineWidth(thickness.value)
        KamiTessellator.begin(GL_LINES)
        buffer.pos(pos1.x, pos1.y, pos1.z).color(r.value, g.value, b.value, a.value).endVertex()
        buffer.pos(pos2.x, pos2.y, pos2.z).color(r.value, g.value, b.value, a.value).endVertex()
        KamiTessellator.render()

        /* Render hit position */
        if (result.typeOfHit != RayTraceResult.Type.MISS) {
            val box = if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
                AxisAlignedBB(result.blockPos).grow(0.002)
            } else {
                result.entityHit.renderBoundingBox
            }.offset(-mc.renderManager.renderPosX, -mc.renderManager.renderPosY, -mc.renderManager.renderPosZ)
            KamiTessellator.begin(GL_QUADS)
            KamiTessellator.drawBox(box, colour, a.value / 2, GeometryMasks.Quad.ALL)
            KamiTessellator.render()
            KamiTessellator.begin(GL_LINES)
            KamiTessellator.drawOutline(box, colour, a.value, thickness.value)
            KamiTessellator.render()
        }
    }
}