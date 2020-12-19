package me.zeroeightsix.kami.util.graphics

import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.*

object GlStateUtils {

    @JvmStatic
    var colorLock = false
        private set

    @JvmStatic
    fun useVbo(): Boolean {
        return Wrapper.minecraft.gameSettings.useVbo
    }

    @JvmStatic
    fun alpha(state: Boolean) {
        if (state) {
            GlStateManager.enableAlpha()
        } else {
            GlStateManager.disableAlpha()
        }
    }

    @JvmStatic
    fun blend(state: Boolean) {
        if (state) {
            GlStateManager.enableBlend()
            GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        } else {
            GlStateManager.disableBlend()
        }
    }

    @JvmStatic
    fun smooth(state: Boolean) {
        if (state) {
            GlStateManager.shadeModel(GL_SMOOTH)
        } else {
            GlStateManager.shadeModel(GL_FLAT)
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
            GlStateManager.enableDepth()
        } else {
            GlStateManager.disableDepth()
        }
    }

    @JvmStatic
    fun texture2d(state: Boolean) {
        if (state) {
            GlStateManager.enableTexture2D()
        } else {
            GlStateManager.disableTexture2D()
        }
    }

    @JvmStatic
    fun cull(state: Boolean) {
        if (state) {
            GlStateManager.enableCull()
        } else {
            GlStateManager.disableCull()
        }
    }

    @JvmStatic
    fun lighting(state: Boolean) {
        if (state) {
            GlStateManager.enableLighting()
        } else {
            GlStateManager.disableLighting()
        }
    }

    @JvmStatic
    fun colorLock(state: Boolean) {
        colorLock = state
    }

    @JvmStatic
    fun resetTexParam() {
        GlStateManager.bindTexture(0)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 1000)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LOD, 1000)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_LOD, -1000)
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