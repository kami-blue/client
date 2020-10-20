package me.zeroeightsix.kami.util.graphics

import me.zeroeightsix.kami.module.modules.client.ClickGUI
import me.zeroeightsix.kami.util.Quad
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11.*
import java.util.*

object GlStateUtils {
    private val mc = Wrapper.minecraft
    private var lastScissor: Quad<Int, Int, Int, Int>? = null
    private val scissorList = LinkedList<Quad<Int, Int, Int, Int>>()

    fun glScissor(x: Int, y: Int, width: Int, height: Int) {
        lastScissor = Quad(x, y, width, height)
        glScissor(x, y, width, height)
    }

    fun pushScissor() {
        lastScissor?.let { scissorList.add(it) }
    }

    fun popScissor() {
        scissorList.pollLast()?.let { glScissor(it.first, it.second, it.third, it.fourth) }
    }

    @JvmStatic
    fun useVbo(): Boolean {
        return mc.gameSettings.useVbo
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
    fun rescaleActual() {
        rescale(Wrapper.minecraft.displayWidth.toDouble(), Wrapper.minecraft.displayHeight.toDouble())
    }

    @JvmStatic
    fun rescaleKami() {
        val scale = ClickGUI.getScaleFactor()
        rescale(Wrapper.minecraft.displayWidth / scale, Wrapper.minecraft.displayHeight / scale)
    }

    @JvmStatic
    fun rescaleMc() {
        val resolution = ScaledResolution(Wrapper.minecraft)
        rescale(resolution.scaledWidth_double, resolution.scaledHeight_double)
    }

    @JvmStatic
    fun rescale(width: Double, height: Double) {
        GlStateManager.clear(256)
        GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight)
        GlStateManager.matrixMode(GL_PROJECTION)
        GlStateManager.loadIdentity()
        GlStateManager.ortho(0.0, width, height, 0.0, 1000.0, 3000.0)
        GlStateManager.matrixMode(GL_MODELVIEW)
        GlStateManager.loadIdentity()
        GlStateManager.translate(0.0f, 0.0f, -2000.0f)
    }
}