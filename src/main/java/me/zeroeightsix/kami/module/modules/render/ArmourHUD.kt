package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.color.ColorConverter
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.GameType
import kotlin.math.floor

@Module.Info(
        name = "ArmourHUD",
        category = Module.Category.RENDER,
        description = "Displays your armour and it's durability on screen",
        showOnArray = Module.ShowOnArray.OFF
)
object ArmourHUD : Module() {
    private val damage = register(Settings.b("Damage", false))
    private val armour: NonNullList<ItemStack>
        get() = if (mc.playerController.getCurrentGameType() == GameType.CREATIVE || mc.playerController.getCurrentGameType() == GameType.SPECTATOR) {
            NonNullList.withSize(4, ItemStack.EMPTY)
        } else {
            mc.player.inventory.armorInventory
        }

    override fun onRender() {
        GlStateManager.enableTexture2D()
        val resolution = ScaledResolution(mc)
        val i = resolution.scaledWidth / 2
        var iteration = 0
        val y = resolution.scaledHeight - 55 - if (isEyeInWater()) 10 else 0
        for (`is` in armour) {
            iteration++
            if (`is`.isEmpty()) continue
            val x = i - 90 + (9 - iteration) * 20 + 2
            GlStateManager.enableDepth()
            mc.renderItem.zLevel = 200f
            mc.renderItem.renderItemAndEffectIntoGUI(`is`, x, y)
            mc.renderItem.renderItemOverlayIntoGUI(mc.fontRenderer, `is`, x, y, "")
            mc.renderItem.zLevel = 0f
            GlStateManager.enableTexture2D()
            GlStateManager.disableLighting()
            GlStateManager.disableDepth()
            val s = if (`is`.count > 1) `is`.count.toString() + "" else ""
            mc.fontRenderer.drawStringWithShadow(s, x + 19 - 2 - mc.fontRenderer.getStringWidth(s).toFloat(), y + 9.toFloat(), 0xffffff)
            if (damage.value) {
                val green = (`is`.maxDamage.toFloat() - `is`.getItemDamage().toFloat()) / `is`.maxDamage.toFloat()
                val red = 1 - green
                val dmg = 100 - (red * 100).toInt()
                mc.fontRenderer.drawStringWithShadow(dmg.toString() + "", x + 8 - mc.fontRenderer.getStringWidth(dmg.toString() + "") / 2.toFloat(), y - 11.toFloat(), ColorConverter.rgbToInt((red * 255).toInt(), (green * 255).toInt(), 0))
            }
        }
        GlStateManager.enableDepth()
        GlStateManager.disableLighting()
    }

    private fun isEyeInWater(): Boolean {
        val eyePos = mc.player.getPositionEyes(1f)
        val flooredEyePos = BlockPos(floor(eyePos.x), floor(eyePos.y), floor(eyePos.z))
        val block = mc.world.getBlockState(flooredEyePos).block
        return block == Blocks.WATER || block == Blocks.FLOWING_WATER
    }
}