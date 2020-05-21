package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.client.InfoOverlay
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MathsUtils
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType

/**
 * Created by Dewy on the 4th of April, 2020
 */
// The code here is terrible. Not proud of it. TODO: Make this not suck.
@Module.Info(
        name = "ElytraReplace",
        description = "Automatically swap and replace your chestplate and elytra.",
        category = Module.Category.MOVEMENT
)
class ElytraReplace : Module() {
    private val threshold = register(Settings.integerBuilder("Broken %").withRange(1, 100).withValue(7).build())
    private val inventoryMode = register(Settings.b("Inventory", false))
    private val elytraFlightCheck = register(Settings.b("ElytraFlight Check", true))
    private var currentlyMovingElytra = false
    private var currentlyMovingChestplate = false
    private var elytraCount = 0
    private var chestplateCount = 0

    override fun onUpdate() {
        if (!inventoryMode.value && mc.currentScreen is GuiContainer) {
            return
        }

        elytraCount = 0
        for (i in 0..44) {
            if (mc.player.inventory.getStackInSlot(i).getItem() === Items.ELYTRA && !isBroken(i)) {
                elytraCount += 1
            } else if (mc.player.inventory.getStackInSlot(i).getItem() === Items.DIAMOND_CHESTPLATE && !isBroken(i)) {
                chestplateCount += 1
            }
        }

        if (currentlyMovingElytra) {
            mc.playerController.windowClick(0, 6, 0, ClickType.PICKUP, mc.player)
            currentlyMovingElytra = false
            return
        } else if (currentlyMovingChestplate) {
            mc.playerController.windowClick(0, 6, 0, ClickType.PICKUP, mc.player)
            currentlyMovingChestplate = false
            return
        }

        if (onGround()) {
            var slot = -420

            if (chestplateCount == 0) {
                return
            }

            if (mc.player.inventory.armorInventory[2].isEmpty()) {
                for (i in 0..44) {
                    if (mc.player.inventory.getStackInSlot(i).getItem() === Items.DIAMOND_CHESTPLATE) {
                        slot = i
                        break
                    }
                }
                mc.playerController.windowClick(0, if (slot < 9) slot + 36 else slot, 0, ClickType.PICKUP, mc.player)
                currentlyMovingElytra = true
                return
            } else if (!(mc.player.inventory.armorInventory[2].getItem() === Items.DIAMOND_CHESTPLATE)) {
                for (i in 0..44) {
                    if (mc.player.inventory.getStackInSlot(i).getItem() === Items.DIAMOND_CHESTPLATE) {
                        slot = i
                        break
                    }
                }
                mc.playerController.windowClick(0, if (slot < 9) slot + 36 else slot, 0, ClickType.PICKUP, mc.player)
                mc.playerController.windowClick(0, 6, 0, ClickType.PICKUP, mc.player)
                mc.playerController.windowClick(0, if (slot < 9) slot + 36 else slot, 0, ClickType.PICKUP, mc.player)
                return
            }
        } else if (passElytraFlightCheck()) {
            var slot = -420

            if (elytraCount == 0) {
                return
            }

            // if there's no elytra, or it's broken
            if (!(mc.player.inventory.armorInventory[2].getItem() === Items.ELYTRA) || isBrokenArmor(2)) {
                for (i in 0..44) {
                    if (mc.player.inventory.getStackInSlot(i).getItem() === Items.ELYTRA && !isBroken(i)) {
                        slot = i
                        break
                    }
                }
                if (mc.player.inventory.armorInventory[2].isEmpty()) {
                    mc.playerController.windowClick(0, if (slot < 9) slot + 36 else slot, 0, ClickType.PICKUP, mc.player)
                    currentlyMovingElytra = true
                } else {
                    mc.playerController.windowClick(0, if (slot < 9) slot + 36 else slot, 0, ClickType.PICKUP, mc.player)
                    mc.playerController.windowClick(0, 6, 0, ClickType.PICKUP, mc.player)
                    mc.playerController.windowClick(0, if (slot < 9) slot + 36 else slot, 0, ClickType.PICKUP, mc.player)
                }
            }
        }
    }

    override fun getHudInfo(): String {
        return elytraCount.toString()
    }

    private fun onGround(): Boolean {
        return mc.player.onGround
    }

    private fun isBroken(i : Int): Boolean { // (100 * damage / max damage) >= (100 - 70)
        return if (mc.player.inventory.getStackInSlot(i).maxDamage == 0) {
            false
        } else {
            100 * mc.player.inventory.getStackInSlot(i).getItemDamage() / mc.player.inventory.getStackInSlot(i).maxDamage > MathsUtils.reverseNumber(threshold.value, 1, 100)
        }
    }

    private fun isBrokenArmor(i : Int): Boolean { // (100 * damage / max damage) >= (100 - 70)
        return if (mc.player.inventory.armorInventory[i].maxDamage == 0) {
            false
        } else {
            100 * mc.player.inventory.armorInventory[i].getItemDamage() / mc.player.inventory.armorInventory[i].maxDamage > MathsUtils.reverseNumber(threshold.value, 1, 100)
        }
    }

    private fun passElytraFlightCheck(): Boolean {
        return if (elytraFlightCheck.value && KamiMod.MODULE_MANAGER.isModuleEnabled(ElytraFlight::class.java)) {
            true
        } else !elytraFlightCheck.value
    }
}