package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.colourUtils.ColourConverter
import me.zeroeightsix.kami.util.colourUtils.ColourHolder
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraft.world.GameType

/**
 * Created by 086 on 24/01/2018.
 */
@Module.Info(
        name = "ArmourHUD",
        category = Module.Category.RENDER,
        showOnArray = Module.ShowOnArray.OFF,
        description = "Displays your armour and it's durability on screen"
)
class ArmourHUD : Module() {
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
        val y = resolution.scaledHeight - 55 - if (mc.player.isInWater) 10 else 0
        for (`is` in armour) {
            iteration++
            if (`is`.isEmpty()) continue
            val x = i - 90 + (9 - iteration) * 20 + 2
            GlStateManager.enableDepth()
            itemRender.zLevel = 200f
            itemRender.renderItemAndEffectIntoGUI(`is`, x, y)
            itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, `is`, x, y, "")
            itemRender.zLevel = 0f
            GlStateManager.enableTexture2D()
            GlStateManager.disableLighting()
            GlStateManager.disableDepth()
            val s = if (`is`.count > 1) `is`.count.toString() + "" else ""
            mc.fontRenderer.drawStringWithShadow(s, x + 19 - 2 - mc.fontRenderer.getStringWidth(s).toFloat(), y + 9.toFloat(), 0xffffff)
            if (damage.value) {
                val green = (`is`.maxDamage.toFloat() - `is`.getItemDamage().toFloat()) / `is`.maxDamage.toFloat()
                val red = 1 - green
                val dmg = 100 - (red * 100).toInt()
                mc.fontRenderer.drawStringWithShadow(dmg.toString() + "", x + 8 - mc.fontRenderer.getStringWidth(dmg.toString() + "") / 2.toFloat(), y - 11.toFloat(), ColourConverter.rgbToInt((red * 255).toInt(), (green * 255).toInt(), 0))
            }
        }
        GlStateManager.enableDepth()
        GlStateManager.disableLighting()
    }

    companion object {
        private val itemRender = Minecraft.getMinecraft().getRenderItem()
    }
}