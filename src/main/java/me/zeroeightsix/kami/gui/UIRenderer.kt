package me.zeroeightsix.kami.gui

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame
import me.zeroeightsix.kami.gui.rgui.component.listen.RenderListener
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.Wrapper.minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.util.function.Consumer

object UIRenderer {
    fun renderAndUpdateFrames() {
        if (minecraft.currentScreen is DisplayGuiScreen || minecraft.gameSettings.showDebugInfo) return
        val gui = KamiMod.getInstance().guiManager

        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        for (child in gui.children) {
            if (child is Frame) {
                GlStateManager.pushMatrix()
                if (child.isPinned && child.isVisible) {
                    val slide = child.opacity != 0f
                    GL11.glTranslated(child.x.toDouble(), child.y.toDouble(), 0.0)
                    child.renderListeners.forEach(Consumer { renderListener: RenderListener -> renderListener.onPreRender() })
                    child.theme.getUIForComponent(child).renderComponent(child, child.theme.fontRenderer)
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
                    GL11.glTranslated(translateX, translateY, 0.0)
                    child.renderListeners.forEach(Consumer { obj: RenderListener -> obj.onPostRender() })
                    child.renderChildren()
                    GL11.glTranslated(-translateX, -translateY, 0.0)
                    GL11.glTranslated(-child.x.toDouble(), -child.y.toDouble(), 0.0)
                }
                GlStateManager.popMatrix()
            }
        }
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
    }
}