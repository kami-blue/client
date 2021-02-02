package org.kamiblue.client.module.modules.combat

import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.manager.managers.CombatManager
import org.kamiblue.client.mixin.extension.syncCurrentPlayItem
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.*
import org.kamiblue.client.util.combat.CombatUtils
import org.kamiblue.client.util.combat.CombatUtils.calcDamageFromMob
import org.kamiblue.client.util.combat.CombatUtils.calcDamageFromPlayer
import org.kamiblue.client.util.combat.CrystalUtils.calcCrystalDamage
import org.kamiblue.client.util.items.*
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.safeListener
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
    private val hpThreshold by setting("HpThreshold", 5f, 1f..20f, 0.5f, { type == Type.TOTEM })
    private val bindTotem by setting("BindTotem", Bind(), { type == Type.TOTEM })
    private val checkDamage by setting("CheckDamage", true, { type == Type.TOTEM })
    private val mob by setting("Mob", true, { type == Type.TOTEM && checkDamage })
    private val player by setting("Player", true, { type == Type.TOTEM && checkDamage })
    private val crystal by setting("Crystal", true, { type == Type.TOTEM && checkDamage })
    private val falling by setting("Falling", true, { type == Type.TOTEM && checkDamage })

    // Gapple
    private val offhandGapple by setting("OffhandGapple", false, { type == Type.GAPPLE })
    private val bindGapple by setting("BindGapple", Bind(), { type == Type.GAPPLE && offhandGapple })
    private val checkAuraG by setting("CheckAuraG", true, { type == Type.GAPPLE && offhandGapple })
    private val checkWeaponG by setting("CheckWeaponG", false, { type == Type.GAPPLE && offhandGapple })
    private val checkCAGapple by setting("CheckCrystalAuraG", true, { type == Type.GAPPLE && offhandGapple && !offhandCrystal })

    // Strength
    private val offhandStrength by setting("OffhandStrength", false, { type == Type.STRENGTH })
    private val bindStrength by setting("BindStrength", Bind(), { type == Type.STRENGTH && offhandStrength })
    private val checkAuraS by setting("CheckAuraS", true, { type == Type.STRENGTH && offhandStrength })
    private val checkWeaponS by setting("CheckWeaponS", false, { type == Type.STRENGTH && offhandStrength })

    // Crystal
    private val offhandCrystal by setting("OffhandCrystal", false, { type == Type.CRYSTAL })
    private val bindCrystal by setting("BindCrystal", Bind(), { type == Type.CRYSTAL && offhandCrystal })
    private val checkCACrystal by setting("CheckCrystalAuraC", false, { type == Type.CRYSTAL && offhandCrystal })

    // General
    private val priority by setting("Priority", Priority.HOTBAR)
    private val switchMessage by setting("SwitchMessage", true)

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
    private val movingTimer = TickTimer()
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

            transactionLog[it.packet.actionNumber] = it.packet.wasAccepted()
            if (!transactionLog.containsValue(false)) {
                movingTimer.reset(-175L) // If all the click packets were accepted then we reset the timer for next moving
            }
        }

        safeListener<TickEvent.ClientTickEvent>(1100) {
            if (player.isDead) return@safeListener

            updateDamage()

            if (!movingTimer.tick(200L, false)) return@safeListener // Delays 4 ticks by default

            if (!player.inventory.itemStack.isEmpty) { // If player is holding an in inventory
                if (mc.currentScreen is GuiContainer) {// If inventory is open (playing moving item)
                    movingTimer.reset() // delay for 5 ticks
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

    private fun SafeClientEvent.checkTotem() = CombatUtils.getHealthSmart(player) < hpThreshold
        || (checkDamage && CombatUtils.getHealthSmart(player) - maxDamage < hpThreshold)

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

        getItemSlot(typeOriginal)?.let { (slot, typeAlt) ->
            // Second check is for case of when player ran out of the original type of item
            if (!alternativeType && typeAlt != typeOriginal || checkOffhandItem(typeAlt)) return

            transactionLog.clear()
            transactionLog.putAll(moveToSlot(slot.slotNumber, 45).associate { it to false })

            playerController.syncCurrentPlayItem()
            movingTimer.reset()

            if (switchMessage) MessageSendHelper.sendChatMessage("$chatName Offhand now has a ${typeAlt.toString().toLowerCase()}")
        }
    }

    private fun SafeClientEvent.checkOffhandItem(type: Type) = type.filter(player.heldItemOffhand)

    private fun SafeClientEvent.getItemSlot(type: Type, loopTime: Int = 1): Pair<Slot, Type>? =
        getSlot(type)?.to(type)
            ?: if (loopTime <= 3) {
                getItemSlot(type.next(), loopTime + 1)
            } else {
                null
            }

    private fun SafeClientEvent.getSlot(type: Type): Slot? {
        val slots = player.inventorySlots

        return if (priority == Priority.HOTBAR) {
            slots.lastOrNull { type.filter(it.stack) }
        } else {
            slots.firstOrNull { type.filter(it.stack) }
        }
    }

    private fun SafeClientEvent.updateDamage() {
        maxDamage = 0f
        if (!checkDamage) return

        for (entity in world.loadedEntityList) {
            if (entity.name == player.name) continue
            if (entity !is EntityMob && entity !is EntityPlayer && entity !is EntityEnderCrystal) continue
            if (player.getDistance(entity) > 10f) continue

            if (mob && entity is EntityMob) {
                maxDamage = max(calcDamageFromMob(entity), maxDamage)
            }

            if (this@AutoOffhand.player && entity is EntityPlayer) {
                maxDamage = max(calcDamageFromPlayer(entity, true), maxDamage)
            }

            if (crystal && entity is EntityEnderCrystal) {
                maxDamage = max(calcCrystalDamage(entity, player), maxDamage)
            }
        }

        if (falling && nextFallDist > 3.0f) maxDamage = max(ceil(nextFallDist - 3.0f), maxDamage)
    }

    private val SafeClientEvent.nextFallDist get() = player.fallDistance - player.motionY.toFloat()
}