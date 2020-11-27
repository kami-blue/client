package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.GuiScreenEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.init.Items
import net.minecraft.util.EnumHand
import org.kamiblue.commons.utils.MathUtils.reverseNumber

@Module.Info(
        name = "AutoMend",
        category = Module.Category.COMBAT,
        description = "Automatically mends armour"
)
object AutoMend : Module() {
    private val autoThrow = register(Settings.b("AutoThrow", true))
    private val autoSwitch = register(Settings.b("AutoSwitch", true))
    private val autoDisable = register(Settings.booleanBuilder("AutoDisable").withValue(false).withVisibility { autoSwitch.value })
    private val threshold = register(Settings.integerBuilder("Repair%").withValue(75).withRange(1, 100).withStep(1))
    private val gui = register(Settings.b("RunInGUIs", false))

    private var initHotbarSlot = -1
    private var isGuiOpened = false

    init {
        listener<GuiScreenEvent.Displayed> {
            isGuiOpened = it.screen != null
        }

        listener<GuiScreenEvent.Closed> {
            isGuiOpened = false
        }

        listener<SafeTickEvent> {
            if (isGuiOpened && !gui.value) return@listener

            if (shouldMend(0) || shouldMend(1) || shouldMend(2) || shouldMend(3)) {
                if (autoSwitch.value && mc.player.heldItemMainhand.getItem() !== Items.EXPERIENCE_BOTTLE) {
                    val xpSlot = findXpPots()

                    if (xpSlot == -1) {
                        if (autoDisable.value) {
                            MessageSendHelper.sendWarningMessage("$chatName No XP in hotbar, disabling")
                            disable()
                        }
                        return@listener
                    }
                    mc.player.inventory.currentItem = xpSlot
                }
                if (autoThrow.value && mc.player.heldItemMainhand.getItem() === Items.EXPERIENCE_BOTTLE) {
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND)
                }
            }
        }
    }

    override fun onEnable() {
        if (mc.player == null) return
        if (autoSwitch.value) {
            initHotbarSlot = mc.player.inventory.currentItem
        }
    }

    override fun onDisable() {
        if (mc.player == null) return
        if (autoSwitch.value && initHotbarSlot != -1 && initHotbarSlot != mc.player.inventory.currentItem) {
            mc.player.inventory.currentItem = initHotbarSlot
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
        val stack = mc.player.inventory.armorInventory[i]
        return stack.isItemDamaged && 100 * stack.getItemDamage() / stack.maxDamage > reverseNumber(threshold.value, 1, 100)
    }
}