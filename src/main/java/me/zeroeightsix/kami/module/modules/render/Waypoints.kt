package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.FileInstanceManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.ColourConverter
import me.zeroeightsix.kami.util.KamiTessellator
import me.zeroeightsix.kami.util.WaypointInfo
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11
import java.util.function.Consumer

/**
 * @author Guacamole
 * Created by Guacamole on 31/07/20
 */
@Module.Info(
        name = "Waypoints",
        description = "Allows rendering of waypoints set using waypoint command",
        category = Module.Category.RENDER
)
class Waypoints : Module() {
    private val page = register(Settings.e<Page>("Page", Page.GENERIC_SETTINGS))
    /* Generic Settings */
    private val radius = register(Settings.doubleBuilder("Scale").withMinimum(1.0).withMaximum(16.0).withValue(6.0).withVisibility { page.value == Page.GENERIC_SETTINGS }.build())
    private val tracers = register(Settings.booleanBuilder("Tracers").withValue(false).withVisibility { page.value == Page.GENERIC_SETTINGS }.build())
    private val customTracerColor = register(Settings.booleanBuilder("CustomTracerColor").withValue(true).withVisibility { page.value == Page.GENERIC_SETTINGS }.build())
    private val tr = register(Settings.integerBuilder("Red").withMinimum(0).withValue(155).withMaximum(255).withVisibility { customTracerColor.value && page.value == Page.GENERIC_SETTINGS }.build())
    private val tg = register(Settings.integerBuilder("Green").withMinimum(0).withValue(144).withMaximum(255).withVisibility { customTracerColor.value && page.value == Page.GENERIC_SETTINGS }.build())
    private val tb = register(Settings.integerBuilder("Blue").withMinimum(0).withValue(255).withMaximum(255).withVisibility { customTracerColor.value && page.value == Page.GENERIC_SETTINGS }.build())
    private val customFontColor = register(Settings.booleanBuilder("CustomFontColor").withValue(false).withVisibility { page.value == Page.GENERIC_SETTINGS }.build())
    private val fr = register(Settings.integerBuilder("Red").withMinimum(0).withValue(155).withMaximum(255).withVisibility { customFontColor.value && page.value == Page.GENERIC_SETTINGS }.build())
    private val fg = register(Settings.integerBuilder("Green").withMinimum(0).withValue(144).withMaximum(255).withVisibility { customFontColor.value && page.value == Page.GENERIC_SETTINGS }.build())
    private val fb = register(Settings.integerBuilder("Blue").withMinimum(0).withValue(255).withMaximum(255).withVisibility { customFontColor.value && page.value == Page.GENERIC_SETTINGS }.build())

    /* Waypoint Types */
    private val t0 = register(Settings.booleanBuilder("Normal").withValue(true).withVisibility { page.value == Page.WAYPOINT_TYPES }.build())
    private val t1 = register(Settings.booleanBuilder("LogoutSpot").withValue(false).withVisibility { page.value == Page.WAYPOINT_TYPES }.build())
    private val t2 = register(Settings.booleanBuilder("Stash").withValue(false).withVisibility { page.value == Page.WAYPOINT_TYPES }.build())
    private val t3 = register(Settings.booleanBuilder("TeleportSpot").withValue(false).withVisibility { page.value == Page.WAYPOINT_TYPES }.build())
    private val t5 = register(Settings.booleanBuilder("Death").withValue(false).withVisibility { page.value == Page.WAYPOINT_TYPES }.build())
    private val t4 = register(Settings.booleanBuilder("Other").withValue(false).withVisibility { page.value == Page.WAYPOINT_TYPES }.build())

    private enum class Page {
        GENERIC_SETTINGS, WAYPOINT_TYPES
    }

    override fun onWorldRender(event: RenderEvent) {
        val waypoints = FileInstanceManager.waypoints

        val viewerYaw = mc.getRenderManager().playerViewY
        val viewerPitch = mc.getRenderManager().playerViewX
        val isThirdPersonFrontal = try { mc.getRenderManager().options.thirdPersonView == 2 } catch (ignored: NullPointerException) { false }
        val maxDistance = (mc.getRenderManager().options?.renderDistanceChunks ?: 10) * 16
        var zoomer2 = maxDistance / radius.value

        waypoints.forEach(Consumer { waypoint: WaypointInfo ->
            if (waypoint.wpType == 0 && !t0.value) return@Consumer
            else if (waypoint.wpType == 1 && !t1.value) return@Consumer
            else if (waypoint.wpType == 2 && !t2.value) return@Consumer
            else if (waypoint.wpType == 3 && !t3.value) return@Consumer
            else if (waypoint.wpType == 4 && !t4.value) return@Consumer
            else if (waypoint.wpType == 5 && !t5.value) return@Consumer

            GlStateManager.pushMatrix()
            GlStateManager.disableLighting()

            val str = "${waypoint.name}"
            val x = waypoint.pos.getX().toDouble()
            val y = waypoint.pos.getY().toDouble()
            val z = waypoint.pos.getZ().toDouble()
            var offX = x - mc.getRenderManager().renderPosX
            var offY = y - mc.getRenderManager().renderPosY
            var offZ = z - mc.getRenderManager().renderPosZ
            val dist = mc.player.getDistance(x, y, z)
            val zoomer3 = maxDistance / dist
            if (dist > radius.value) {
                offX *= zoomer3
                offY *= zoomer3
                offY += 1.6f * (1.0f - zoomer3)
                offZ *= zoomer3
            } else {
                offX *= zoomer2
                offY *= zoomer2
                offY += 1.6f * (1.0f - zoomer2)
                offZ *= zoomer2
            }
            GlStateManager.translate(offX, offY, offZ)
            GlStateManager.rotate(-viewerYaw, 0.0f, 1.0f, 0.0f)
            GlStateManager.rotate((if (isThirdPersonFrontal) -1 else 1).toFloat() * viewerPitch, 1.0f, 0.0f, 0.0f)
            GlStateManager.scale(-0.025f, -0.025f, 0.025f)
            GlStateManager.scale(zoomer2, zoomer2, zoomer2)
            val fontRendererIn = mc.fontRenderer
            val i: Int = fontRendererIn.getStringWidth(str) / 2
            GlStateManager.enableBlend()
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
            GlStateManager.disableTexture2D()

            val tessellator = Tessellator.getInstance()
            val bufferbuilder = tessellator.buffer

            GlStateManager.disableDepth()
            GL11.glTranslatef(0f, -20f, 0f)
            bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
            bufferbuilder.pos(-i - 1.toDouble(), 8.0, 0.0).color(0.0f, 0.0f, 0.0f, 0.5f).endVertex()
            bufferbuilder.pos(-i - 1.toDouble(), 19.0, 0.0).color(0.0f, 0.0f, 0.0f, 0.5f).endVertex()
            bufferbuilder.pos(i + 1.toDouble(), 19.0, 0.0).color(0.0f, 0.0f, 0.0f, 0.5f).endVertex()
            bufferbuilder.pos(i + 1.toDouble(), 8.0, 0.0).color(0.0f, 0.0f, 0.0f, 0.5f).endVertex()
            tessellator.draw()

            bufferbuilder.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR)
            bufferbuilder.pos(-i - 1.toDouble(), 8.0, 0.0).color(.1f, .1f, .1f, .1f).endVertex()
            bufferbuilder.pos(-i - 1.toDouble(), 19.0, 0.0).color(.1f, .1f, .1f, .1f).endVertex()
            bufferbuilder.pos(i + 1.toDouble(), 19.0, 0.0).color(.1f, .1f, .1f, .1f).endVertex()
            bufferbuilder.pos(i + 1.toDouble(), 8.0, 0.0).color(.1f, .1f, .1f, .1f).endVertex()
            tessellator.draw()

            GlStateManager.enableTexture2D()
            GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f)
            fontRendererIn.drawString(str, -i, 10, if (customFontColor.value) ColourConverter.rgbToInt(fr.value, fg.value, fb.value, 255) else ColourConverter.rgbToInt(155, 144, 255, 255))
            if (tracers.value) KamiTessellator.drawLineToPos(x, y, z, if (customTracerColor.value) ColourConverter.rgbToInt(tr.value, tg.value, tb.value, 255) else ColourConverter.rgbToInt(155, 144, 255, 255), 255F)
            GlStateManager.glNormal3f(0.0f, 0.0f, 0.0f)
            GL11.glTranslatef(0f, 20f, 0f)

            GlStateManager.scale(-40f, -40f, 40f)
            GlStateManager.enableDepth()
            GlStateManager.popMatrix()

            RenderHelper.disableStandardItemLighting()
            GlStateManager.enableLighting()
        })
    }
}