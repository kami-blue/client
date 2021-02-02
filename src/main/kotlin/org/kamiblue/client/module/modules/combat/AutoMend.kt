package org.kamiblue.client.module.modules.combat

import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.GuiEvent
import org.kamiblue.client.manager.managers.FriendManager
import org.kamiblue.client.manager.managers.PlayerPacketManager
import org.kamiblue.client.manager.managers.PlayerPacketManager.PlayerPacket
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.EntityUtils.isFakeOrSelf
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.items.swapToSlot
import org.kamiblue.client.util.math.Vec2f
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.runSafe
import org.kamiblue.client.util.threads.safeListener
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Enchantments
import net.minecraft.init.Items
import net.minecraft.util.EnumHand
import net.minecraft.util.math.RayTraceResult
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.utils.MathUtils.reverseNumber
import org.kamiblue.event.listener.listener

internal object AutoMend : Module(
    name = "AutoMend",
    category = Category.COMBAT,
    description = "Automatically mends armour"
) {
    private val autoThrow by setting("AutoThrow", true)
    private val throwDelay = setting("ThrowDelay", 2, 0..5, 1, description = "Number of ticks between throws to allow absorption")
    private val autoSwitch by setting("AutoSwitch", true)
    private val autoDisable by setting("AutoDisable", false, { autoSwitch })
    private val cancelNearby by setting("CancelNearby", NearbyMode.OFF, description = "Don't mend when an enemy is nearby")
    private val pauseNearbyRadius by setting("NearbyRadius", 8, 1..8, 1, { cancelNearby != NearbyMode.OFF })
    private val threshold by setting("RepairAt", 75, 1..100, 1, description = "Percentage to start repairing any armor piece")
    private val gui by setting("AllowGUI", false, description = "Allow mending when inside a GUI")

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

            if ((autoSwitch || autoThrow) // avoid checking if no actions are going to be done
                && hasBlockUnder()
                && (shouldMend(0) || shouldMend(1) || shouldMend(2) || shouldMend(3))) {
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
                    val packet = PlayerPacket(rotating = true, rotation = Vec2f(player.rotationYaw, 90.0f))
                    PlayerPacketManager.addPacket(AutoMend, packet)
                    if (validServerSideRotation() && throwDelayTimer.tick(throwDelay.value.toLong())) {
                        playerController.processRightClick(player, world, EnumHand.MAIN_HAND)
                    }
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
        val hasMending = EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, stack) > 0
        return hasMending && stack.isItemDamaged && 100 * stack.itemDamage / stack.maxDamage > reverseNumber(threshold, 1, 100)
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
