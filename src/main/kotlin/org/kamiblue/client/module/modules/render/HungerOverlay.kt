package org.kamiblue.client.module.modules.render

import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.init.MobEffects
import net.minecraft.item.ItemFood
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.GuiIngameForge
import net.minecraftforge.client.event.RenderGameOverlayEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.graphics.GlStateUtils
import org.kamiblue.client.util.items.foodValue
import org.kamiblue.client.util.items.saturation
import org.kamiblue.commons.extension.ceilToInt
import org.kamiblue.event.listener.listener
import org.lwjgl.opengl.GL11.glColor4f
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min

internal object HungerOverlay : Module(
    name = "HungerOverlay",
    description = "Displays a helpful overlay over your hunger bar.",
    category = Category.RENDER
) {
    private val saturationOverlay = setting("Saturation Overlay", true)
    private val foodHungerOverlay = setting("Food Hunger Overlay", true)
    private val foodSaturationOverlay = setting("Food Saturation Overlay", true)

    private val icons = ResourceLocation("kamiblue/textures/hungeroverlay.png")

    init {
        listener<RenderGameOverlayEvent.Post> {
            if (it.type != RenderGameOverlayEvent.ElementType.FOOD) return@listener

            val time = (System.currentTimeMillis() % 5000L) / 2500f
            val flashAlpha = -0.5f * cos(time * 3.1415927f) + 0.5f
            val stats = mc.player.foodStats
            val resolution = ScaledResolution(mc)
            val left = resolution.scaledWidth / 2 + 82
            val top = resolution.scaledHeight - GuiIngameForge.right_height + 10

            val item = mc.player.heldItemMainhand.item
            val foodValue = (item as? ItemFood)?.foodValue ?: 0
            val saturation = (item as? ItemFood)?.saturation ?: 0.0f

            val newHungerValue = min(stats.foodLevel + foodValue, 20)
            val newSaturationValue = min((stats.saturationLevel + saturation), newHungerValue.toFloat())

            GlStateUtils.blend(true)
            if (foodHungerOverlay.value && foodValue > 0) {
                drawHungerBar(stats.foodLevel, newHungerValue, left, top, flashAlpha)
            }

            if (saturationOverlay.value) {
                drawSaturationBar(0f, stats.saturationLevel, left, top, 1f)
            }

            if (foodSaturationOverlay.value && saturation > 0.0f) {
                drawSaturationBar(floor(stats.saturationLevel), newSaturationValue, left, top, flashAlpha)
            }
            mc.textureManager.bindTexture(Gui.ICONS)
        }
    }

    private fun drawHungerBar(start: Int, end: Int, left: Int, top: Int, alpha: Float) {
        val textureX = if (mc.player.isPotionActive(MobEffects.HUNGER)) 88 else 52

        mc.textureManager.bindTexture(Gui.ICONS)
        drawBarHalf((start / 2f).toInt(), (end / 2f), left, top, textureX, alpha)
    }

    private fun drawSaturationBar(start: Float, end: Float, left: Int, top: Int, alpha: Float) {
        mc.textureManager.bindTexture(icons)
        drawBarFourth((start / 2f).toInt(), (end / 2f), left, top, alpha)
    }

    private fun drawBarHalf(start: Int, end: Float, left: Int, top: Int, textureX: Int, alpha: Float) {
        glColor4f(1f, 1f, 1f, alpha)
        for (currentBar in start..(end).ceilToInt()) {
            val remainBars = min((floor((end - currentBar) * 2f) / 2f), 1f)
            val posX = left - (currentBar * 8)
            when (remainBars) {
                1.0f -> mc.ingameGUI.drawTexturedModalRect(posX, top, textureX, 27, 9, 9)
                0.5f -> mc.ingameGUI.drawTexturedModalRect(posX, top, textureX + 9, 27, 9, 9)
            }
        }
        glColor4f(1f, 1f, 1f, 1f)
    }

    private fun drawBarFourth(start: Int, end: Float, left: Int, top: Int, alpha: Float) {
        glColor4f(1f, 1f, 1f, alpha)
        for (currentBar in start..(end).ceilToInt()) {
            val remainBars = min((floor((end - currentBar) * 4f) / 4f), 1f)
            val posX = left - (currentBar * 8)
            when (remainBars) {
                1.00f -> mc.ingameGUI.drawTexturedModalRect(posX, top, 27, 0, 9, 9)
                0.75f -> mc.ingameGUI.drawTexturedModalRect(posX, top, 18, 0, 9, 9)
                0.50f -> mc.ingameGUI.drawTexturedModalRect(posX, top, 9, 0, 9, 9)
                0.25f -> mc.ingameGUI.drawTexturedModalRect(posX, top, 0, 0, 9, 9)
            }
        }
        glColor4f(1f, 1f, 1f, 1f)
    }
}