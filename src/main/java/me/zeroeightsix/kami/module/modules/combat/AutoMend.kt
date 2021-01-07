package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.GuiEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.runSafe
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.init.Items
import net.minecraft.util.EnumHand
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.utils.MathUtils.reverseNumber
import org.kamiblue.event.listener.listener

object AutoMend : Module(
    name = "AutoMend",
    category = Category.COMBAT,
    description = "Automatically mends armour"
) {
    private val autoThrow by setting("AutoThrow", true)
    private val autoSwitch by setting("AutoSwitch", true)
    private val autoDisable by setting("AutoDisable", false, { autoSwitch })
    private val threshold by setting("Repair%", 75, 1..100, 1)
    private val gui by setting("RunInGUIs", false)

    private var initHotbarSlot = -1
    private var isGuiOpened = false

    init {
        onEnable {
            if (autoSwitch) {
                runSafe {
                    initHotbarSlot = player.inventory.currentItem
                }
            }
        }

        onDisable {
            if (autoSwitch) {
                runSafe {
                    if (initHotbarSlot != -1 && initHotbarSlot != player.inventory.currentItem) {
                        player.inventory.currentItem = initHotbarSlot
                    }
                }
            }
        }

        listener<GuiEvent.Displayed> {
            isGuiOpened = it.screen != null
        }

        listener<GuiEvent.Closed> {
            isGuiOpened = false
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (isGuiOpened && !gui) return@safeListener

            if (shouldMend(0) || shouldMend(1) || shouldMend(2) || shouldMend(3)) {
                if (autoSwitch && player.heldItemMainhand.item !== Items.EXPERIENCE_BOTTLE) {
                    val xpSlot = findXpPots()

                    if (xpSlot == -1) {
                        if (autoDisable) {
                            MessageSendHelper.sendWarningMessage("$chatName No XP in hotbar, disabling")
                            disable()
                        }
                        return@safeListener
                    }
                    player.inventory.currentItem = xpSlot
                }
                if (autoThrow && player.heldItemMainhand.item === Items.EXPERIENCE_BOTTLE) {
                    playerController.processRightClick(player, world, EnumHand.MAIN_HAND)
                }
            }
        }
    }

    private fun SafeClientEvent.findXpPots(): Int {
        var slot = -1
        for (i in 0..8) {
            if (player.inventory.getStackInSlot(i).item === Items.EXPERIENCE_BOTTLE) {
                slot = i
                break
            }
        }
        return slot
    }

    private fun SafeClientEvent.shouldMend(i: Int): Boolean { // (100 * damage / max damage) >= (100 - 70)
        val stack = player.inventory.armorInventory[i]
        return stack.isItemDamaged && 100 * stack.itemDamage / stack.maxDamage > reverseNumber(threshold, 1, 100)
    }
}