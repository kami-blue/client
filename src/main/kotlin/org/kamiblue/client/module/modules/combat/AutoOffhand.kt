package org.kamiblue.client.module.modules.combat

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects
import net.minecraft.inventory.Slot
import net.minecraft.item.*
import net.minecraft.network.play.server.SPacketConfirmTransaction
import net.minecraft.potion.PotionUtils
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.manager.managers.CombatManager
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.*
import org.kamiblue.client.util.combat.CombatUtils.calcDamageFromMob
import org.kamiblue.client.util.combat.CombatUtils.calcDamageFromPlayer
import org.kamiblue.client.util.combat.CombatUtils.scaledHealth
import org.kamiblue.client.util.items.*
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.commons.extension.next
import org.lwjgl.input.Keyboard
import kotlin.math.ceil
import kotlin.math.max

internal object AutoOffhand : Module(
    name = "AutoOffhand",
    description = "Manages item in your offhand",
    category = Category.COMBAT
) {
    private val type by setting("Type", Type.TOTEM)

    // Totem
    private val hpThreshold by setting("Hp Threshold", 5f, 1f..20f, 0.5f, { type == Type.TOTEM })
    private val bindTotem by setting("Bind Totem", Bind(), { type == Type.TOTEM })
    private val checkDamage by setting("Check Damage", true, { type == Type.TOTEM })
    private val mob by setting("Mob", true, { type == Type.TOTEM && checkDamage })
    private val player by setting("Player", true, { type == Type.TOTEM && checkDamage })
    private val crystal by setting("Crystal", true, { type == Type.TOTEM && checkDamage })
    private val falling by setting("Falling", true, { type == Type.TOTEM && checkDamage })

    // Gapple
    private val offhandGapple by setting("Offhand Gapple", false, { type == Type.GAPPLE })
    private val bindGapple by setting("Bind Gapple", Bind(), { type == Type.GAPPLE && offhandGapple })
    private val checkAuraG by setting("Check Aura G", true, { type == Type.GAPPLE && offhandGapple })
    private val checkWeaponG by setting("Check Weapon G", false, { type == Type.GAPPLE && offhandGapple })
    private val checkCAGapple by setting("Check CrystalAura G", true, { type == Type.GAPPLE && offhandGapple && !offhandCrystal })

    // Strength
    private val offhandStrength by setting("Offhand Strength", false, { type == Type.STRENGTH })
    private val bindStrength by setting("Bind Strength", Bind(), { type == Type.STRENGTH && offhandStrength })
    private val checkAuraS by setting("Check Aura S", true, { type == Type.STRENGTH && offhandStrength })
    private val checkWeaponS by setting("Check Weapon S", false, { type == Type.STRENGTH && offhandStrength })

    // Crystal
    private val offhandCrystal by setting("Offhand Crystal", false, { type == Type.CRYSTAL })
    private val bindCrystal by setting("Bind Crystal", Bind(), { type == Type.CRYSTAL && offhandCrystal })
    private val checkCACrystal by setting("Check Crystal Aura C", false, { type == Type.CRYSTAL && offhandCrystal })

    // General
    private val priority by setting("Priority", Priority.HOTBAR)
    private val switchMessage by setting("Switch Message", true)
    private val delay by setting("Delay", 2, 1..20, 1,
        description = "Ticks to wait between each move")
    private val confirmTimeout by setting("Confirm Timeout", 5, 1..20, 1,
        description = "Maximum ticks to wait for confirm packets from server")

    private enum class Type(val filter: (ItemStack) -> Boolean) {
        TOTEM({ it.item.id == 449 }),
        GAPPLE({ it.item is ItemAppleGold }),
        STRENGTH({ it -> it.item is ItemPotion && PotionUtils.getEffectsFromStack(it).any { it.potion == MobEffects.STRENGTH } }),
        CRYSTAL({ it.item is ItemEndCrystal }),
    }

    @Suppress("UNUSED")
    private enum class Priority {
        HOTBAR, INVENTORY
    }

    private val transactionLog = HashMap<Short, Boolean>()
    private val confirmTimer = TickTimer(TimeUnit.TICKS)
    private val movingTimer = TickTimer(TimeUnit.TICKS)
    private var maxDamage = 0f

    init {
        safeListener<InputEvent.KeyInputEvent> {
            val key = Keyboard.getEventKey()
            when {
                bindTotem.isDown(key) -> switchToType(Type.TOTEM)
                bindGapple.isDown(key) -> switchToType(Type.GAPPLE)
                bindStrength.isDown(key) -> switchToType(Type.STRENGTH)
                bindCrystal.isDown(key) -> switchToType(Type.CRYSTAL)
            }
        }

        safeListener<PacketEvent.Receive> {
            if (it.packet !is SPacketConfirmTransaction || it.packet.windowId != 0 || !transactionLog.containsKey(it.packet.actionNumber)) return@safeListener

            transactionLog[it.packet.actionNumber] = true
            if (!transactionLog.containsValue(false)) {
                confirmTimer.reset(confirmTimeout * -50L) // If all the click packets were accepted then we reset the timer for next moving
            }
        }

        safeListener<TickEvent.ClientTickEvent>(1100) {
            if (player.isDead || player.health <= 0.0f) return@safeListener

            if (!confirmTimer.tick(confirmTimeout.toLong(), false)) return@safeListener
            if (!movingTimer.tick(delay.toLong(), false)) return@safeListener // Delays `delay` ticks

            updateDamage()

            if (!player.inventory.itemStack.isEmpty) { // If player is holding an in inventory
                if (mc.currentScreen is GuiContainer) { // If inventory is open (playing moving item)
                    movingTimer.reset() // reset movingTimer as the user is currently interacting with the inventory.
                } else { // If inventory is not open (ex. inventory desync)
                    removeHoldingItem()
                }
                return@safeListener
            }

            switchToType(getType(), true)
        }
    }

    private fun SafeClientEvent.getType() = when {
        checkTotem() -> Type.TOTEM
        checkStrength() -> Type.STRENGTH
        checkGapple() -> Type.GAPPLE
        checkCrystal() -> Type.CRYSTAL
        player.heldItemOffhand.isEmpty -> Type.TOTEM
        else -> null
    }

    private fun SafeClientEvent.checkTotem() = player.scaledHealth < hpThreshold
        || (checkDamage && player.scaledHealth - maxDamage < hpThreshold)

    private fun SafeClientEvent.checkGapple() = offhandGapple
        && (checkAuraG && CombatManager.isActiveAndTopPriority(KillAura)
        || checkWeaponG && player.heldItemMainhand.item.isWeapon
        || (checkCAGapple && !offhandCrystal) && CombatManager.isOnTopPriority(CrystalAura))

    private fun checkCrystal() = offhandCrystal
        && checkCACrystal && CrystalAura.isEnabled && CombatManager.isOnTopPriority(CrystalAura)

    private fun SafeClientEvent.checkStrength() = offhandStrength
        && !player.isPotionActive(MobEffects.STRENGTH)
        && player.inventoryContainer.inventory.any(Type.STRENGTH.filter)
        && (checkAuraS && CombatManager.isActiveAndTopPriority(KillAura)
        || checkWeaponS && player.heldItemMainhand.item.isWeapon)

    private fun SafeClientEvent.switchToType(typeOriginal: Type?, alternativeType: Boolean = false) {
        // First check for whether player is holding the right item already or not
        if (typeOriginal == null || checkOffhandItem(typeOriginal)) return

        val attempts = if (alternativeType) 4 else 1

        getItemSlot(typeOriginal, attempts)?.let { (slot, typeAlt) ->
            if (slot == player.offhandSlot) return

            transactionLog.clear()
            moveToSlot(slot, player.offhandSlot).forEach {
                transactionLog[it] = false
            }

            playerController.updateController()

            confirmTimer.reset()
            movingTimer.reset()

            if (switchMessage) MessageSendHelper.sendChatMessage("$chatName Offhand now has a ${typeAlt.toString().toLowerCase()}")
        }
    }

    private fun SafeClientEvent.checkOffhandItem(type: Type) = type.filter(player.heldItemOffhand)

    private fun SafeClientEvent.getItemSlot(type: Type, attempts: Int): Pair<Slot, Type>? =
        getSlot(type)?.to(type)
            ?: if (attempts > 1) {
                getItemSlot(type.next(), attempts - 1)
            } else {
                null
            }

    private fun SafeClientEvent.getSlot(type: Type): Slot? {
        return player.offhandSlot.takeIf(filter(type))
            ?: if (priority == Priority.HOTBAR) {
                player.hotbarSlots.findItemByType(type)
                    ?: player.inventorySlots.findItemByType(type)
                    ?: player.craftingSlots.findItemByType(type)
            } else {
                player.inventorySlots.findItemByType(type)
                    ?: player.hotbarSlots.findItemByType(type)
                    ?: player.craftingSlots.findItemByType(type)
            }
    }

    private fun List<Slot>.findItemByType(type: Type) =
        find(filter(type))

    private fun filter(type: Type) = { it: Slot ->
        type.filter(it.stack)
    }

    private fun SafeClientEvent.updateDamage() {
        maxDamage = 0f
        if (!checkDamage) return

        for (entity in world.loadedEntityList) {
            if (entity.name == player.name) continue
            if (entity !is EntityMob && entity !is EntityPlayer && entity !is EntityEnderCrystal) continue
            if (player.getDistance(entity) > 10.0f) continue

            when {
                mob && entity is EntityMob -> {
                    maxDamage = max(calcDamageFromMob(entity), maxDamage)
                }
                this@AutoOffhand.player && entity is EntityPlayer -> {
                    maxDamage = max(calcDamageFromPlayer(entity, true), maxDamage)
                }
                crystal && entity is EntityEnderCrystal -> {
                    val damage = CombatManager.crystalMap[entity] ?: continue
                    maxDamage = max(damage.selfDamage, maxDamage)
                }
            }
        }

        if (falling && nextFallDist > 3.0f) maxDamage = max(ceil(nextFallDist - 3.0f), maxDamage)
    }

    private val SafeClientEvent.nextFallDist get() = player.fallDistance - player.motionY.toFloat()
}