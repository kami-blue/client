package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.GuiEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.init.Items
import net.minecraft.util.EnumHand
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.utils.MathUtils.reverseNumber
import org.kamiblue.event.listener.listener

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
        listener<GuiEvent.Displayed> {
            isGuiOpened = it.screen != null
        }

        listener<GuiEvent.Closed> {
            isGuiOpened = false
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (isGuiOpened && !gui.value) return@safeListener

            if (shouldMend(0) || shouldMend(1) || shouldMend(2) || shouldMend(3)) {
                if (autoSwitch.value && player.heldItemMainhand.item !== Items.EXPERIENCE_BOTTLE) {
                    val xpSlot = findXpPots()

                    if (xpSlot == -1) {
                        if (autoDisable.value) {
                            MessageSendHelper.sendWarningMessage("$chatName No XP in hotbar, disabling")
                            disable()
                        }
                        return@safeListener
                    }
                    player.inventory.currentItem = xpSlot
                }
                if (autoThrow.value && player.heldItemMainhand.item === Items.EXPERIENCE_BOTTLE) {
                    playerController.processRightClick(player, world, EnumHand.MAIN_HAND)
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
            if (mc.player.inventory.getStackInSlot(i).item === Items.EXPERIENCE_BOTTLE) {
                slot = i
                break
            }
        }
        return slot
    }

    private fun SafeClientEvent.shouldMend(i: Int): Boolean { // (100 * damage / max damage) >= (100 - 70)
        val stack = player.inventory.armorInventory[i]
        return stack.isItemDamaged && 100 * stack.itemDamage / stack.maxDamage > reverseNumber(threshold.value, 1, 100)
    }
}