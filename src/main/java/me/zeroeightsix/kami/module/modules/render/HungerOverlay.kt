package me.zeroeightsix.kami.module.modules.render

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.init.MobEffects
import net.minecraft.item.Item
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemStack
import net.minecraft.util.FoodStats
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.GuiIngameForge
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.ObfuscationReflectionHelper
import org.lwjgl.opengl.GL11.*
import kotlin.math.*

/**
 * Created (partly) by Dewy on the 19th of April, 2020
 * Updated by Xiaro on 10/09/20
 */
@Module.Info(
        name = "HungerOverlay",
        description = "Displays a helpful overlay over your hunger bar.",
        category = Module.Category.RENDER
)
class HungerOverlay : Module() {
    private val exhaustionOverlay: Setting<Boolean> = register(Settings.booleanBuilder("ExhaustionOverlay").withValue(true).build())
    private val saturationOverlay: Setting<Boolean> = register(Settings.booleanBuilder("SaturationOverlay").withValue(true).build())
    private val foodHungerOverlay: Setting<Boolean> = register(Settings.booleanBuilder("FoodHungerOverlay").withValue(true).build())
    private val foodSaturationOverlay: Setting<Boolean> = register(Settings.booleanBuilder("FoodSaturationOverlay").withValue(true).build())

    private val exhaustion = ObfuscationReflectionHelper.findField(FoodStats::class.java, "foodExhaustionLevel")
    private val icons = ResourceLocation("kamiblue/textures/hungeroverlay.png")

    @EventHandler
    private val listener = Listener(EventHook { event: RenderGameOverlayEvent.Post ->
        if (event.type != RenderGameOverlayEvent.ElementType.FOOD) return@EventHook

        val time = (System.currentTimeMillis() % 5000L) / 2500f
        val flashAlpha = -0.5f * cos( time * 3.1415927f) + 0.5f
        val stats = mc.player.foodStats
        val resolution = ScaledResolution(mc)
        val left = resolution.scaledWidth / 2 + 82
        val top = resolution.scaledHeight - GuiIngameForge.right_height + 10
        val foodValues = getFoodValues(mc.player.heldItemMainhand.getItem())
        val newHungerValue = min(stats.foodLevel + foodValues.first, 20)
        val newSaturationValue = min((stats.saturationLevel + foodValues.second), newHungerValue.toFloat())
        val exhaustionValue = exhaustion.getFloat(mc.player.foodStats)

        GlStateUtils.blend(true)
        if (foodHungerOverlay.value && foodValues.first > 0) {
            drawHungerBar(stats.foodLevel, newHungerValue, left, top, flashAlpha)
        }

        if (saturationOverlay.value) {
            drawSaturationBar(0f, stats.saturationLevel, left, top, 1f)
        }

        if (foodSaturationOverlay.value && foodValues.second > 0f) {
            drawSaturationBar(floor(stats.saturationLevel), newSaturationValue, left, top, flashAlpha)
        }

        if (exhaustionOverlay.value && exhaustionValue > 0f) {
            drawExhaustionBar(exhaustionValue, left, top)
        }
        GlStateUtils.blend(false)
        mc.textureManager.bindTexture(Gui.ICONS)
    })

            /**
     * @return <Hunger, Saturation>
     */

    /**
     * @return <Hunger, Saturation>
     */
    private fun getFoodValues(item: Item): Pair<Int, Float> {
        if (item !is ItemFood) return 0 to 0f
        return item.getHealAmount(ItemStack.EMPTY) to item.getHealAmount(ItemStack.EMPTY).toFloat() * item.getSaturationModifier(ItemStack.EMPTY) * 2f
    }

    private fun drawHungerBar(start: Int, end: Int, left: Int, top: Int, alpha: Float) {
        val textureX = if (mc.player.isPotionActive(MobEffects.HUNGER)) 88 else 52

        mc.textureManager.bindTexture(Gui.ICONS)
        drawBarHalf((start / 2f).toInt(), (end / 2f), left, top, textureX, alpha)
    }

    private fun drawExhaustionBar(exhaustion: Float, left: Int, top: Int) {

        mc.textureManager.bindTexture(icons)
        drawBarFourth(0, exhaustion * 2.5f, left, top, 9, 0.75f)
    }

    private fun drawSaturationBar(start: Float, end: Float, left: Int, top: Int, alpha: Float) {
        mc.textureManager.bindTexture(icons)
        drawBarFourth((start / 2f).toInt(), (end / 2f), left, top, 0, alpha)
    }

    private fun drawBarHalf(start: Int, end: Float, left: Int, top: Int, textureX: Int, alpha: Float) {
        glColor4f(1f, 1f, 1f, alpha)
        for (currentBar in start..ceil(end).toInt()) {
            val remainBars = min((floor((end - currentBar) * 2f) / 2f), 1f)
            val posX = left - (currentBar * 8)
            when (remainBars) {
                1.0f -> mc.ingameGUI.drawTexturedModalRect(posX, top, textureX, 27, 9, 9)
                0.5f -> mc.ingameGUI.drawTexturedModalRect(posX, top, textureX + 9, 27, 9, 9)
            }
        }
        glColor4f(1f, 1f, 1f, 1f)
    }

    private fun drawBarFourth(start: Int, end: Float, left: Int, top: Int, textureY: Int, alpha: Float) {
        glColor4f(1f, 1f, 1f, alpha)
        for (currentBar in start..ceil(end).toInt()) {
            val remainBars = min((floor((end - currentBar) * 4f) / 4f), 1f)
            val posX = left - (currentBar * 8)
            when (remainBars) {
                1.00f -> mc.ingameGUI.drawTexturedModalRect(posX, top, 27, textureY, 9, 9)
                0.75f -> mc.ingameGUI.drawTexturedModalRect(posX, top, 18, textureY, 9, 9)
                0.50f -> mc.ingameGUI.drawTexturedModalRect(posX, top, 9, textureY, 9, 9)
                0.25f -> mc.ingameGUI.drawTexturedModalRect(posX, top, 0, textureY, 9, 9)
            }
        }
        glColor4f(1f, 1f, 1f, 1f)
    }
}