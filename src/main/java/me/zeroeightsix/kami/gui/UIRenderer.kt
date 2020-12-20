package me.zeroeightsix.kami.gui

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import org.lwjgl.opengl.GL11.*

object UIRenderer {
    fun renderAndUpdateFrames() {
        if (Wrapper.minecraft.currentScreen is DisplayGuiScreen || Wrapper.minecraft.gameSettings.showDebugInfo) return
        val gui = KamiMod.INSTANCE.guiManager
        GlStateUtils.rescaleKami()

        for (child in gui.children) {
            if (child !is Frame) continue
            if (!child.isPinned || !child.isVisible) continue

            glPushMatrix()
            val slide = child.opacity != 0f
            glTranslated(child.x.toDouble(), child.y.toDouble(), 0.0)
            for (renderListener in child.renderListeners) renderListener.onPreRender()
            child.theme.getUIForComponent(child).renderComponent(child)
            var translateX = 0.0
            var translateY = 0.0
            if (slide) {
                translateX += child.originOffsetX
                translateY += child.originOffsetY
            } else {
                if (child.docking.isBottom) {
                    translateY += child.originOffsetY
                }
                if (child.docking.isRight) {
                    translateX += child.originOffsetX
                    if (child.children.size > 0) {
                        translateX += (child.width - child.children[0].x - child.children[0].width) / DisplayGuiScreen.getScale()
                    }
                }
                if (child.docking.isLeft && child.children.size > 0) {
                    translateX -= child.children[0].x
                }
                if (child.docking.isTop && child.children.size > 0) {
                    translateY -= child.children[0].y
                }
            }
            glTranslated(translateX, translateY, 0.0)
            for (renderListener in child.renderListeners) renderListener.onPostRender()
            child.renderChildren()
            glPopMatrix()
        }
        GlStateUtils.rescaleMc()
    }
}