package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.GuiEvent
import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.EntityUtils.isFakeOrSelf
import me.zeroeightsix.kami.util.items.swapToSlot
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.runSafe
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.util.EnumHand
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.utils.MathUtils.reverseNumber
import org.kamiblue.event.listener.listener

internal object AutoMend : Module(
    name = "AutoMend",
    category = Category.COMBAT,
    description = "Automatically mends armour"
) {
    private val autoThrow by setting("AutoThrow", true)
    private val autoSwitch by setting("AutoSwitch", true)
    private val autoDisable by setting("AutoDisable", false, { autoSwitch })
    private val cancelNearby by setting("CancelNearby", NearbyMode.OFF, description = "Don't mend when an enemy is nearby")
    private val pauseNearbyRadius by setting("NearbyRadius", 8, 1..8, 1, { cancelNearby != NearbyMode.OFF })
    private val threshold by setting("RepairAt", 75, 1..100, 1, description = "Percentage to start repairing any armor piece")
    private val gui by setting("AllowGUI", false, description = "Allow mending when inside a GUI")

    private var initHotbarSlot = -1
    private var isGuiOpened = false
    private var paused = false

    @Suppress("unused")
    private enum class NearbyMode {
        OFF, PAUSE, DISABLE
    }

    init {
        onEnable {
            paused = false
            if (autoSwitch) {
                runSafe {
                    initHotbarSlot = player.inventory.currentItem
                }
            }
        }

        onDisable {
            switchback()
        }

        listener<GuiEvent.Displayed> {
            isGuiOpened = it.screen != null
        }

        listener<GuiEvent.Closed> {
            isGuiOpened = false
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (isGuiOpened && !gui) return@safeListener

            if (cancelNearby != NearbyMode.OFF && isNearbyPlayer()) {
                if (cancelNearby == NearbyMode.DISABLE) {
                    disable()
                } else {
                    if (!paused)
                        switchback()
                    paused = true
                }
                return@safeListener
            }
            paused = false

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

    private fun switchback() {
        if (autoSwitch) {
            runSafe {
                if (initHotbarSlot != -1 && initHotbarSlot != player.inventory.currentItem) {
                    swapToSlot(initHotbarSlot)
                }
            }
        }
    }

    private fun isNearbyPlayer(): Boolean {
        for (entity in mc.world.loadedEntityList) {
            if (entity !is EntityPlayer) continue
            if (entity.isFakeOrSelf) continue
            if (AntiBot.isBot(entity)) continue
            if (mc.player.getDistance(entity) > pauseNearbyRadius) continue
            if (FriendManager.isFriend(entity.name)) continue
            return true
        }
        return false
    }
}