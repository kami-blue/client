package me.zeroeightsix.kami.util.graphics

import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.OpenGlHelper
import org.lwjgl.opengl.GL11.*

object GlStateUtils {

    @JvmStatic
    fun useVbo(): Boolean {
        return Wrapper.minecraft.gameSettings.useVbo
    }

    @JvmStatic
    fun blend(state: Boolean) {
        if (state) {
            glEnable(GL_BLEND)
            OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        } else {
            glDisable(GL_BLEND)
        }
    }

    @JvmStatic
    fun smooth(state: Boolean) {
        if (state) {
            glShadeModel(GL_SMOOTH)
        } else {
            glShadeModel(GL_FLAT)
        }
    }

    @JvmStatic
    fun lineSmooth(state: Boolean) {
        if (state) {
            glEnable(GL_LINE_SMOOTH)
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        } else {
            glDisable(GL_LINE_SMOOTH)
        }
    }

    @JvmStatic
    fun depth(state: Boolean) {
        if (state) {
            glEnable(GL_DEPTH_TEST)
        } else {
            glDisable(GL_DEPTH_TEST)
        }
    }

    @JvmStatic
    fun texture2d(state: Boolean) {
        if (state) {
            glEnable(GL_TEXTURE_2D)
        } else {
            glDisable(GL_TEXTURE_2D)
        }
    }

    @JvmStatic
    fun cull(state: Boolean) {
        if (state) {
            glEnable(GL_CULL_FACE)
        } else {
            glDisable(GL_CULL_FACE)
        }
    }

    @JvmStatic
    fun rescaleActual() {
        rescale(Wrapper.minecraft.displayWidth.toDouble(), Wrapper.minecraft.displayHeight.toDouble())
    }

    @JvmStatic
    fun rescaleKami() {
        val guiScale = DisplayGuiScreen.getScale()
        rescale(Wrapper.minecraft.displayWidth / guiScale, Wrapper.minecraft.displayHeight / guiScale)
    }

    @JvmStatic
    fun rescaleMc() {
        val resolution = ScaledResolution(Wrapper.minecraft)
        rescale(resolution.scaledWidth_double, resolution.scaledHeight_double)
    }

    @JvmStatic
    fun rescale(width: Double, height: Double) {
        glClear(256)
        glMatrixMode(5889)
        glLoadIdentity()
        glOrtho(0.0, width, height, 0.0, 1000.0, 3000.0)
        glMatrixMode(5888)
        glLoadIdentity()
        glTranslated(0.0, 0.0, -2000.0)
    }
}