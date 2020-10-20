package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderOverlayEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.color.ColorConverter
import me.zeroeightsix.kami.util.event.listener
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

    init {
        listener<RenderOverlayEvent> {
            val resolution = ScaledResolution(mc)
            val width = resolution.scaledWidth / 2
            val height = resolution.scaledHeight - 55 - if (isEyeInWater()) 10 else 0

            for ((index, itemStack) in armour.withIndex()) {
                if (itemStack.isEmpty()) continue
                val x = width - (9 - index) * 20 + 92

                GlStateManager.enableDepth()
                GlStateManager.enableTexture2D()
                mc.renderItem.zLevel = 200f
                mc.renderItem.renderItemAndEffectIntoGUI(itemStack, x, height)
                mc.renderItem.renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, x, height, "")
                mc.renderItem.zLevel = 0f
                GlStateManager.enableTexture2D()
                GlStateManager.disableLighting()
                GlStateManager.disableDepth()

                if (damage.value) {
                    val dura = (itemStack.maxDamage - itemStack.itemDamage) / itemStack.maxDamage.toFloat()
                    val duraText = dura.toInt().toString()
                    val green = (dura * 255.0f).toInt()
                    val red = 255 - green
                    mc.fontRenderer.drawStringWithShadow(duraText, x + 8 - mc.fontRenderer.getStringWidth(duraText) / 2.0f, height - 11.0f, ColorConverter.rgbToHex(red, green, 0))
                }
            }

            GlStateManager.enableDepth()
        }
    }

    private fun isEyeInWater(): Boolean {
        val eyePos = mc.player.getPositionEyes(1f)
        val flooredEyePos = BlockPos(floor(eyePos.x), floor(eyePos.y), floor(eyePos.z))
        val block = mc.world.getBlockState(flooredEyePos).block
        return block == Blocks.WATER || block == Blocks.FLOWING_WATER
    }
}