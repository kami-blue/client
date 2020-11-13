package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.managers.CombatManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.combat.CombatUtils
import me.zeroeightsix.kami.util.combat.CrystalUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.network.play.server.SPacketConfirmTransaction
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Keyboard
import kotlin.math.ceil
import kotlin.math.max

@Module.Info(
        name = "AutoOffhand",
        description = "Manages item in your offhand",
        category = Module.Category.COMBAT
)
object AutoOffhand : Module() {
    private val type = setting("Type", Type.TOTEM)

    // Totem
    private val hpThreshold = setting("HpThreshold", 5f, 1f..20f, 0.5f, { type.value == Type.TOTEM })
    private val bindTotem = setting("BindTotem", { type.value == Type.TOTEM })
    private val checkDamage = setting("CheckDamage", true, { type.value == Type.TOTEM })
    private val mob = setting("Mob", true, { type.value == Type.TOTEM && checkDamage.value })
    private val player = setting("Player", true, { type.value == Type.TOTEM && checkDamage.value })
    private val crystal = setting("Crystal", true, { type.value == Type.TOTEM && checkDamage.value })
    private val falling = setting("Falling", true, { type.value == Type.TOTEM && checkDamage.value })

    // Gapple
    private val offhandGapple = setting("OffhandGapple", false, { type.value == Type.GAPPLE })
    private val bindGapple = setting("BindGapple", { type.value == Type.GAPPLE && offhandGapple.value })
    private val checkAura = setting("CheckAura", true, { type.value == Type.GAPPLE && offhandGapple.value })
    private val checkWeapon = setting("CheckWeapon", false, { type.value == Type.GAPPLE && offhandGapple.value })
    private val checkCAGapple = setting("CheckCrystalAura", true, { type.value == Type.GAPPLE && offhandGapple.value && !offhandCrystal.value })

    // Crystal
    private val offhandCrystal = setting("OffhandCrystal", false, { type.value == Type.CRYSTAL })
    private val bindCrystal = setting("BindCrystal", { type.value == Type.CRYSTAL && offhandCrystal.value })
    private val checkCACrystal = setting("CheckCrystalAura", false, { type.value == Type.CRYSTAL && offhandCrystal.value })

    // General
    private val priority = setting("Priority", Priority.HOTBAR)
    private val switchMessage = setting("SwitchMessage", true)

    private enum class Type(val itemId: Int) {
        TOTEM(449),
        GAPPLE(322),
        CRYSTAL(426)
    }

    @Suppress("UNUSED")
    private enum class Priority {
        HOTBAR, INVENTORY
    }

    private val transactionLog = HashMap<Short, Boolean>()
    private val movingTimer = TimerUtils.TickTimer()
    private var maxDamage = 0f

    init {
        listener<InputEvent.KeyInputEvent> {
            when {
                bindTotem.value.isDown(Keyboard.getEventKey()) -> switchToType(Type.TOTEM)
                bindGapple.value.isDown(Keyboard.getEventKey()) -> switchToType(Type.GAPPLE)
                bindCrystal.value.isDown(Keyboard.getEventKey()) -> switchToType(Type.CRYSTAL)
            }
        }

        listener<PacketEvent.Receive> {
            if (mc.player == null || it.packet !is SPacketConfirmTransaction || it.packet.windowId != 0 || !transactionLog.containsKey(it.packet.actionNumber)) return@listener
            transactionLog[it.packet.actionNumber] = it.packet.wasAccepted()
            if (!transactionLog.containsValue(false)) movingTimer.reset(-175L) // If all the click packets were accepted then we reset the timer for next moving
        }

        listener<SafeTickEvent>(1100) {
            if (mc.player.isDead || !movingTimer.tick(200L, false)) return@listener // Delays 4 ticks by default
            if (!mc.player.inventory.getItemStack().isEmpty()) { // If player is holding an in inventory
                if (mc.currentScreen is GuiContainer) {// If inventory is open (playing moving item)
                    movingTimer.reset() // delay for 5 ticks
                } else { // If inventory is not open (ex. inventory desync)
                    InventoryUtils.removeHoldingItem()
                }
            } else { // If player is not holding an item in inventory
                switchToType(getType(), true)
            }
            updateDamage()
        }
    }

    private fun getType() = when {
        checkTotem() -> Type.TOTEM
        checkGapple() -> Type.GAPPLE
        checkCrystal() -> Type.CRYSTAL
        mc.player.heldItemOffhand.isEmpty() -> Type.TOTEM
        else -> null
    }

    private fun switchToType(type1: Type?, alternativeType: Boolean = false) {
        // First check for whether player is holding the right item already or not
        if (type1 != null && !checkOffhandItem(type1)) getItemSlot(type1)?.let { (slot, type2) ->
            // Second check is for case of when player ran out of the original type of item
            if ((!alternativeType && type2 != type1) || slot == 45 || checkOffhandItem(type2)) return@let
            transactionLog.clear()
            transactionLog.putAll(InventoryUtils.moveToSlot(0, slot, 45).associate { it to false })
            mc.playerController.updateController()
            movingTimer.reset()
            if (switchMessage.value) MessageSendHelper.sendChatMessage("$chatName Offhand now has a ${type2.toString().toLowerCase()}")
        }
    }

    private fun checkTotem() = CombatUtils.getHealthSmart(mc.player) < hpThreshold.value
            || (checkDamage.value && CombatUtils.getHealthSmart(mc.player) - maxDamage < hpThreshold.value)

    private fun checkGapple(): Boolean {
        val item = mc.player.heldItemMainhand.getItem()
        return offhandGapple.value
                && (checkAura.value && CombatManager.isActiveAndTopPriority(KillAura)
                || checkWeapon.value && (item is ItemSword || item is ItemAxe)
                || (checkCAGapple.value && !offhandCrystal.value) && CombatManager.isOnTopPriority(CrystalAura))
    }

    private fun checkCrystal() = offhandCrystal.value && checkCACrystal.value && CrystalAura.isEnabled && CombatManager.isOnTopPriority(CrystalAura)

    private fun checkOffhandItem(type: Type) = Item.getIdFromItem(mc.player.heldItemOffhand.getItem()) == type.itemId

    private fun getItemSlot(type: Type, loopTime: Int = 1): Pair<Int, Type>? = getSlot(type.itemId)?.to(type)
            ?: if (loopTime <= 3) getItemSlot(getNextType(type), loopTime + 1)
            else null

    private fun getSlot(itemId: Int): Int? {
        val sublist = mc.player.inventoryContainer.inventory.subList(9, 46)
        val filter = getFilter(itemId)
        // 9 - 35 are main inventory, 36 - 44 are hotbar. So finding last one will result in prioritize hotbar
        val slot = if (priority.value == Priority.HOTBAR) sublist.indexOfLast(filter) else sublist.indexOfFirst(filter)
        // Add 9 to it because it is the sub list's index
        return if (slot != -1) slot + 9 else null
    }

    private fun getFilter(itemId: Int): (ItemStack) -> Boolean = { Item.getIdFromItem(it.getItem()) == itemId }

    private fun getNextType(type: Type) = with(Type.values()) { this[(type.ordinal + 1) % this.size] }

    private fun updateDamage() {
        maxDamage = 0f
        if (!checkDamage.value) return
        for (entity in mc.world.loadedEntityList) {
            if (entity.name == mc.player.name) continue
            if (entity !is EntityMob && entity !is EntityPlayer && entity !is EntityEnderCrystal) continue
            if (mc.player.getDistance(entity) > 10f) continue
            if (mob.value && entity is EntityMob) {
                maxDamage = max(CombatUtils.calcDamageFromMob(entity), maxDamage)
            }
            if (player.value && entity is EntityPlayer) {
                maxDamage = max(CombatUtils.calcDamageFromPlayer(entity), maxDamage)
            }
            if (crystal.value && entity is EntityEnderCrystal) {
                maxDamage = max(CrystalUtils.calcDamage(entity, mc.player), maxDamage)
            }
        }
        if (falling.value && nextFallDist > 3.0f) maxDamage = max(ceil(nextFallDist - 3.0f), maxDamage)
    }

    private val nextFallDist get() = mc.player.fallDistance - mc.player.motionY.toFloat()
}