package org.kamiblue.client.gui.mc

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.inventory.GuiBeacon
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.init.MobEffects
import net.minecraft.inventory.IInventory
import net.minecraft.potion.Potion
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.kamiblue.client.module.modules.misc.BeaconSelector
import java.io.IOException

/**
 * @author TBM
 */
class KamiGuiBeacon(playerInventory: InventoryPlayer, tileBeaconIn: IInventory) : GuiBeacon(playerInventory, tileBeaconIn) {
    private var doRenderButtons = false
    override fun initGui() {
        super.initGui()
        doRenderButtons = true
    }

    override fun updateScreen() {
        super.updateScreen()
        if (doRenderButtons) {
            var id = 20
            var newY = guiTop
            for (pos1 in EFFECTS_LIST) {
                for (potion in pos1) {
                    val customPotion = PowerButtonCustom(id, guiLeft - 27, newY, potion, 0)
                    buttonList.add(customPotion)
                    if (potion === Potion.getPotionById(BeaconSelector.effect)) {
                        customPotion.isSelected = true
                    }
                    newY += 27
                    id++
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun actionPerformed(button: GuiButton) {
        super.actionPerformed(button)
        if (button is PowerButtonCustom) {
            if (button.isSelected) return
            val i = Potion.getIdFromPotion(button.effect)
            if (button.tier < 3) {
                BeaconSelector.effect = i
            }
            buttonList.clear()
            initGui()
            updateScreen()
        }
    }

    internal inner class PowerButtonCustom(buttonId: Int, x: Int, y: Int, val effect: Potion, val tier: Int) : Button(buttonId, x, y, INVENTORY_BACKGROUND, effect.statusIconIndex % 8 * 18, 198 + effect.statusIconIndex / 8 * 18) {
        override fun drawButtonForegroundLayer(mouseX: Int, mouseY: Int) {
            var s = I18n.format(effect.name)
            if (tier >= 3 && effect !== MobEffects.REGENERATION) {
                s = "$s II"
            }
            this@KamiGuiBeacon.drawHoveringText(s, mouseX, mouseY)
        }
    }

    @SideOnly(Side.CLIENT)
    internal open class Button protected constructor(buttonId: Int, x: Int, y: Int, private val iconTexture: ResourceLocation, private val iconX: Int, private val iconY: Int) : GuiButton(buttonId, x, y, 22, 22, "") {
        var isSelected = false

        /**
         * Draws this button to the screen.
         */
        override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int, partialTicks: Float) {
            if (visible) {
                mc.textureManager.bindTexture(BEACON_GUI_TEXTURES)
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
                hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height
                var j = 0
                if (!enabled) {
                    j += width * 2
                } else if (isSelected) {
                    j += width * 1
                } else if (hovered) {
                    j += width * 3
                }
                this.drawTexturedModalRect(x, y, j, 219, width, height)
                if (BEACON_GUI_TEXTURES != iconTexture) {
                    mc.textureManager.bindTexture(iconTexture)
                }
                this.drawTexturedModalRect(x + 2, y + 2, iconX, iconY, 18, 18)
            }
        }
    }

    companion object {
        private val BEACON_GUI_TEXTURES = ResourceLocation("textures/gui/container/beacon.png")
        val EFFECTS_LIST = arrayOf(arrayOf(MobEffects.SPEED, MobEffects.HASTE), arrayOf(MobEffects.RESISTANCE, MobEffects.JUMP_BOOST), arrayOf(MobEffects.STRENGTH))
    }
}