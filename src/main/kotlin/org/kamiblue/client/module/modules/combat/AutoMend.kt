package org.kamiblue.client.module.modules.combat

import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Enchantments
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.util.EnumHand
import net.minecraft.util.math.RayTraceResult
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.GuiEvent
import org.kamiblue.client.manager.managers.FriendManager
import org.kamiblue.client.manager.managers.PlayerPacketManager
import org.kamiblue.client.manager.managers.PlayerPacketManager.sendPlayerPacket
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.EntityUtils.isFakeOrSelf
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.items.clickSlot
import org.kamiblue.client.util.items.swapToSlot
import org.kamiblue.client.util.math.Vec2f
import org.kamiblue.client.util.threads.runSafe
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.commons.utils.MathUtils.reverseNumber
import org.kamiblue.event.listener.listener

internal object AutoMend : Module(
    name = "AutoMend",
    category = Category.COMBAT,
    description = "Automatically mends armour"
) {

    private val autoThrow by setting("Auto Throw", true)
    private val throwDelay = setting("Throw Delay", 2, 0..5, 1, description = "Number of ticks between throws to allow absorption")
    private val autoSwitch by setting("Auto Switch", true)
    private val autoDisableExp by setting("Auto Disable", false, { autoSwitch }, description = "Disable when you run out of XP bottles")
    private val autoDisableComplete by setting("Disable on Complete", false)
    private val takeOff by setting("Take Off", true)
    private val pauseAutoArmor = setting("Pause AutoArmor", true, { takeOff })
    private val cancelNearby by setting("Cancel Nearby", NearbyMode.OFF, description = "Don't mend when an enemy is nearby")
    private val pauseNearbyRadius by setting("Nearby Radius", 8, 1..8, 1, { cancelNearby != NearbyMode.OFF })
    private val threshold by setting("Repair At", 75, 1..100, 1, description = "Percentage to start repairing any armor piece")
    private val gui by setting("Allow GUI", false, description = "Allow mending when inside a GUI")

    private var initHotbarSlot = -1
    private var isGuiOpened = false
    private var paused = false

    private val throwDelayTimer = TickTimer(TimeUnit.TICKS)

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
            if (AutoArmor.isPaused && pauseAutoArmor.value) {
                AutoArmor.isPaused = false
            }
        }

        // TODO: Add proper module pausing system
        pauseAutoArmor.listeners.add {
            if (!pauseAutoArmor.value) {
                AutoArmor.isPaused = false
            }
        }

        listener<GuiEvent.Displayed> {
            isGuiOpened = it.screen != null
        }

        listener<GuiEvent.Closed> {
            isGuiOpened = false
        }

        safeListener<TickEvent.ClientTickEvent> {
            var isMending = false

            if (!isGuiOpened || gui) {
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

                // don't call twice in same tick so store in a var
                val shouldMend = shouldMend(0) || shouldMend(1) || shouldMend(2) || shouldMend(3)
                if (!shouldMend && autoDisableComplete) {
                    disable()
                }

                if ((autoSwitch || autoThrow) // avoid checking if no actions are going to be done
                    && hasBlockUnder() && shouldMend) {
                    if (autoSwitch && player.heldItemMainhand.item !== Items.EXPERIENCE_BOTTLE) {
                        val xpSlot = findXpPots()

                        if (xpSlot == -1) {
                            if (autoDisableExp) {
                                disable()
                            }

                            return@safeListener
                        }

                        player.inventory.currentItem = xpSlot
                    }

                    if (autoThrow && player.heldItemMainhand.item === Items.EXPERIENCE_BOTTLE) {
                        sendPlayerPacket {
                            rotate(Vec2f(player.rotationYaw, 90.0f))
                        }

                        isMending = true

                        if (validServerSideRotation() && throwDelayTimer.tick(throwDelay.value.toLong())) {
                            playerController.processRightClick(player, world, EnumHand.MAIN_HAND)
                        }
                    }
                }
            }

            if (pauseAutoArmor.value) {
                AutoArmor.isPaused = isMending

                if (takeOff && isMending) {
                    var minSlot = 9

                    for (i in 0..3) {
                        if (shouldMend(i) || !hasMending(i)) continue
                        val emptySlot = findEmptySlot(minSlot)
                        minSlot = emptySlot + 1

                        if (emptySlot == -1) break
                        clickSlot(player.inventoryContainer.windowId, 8 - i, 0, ClickType.PICKUP)
                        clickSlot(player.inventoryContainer.windowId, emptySlot, 0, ClickType.PICKUP)
                    }
                }
            }
        }
    }

    private fun SafeClientEvent.findEmptySlot(min: Int): Int {
        for (i in min..36) {
            if (player.inventory.getStackInSlot(i).isEmpty) {
                return i
            }
        }

        return -1
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

    private fun SafeClientEvent.hasMending(slot: Int): Boolean {
        val stack = player.inventory.armorInventory[slot]
        return EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, stack) > 0
    }

    private fun SafeClientEvent.shouldMend(i: Int): Boolean { // (100 * damage / max damage) >= (100 - 70)
        val stack = player.inventory.armorInventory[i]
        return hasMending(i) && stack.isItemDamaged && 100 * stack.itemDamage / stack.maxDamage > reverseNumber(threshold, 1, 100)
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

    private fun validServerSideRotation(): Boolean {
        val pitch = PlayerPacketManager.serverSideRotation.y
        return pitch in 80.0f..90.0f
    }

    private fun SafeClientEvent.hasBlockUnder(): Boolean {
        val posVec = player.positionVector
        val result = world.rayTraceBlocks(posVec, posVec.add(0.0, -5.33, 0.0), false, true, false)
        return result != null && result.typeOfHit == RayTraceResult.Type.BLOCK
    }
}
