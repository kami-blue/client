package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.manager.managers.CombatManager
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.combat.CombatUtils
import me.zeroeightsix.kami.util.combat.CombatUtils.calcDamageFromMob
import me.zeroeightsix.kami.util.combat.CombatUtils.calcDamageFromPlayer
import me.zeroeightsix.kami.util.combat.CrystalUtils.calcCrystalDamage
import me.zeroeightsix.kami.util.items.*
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.safeListener
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
    private val type = setting("Type", Type.TOTEM)

    // Totem
    private val hpThreshold = setting("HpThreshold", 5f, 1f..20f, 0.5f, { type.value == Type.TOTEM })
    private val bindTotem = setting("BindTotem", Bind(), { type.value == Type.TOTEM })
    private val checkDamage = setting("CheckDamage", true, { type.value == Type.TOTEM })
    private val mob = setting("Mob", true, { type.value == Type.TOTEM && checkDamage.value })
    private val player = setting("Player", true, { type.value == Type.TOTEM && checkDamage.value })
    private val crystal = setting("Crystal", true, { type.value == Type.TOTEM && checkDamage.value })
    private val falling = setting("Falling", true, { type.value == Type.TOTEM && checkDamage.value })

    // Gapple
    private val offhandGapple = setting("OffhandGapple", false, { type.value == Type.GAPPLE })
    private val bindGapple = setting("BindGapple", Bind(), { type.value == Type.GAPPLE && offhandGapple.value })
    private val checkAuraG = setting("CheckAuraG", true, { type.value == Type.GAPPLE && offhandGapple.value })
    private val checkWeaponG = setting("CheckWeaponG", false, { type.value == Type.GAPPLE && offhandGapple.value })
    private val checkCAGapple = setting("CheckCrystalAuraG", true, { type.value == Type.GAPPLE && offhandGapple.value && !offhandCrystal.value })

    // Strength
    private val offhandStrength = setting("OffhandStrength", false, { type.value == Type.STRENGTH })
    private val bindStrength = setting("BindStrength", Bind(), { type.value == Type.STRENGTH && offhandStrength.value })
    private val checkAuraS = setting("CheckAuraS", true, { type.value == Type.STRENGTH && offhandStrength.value })
    private val checkWeaponS = setting("CheckWeaponS", false, { type.value == Type.STRENGTH && offhandStrength.value })

    // Crystal
    private val offhandCrystal = setting("OffhandCrystal", false, { type.value == Type.CRYSTAL })
    private val bindCrystal = setting("BindCrystal", Bind(), { type.value == Type.CRYSTAL && offhandCrystal.value })
    private val checkCACrystal = setting("CheckCrystalAuraC", false, { type.value == Type.CRYSTAL && offhandCrystal.value })

    // General
    private val priority = setting("Priority", Priority.HOTBAR)
    private val switchMessage = setting("SwitchMessage", true)

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
                bindTotem.value.isDown(key) -> switchToType(Type.TOTEM)
                bindGapple.value.isDown(key) -> switchToType(Type.GAPPLE)
                bindStrength.value.isDown(key) -> switchToType(Type.STRENGTH)
                bindCrystal.value.isDown(key) -> switchToType(Type.CRYSTAL)
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
            if (player.isDead || !movingTimer.tick(200L, false)) return@safeListener // Delays 4 ticks by default

            if (!player.inventory.itemStack.isEmpty) { // If player is holding an in inventory
                if (mc.currentScreen is GuiContainer) {// If inventory is open (playing moving item)
                    movingTimer.reset() // delay for 5 ticks
                } else { // If inventory is not open (ex. inventory desync)
                    removeHoldingItem()
                }
            } else { // If player is not holding an item in inventory
                switchToType(getType(), true)
            }

            updateDamage()
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

    private fun SafeClientEvent.checkTotem() = CombatUtils.getHealthSmart(player) < hpThreshold.value
        || (checkDamage.value && CombatUtils.getHealthSmart(player) - maxDamage < hpThreshold.value)

    private fun SafeClientEvent.checkGapple() = offhandGapple.value
        && (checkAuraS.value && CombatManager.isActiveAndTopPriority(KillAura)
        || checkWeaponG.value && player.heldItemMainhand.item.isWeapon
        || (checkCAGapple.value && !offhandCrystal.value) && CombatManager.isOnTopPriority(CrystalAura))

    private fun checkCrystal() = offhandCrystal.value
        && checkCACrystal.value && CrystalAura.isEnabled && CombatManager.isOnTopPriority(CrystalAura)

    private fun SafeClientEvent.checkStrength() = offhandStrength.value
        && !player.isPotionActive(MobEffects.STRENGTH)
        && (checkAuraG.value && CombatManager.isActiveAndTopPriority(KillAura)
        || checkWeaponS.value && player.heldItemMainhand.item.isWeapon)

    private fun SafeClientEvent.switchToType(typeOriginal: Type?, alternativeType: Boolean = false) {
        // First check for whether player is holding the right item already or not
        if (typeOriginal != null && !checkOffhandItem(typeOriginal)) {
            getItemSlot(typeOriginal)?.let { (slot, typeAlt) ->
                // Second check is for case of when player ran out of the original type of item
                if (!alternativeType && typeAlt != typeOriginal || checkOffhandItem(typeAlt)) return@let

                transactionLog.clear()
                transactionLog.putAll(moveToSlot(slot.slotNumber, 45).associate { it to false })

                playerController.updateController()
                movingTimer.reset()

                if (switchMessage.value) MessageSendHelper.sendChatMessage("$chatName Offhand now has a ${typeAlt.toString().toLowerCase()}")
            }
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

        return if (priority.value == Priority.HOTBAR) {
            slots.lastOrNull { type.filter(it.stack) }
        } else {
            slots.firstOrNull { type.filter(it.stack) }
        }
    }

    private fun SafeClientEvent.updateDamage() {
        maxDamage = 0f
        if (!checkDamage.value) return

        for (entity in world.loadedEntityList) {
            if (entity.name == player.name) continue
            if (entity !is EntityMob && entity !is EntityPlayer && entity !is EntityEnderCrystal) continue
            if (player.getDistance(entity) > 10f) continue

            if (mob.value && entity is EntityMob) {
                maxDamage = max(calcDamageFromMob(entity), maxDamage)
            }

            if (this@AutoOffhand.player.value && entity is EntityPlayer) {
                maxDamage = max(calcDamageFromPlayer(entity, true), maxDamage)
            }

            if (crystal.value && entity is EntityEnderCrystal) {
                maxDamage = max(calcCrystalDamage(entity, player), maxDamage)
            }
        }

        if (falling.value && nextFallDist > 3.0f) maxDamage = max(ceil(nextFallDist - 3.0f), maxDamage)
    }

    private val SafeClientEvent.nextFallDist get() = player.fallDistance - player.motionY.toFloat()
}