package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.*
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11

/**
 * Created by 086 on 11/12/2017.
 *
 *
 * Kurisu Makise is best girl
 */
@Module.Info(
        name = "Tracers",
        description = "Draws lines to other living entities",
        category = Module.Category.RENDER
)
class Tracers : Module() {
    private val players = register(Settings.b("Players", true))
    private val friends = register(Settings.b("Friends", true))
    private val animals = register(Settings.b("Animals", false))
    private val mobs = register(Settings.b("Mobs", false))
    private val range = register(Settings.d("Range", 200.0))
    private val renderInvis = register(Settings.b("Invisible", false))
    private val customColours = register(Settings.booleanBuilder("Custom Colours").withValue(true).build())
    private val opacity = register(Settings.floatBuilder("Opacity").withRange(0f, 1f).withValue(1f).build())
    private val r = register(Settings.integerBuilder("Red").withMinimum(0).withValue(155).withMaximum(255).withVisibility { customColours.value }.build())
    private val g = register(Settings.integerBuilder("Green").withMinimum(0).withValue(144).withMaximum(255).withVisibility { customColours.value }.build())
    private val b = register(Settings.integerBuilder("Blue").withMinimum(0).withValue(255).withMaximum(255).withVisibility { customColours.value }.build())
    private var cycler = HueCycler(3600)
    override fun onWorldRender(event: RenderEvent) {
        GlStateManager.pushMatrix()
        Minecraft.getMinecraft().world.loadedEntityList.stream()
                .filter { e: Entity? -> EntityUtil.isLiving(e) }
                .filter { entity: Entity ->
                    if (entity.isInvisible) {
                        return@filter renderInvis.value
                    }
                    true
                }
                .filter { entity: Entity? -> !EntityUtil.isFakeLocalPlayer(entity) }
                .filter { entity: Entity -> if (entity is EntityPlayer) players.value && mc.player !== entity else if (EntityUtil.isPassive(entity)) animals.value else mobs.value }
                .filter { entity: Entity? -> mc.player.getDistance(entity) < range.value }
                .forEach { entity: Entity ->
                    var colour = getColour(entity)
                    colour = if (colour == ColourUtils.Colors.RAINBOW) {
                        if (!friends.value) return@forEach
                        if (customColours.value) {
                            ColourConverter.rgbToInt(r.value, g.value, b.value, (opacity.value * 255f).toInt())
                        } else {
                            cycler.current()
                        }
                    } else {
                        cycler.current()
                    }
                    val r = (colour ushr 16 and 0xFF) / 255f
                    val g = (colour ushr 8 and 0xFF) / 255f
                    val b = (colour and 0xFF) / 255f
                    drawLineToEntity(entity, r, g, b, opacity.value)
                }
        GlStateManager.popMatrix()
    }

    override fun onUpdate() {
        cycler.next()
    }

    private fun drawRainbowToEntity(entity: Entity, opacity: Float) {
        val eyes = Vec3d(0.0, 0.0, 1.0)
                .rotatePitch((-Math
                        .toRadians(Minecraft.getMinecraft().player.rotationPitch.toDouble())).toFloat())
                .rotateYaw((-Math
                        .toRadians(Minecraft.getMinecraft().player.rotationYaw.toDouble())).toFloat())
        val xyz = interpolate(entity)
        val posx = xyz[0]
        val posy = xyz[1]
        val posz = xyz[2]
        val posx2 = eyes.x
        val posy2 = eyes.y + mc.player.getEyeHeight()
        val posz2 = eyes.z
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glLineWidth(1.5f)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(false)
        cycler.reset()
        cycler.setNext(opacity)
        GlStateManager.disableLighting()
        GL11.glLoadIdentity()
        mc.entityRenderer.orientCamera(mc.renderPartialTicks)
        GL11.glBegin(GL11.GL_LINES)
        run {
            GL11.glVertex3d(posx, posy, posz)
            GL11.glVertex3d(posx2, posy2, posz2)
            cycler.setNext(opacity)
            GL11.glVertex3d(posx2, posy2, posz2)
            GL11.glVertex3d(posx2, posy2, posz2)
        }
        GL11.glEnd()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glColor3d(1.0, 1.0, 1.0)
        GlStateManager.enableLighting()
    }

    private fun getColour(entity: Entity): Int {
        return if (entity is EntityPlayer) {
            if (Friends.isFriend(entity.getName())) ColourUtils.Colors.RAINBOW else ColourUtils.Colors.WHITE
        } else {
            if (EntityUtil.isPassive(entity)) ColourUtils.Colors.GREEN else ColourUtils.Colors.RED
        }
    }

    companion object {
        private fun interpolate(now: Double, then: Double): Double {
            return then + (now - then) * mc.renderPartialTicks
        }

        fun interpolate(entity: Entity): DoubleArray {
            val posX = interpolate(entity.posX, entity.lastTickPosX) - mc.getRenderManager().renderPosX
            val posY = interpolate(entity.posY, entity.lastTickPosY) - mc.getRenderManager().renderPosY
            val posZ = interpolate(entity.posZ, entity.lastTickPosZ) - mc.getRenderManager().renderPosZ
            return doubleArrayOf(posX, posY, posZ)
        }

        fun drawLineToEntity(e: Entity, red: Float, green: Float, blue: Float, opacity: Float) {
            val xyz = interpolate(e)
            drawLine(xyz[0], xyz[1], xyz[2], e.height.toDouble(), red, green, blue, opacity)
        }

        private fun drawLine(posx: Double, posy: Double, posz: Double, up: Double, red: Float, green: Float, blue: Float, opacity: Float) {
            val eyes = Vec3d(0.0, 0.0, 1.0)
                    .rotatePitch((-Math
                            .toRadians(Minecraft.getMinecraft().player.rotationPitch.toDouble())).toFloat())
                    .rotateYaw((-Math
                            .toRadians(Minecraft.getMinecraft().player.rotationYaw.toDouble())).toFloat())
            drawLineFromPosToPos(eyes.x, eyes.y + mc.player.getEyeHeight(), eyes.z, posx, posy, posz, up, red, green, blue, opacity)
        }

        @JvmStatic
        fun drawLineFromPosToPos(posx: Double, posy: Double, posz: Double, posx2: Double, posy2: Double, posz2: Double, up: Double, red: Float, green: Float, blue: Float, opacity: Float) {
            GL11.glBlendFunc(770, 771)
            GL11.glLineWidth(1.5f)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glDepthMask(false)
            GL11.glColor4f(red, green, blue, opacity)
            GlStateManager.disableLighting()
            GL11.glLoadIdentity()
            mc.entityRenderer.orientCamera(mc.renderPartialTicks)
            GL11.glBegin(GL11.GL_LINES)
            run {
                GL11.glVertex3d(posx, posy, posz)
                GL11.glVertex3d(posx2, posy2, posz2)
                GL11.glVertex3d(posx2, posy2, posz2)
                GL11.glVertex3d(posx2, posy2 + up, posz2)
            }
            GL11.glEnd()
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDepthMask(true)
            GL11.glColor3d(1.0, 1.0, 1.0)
            GlStateManager.enableLighting()
        }
    }
}