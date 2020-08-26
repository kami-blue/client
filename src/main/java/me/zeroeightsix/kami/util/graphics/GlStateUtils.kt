package me.zeroeightsix.kami.util.graphics

import me.zeroeightsix.kami.util.Wrapper
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
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
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
}