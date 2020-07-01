package me.zeroeightsix.kami.module.modules.combat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.GuiScreenEvent.Displayed
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MathsUtils.reverseNumber
import me.zeroeightsix.kami.util.MessageSendHelper
import net.minecraft.init.Items
import net.minecraft.util.EnumHand

/**
 * Created 17 October 2019 by hub
 * Updated 21 November 2019 by hub
 * Rewritten by dominikaaaa on 07/04/20
 * Updated by dominikaaaa on 07/05/20
 */
@Module.Info(
        name = "AutoMend",
        category = Module.Category.COMBAT,
        description = "Automatically mends armour"
)
class AutoMend : Module() {
    private val autoThrow = register(Settings.b("AutoThrow", true))
    private val autoSwitch = register(Settings.b("AutoSwitch", true))
    private val autoDisable = register(Settings.booleanBuilder("AutoDisable").withValue(false).withVisibility { autoSwitch.value }.build())
    private val threshold = register(Settings.integerBuilder("Repair%").withMinimum(1).withMaximum(100).withValue(75).build())
    private val fast = register(Settings.b("FastUse", true))
    private val gui = register(Settings.b("RunInGUIs", false))

    private var initHotbarSlot = -1
    private var isGuiOpened = false

    @EventHandler
    private val receiveListener = Listener(EventHook { event: PacketEvent.Receive? ->
        if (mc.player != null && fast.value && mc.player.heldItemMainhand.getItem() === Items.EXPERIENCE_BOTTLE) {
            mc.rightClickDelayTimer = 0
        }
    })

    override fun onEnable() {
        if (mc.player == null) return
        if (autoSwitch.value) {
            initHotbarSlot = mc.player.inventory.currentItem
        }
    }

    override fun onDisable() {
        if (mc.player == null) return
        if (autoSwitch.value) {
            if (initHotbarSlot != -1 && initHotbarSlot != mc.player.inventory.currentItem) {
                mc.player.inventory.currentItem = initHotbarSlot
            }
        }
    }

    override fun onUpdate() {
        if (mc.player == null) return
        if (isGuiOpened && !gui.value) return

        if (shouldMend(0) || shouldMend(1) || shouldMend(2) || shouldMend(3)) {
            if (autoSwitch.value && mc.player.heldItemMainhand.getItem() !== Items.EXPERIENCE_BOTTLE) {
                val xpSlot = findXpPots()

                if (xpSlot == -1) {
                    if (autoDisable.value) {
                        MessageSendHelper.sendWarningMessage("$chatName No XP in hotbar, disabling")
                        disable()
                    }
                    return
                }
                mc.player.inventory.currentItem = xpSlot
            }
            if (autoThrow.value && mc.player.heldItemMainhand.getItem() === Items.EXPERIENCE_BOTTLE) {
                mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND)
            }
        }
    }

    private fun findXpPots(): Int {
        var slot = -1
        for (i in 0..8) {
            if (mc.player.inventory.getStackInSlot(i).getItem() === Items.EXPERIENCE_BOTTLE) {
                slot = i
                break
            }
        }
        return slot
    }

    private fun shouldMend(i: Int): Boolean { // (100 * damage / max damage) >= (100 - 70)
        return if (mc.player.inventory.armorInventory[i].maxDamage == 0) false
        else 100 * mc.player.inventory.armorInventory[i].getItemDamage() / mc.player.inventory.armorInventory[i].maxDamage > reverseNumber(threshold.value, 1, 100)
    }

    @EventHandler
    private val listener = Listener(EventHook { event: Displayed -> isGuiOpened = event.screen != null })
}