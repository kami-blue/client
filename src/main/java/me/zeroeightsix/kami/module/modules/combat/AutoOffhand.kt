package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.mangers.CombatManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.combat.CombatUtils
import me.zeroeightsix.kami.util.combat.CrystalUtils
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemSword
import net.minecraft.network.play.server.SPacketConfirmTransaction
import kotlin.math.max

@Module.Info(
        name = "AutoOffhand",
        description = "Manages item in your offhand",
        category = Module.Category.COMBAT
)
object AutoOffhand : Module() {
    private val type = register(Settings.enumBuilder(Type::class.java, "Type").withValue(Type.TOTEM))

    // Totem
    private val hpThreshold = register(Settings.floatBuilder("HpThreshold").withValue(5f).withRange(1f, 20f).withVisibility { type.value == Type.TOTEM })
    private val checkDamage = register(Settings.booleanBuilder("CheckDamage").withValue(true).withVisibility { type.value == Type.TOTEM })
    private val mob = register(Settings.booleanBuilder("Mob").withValue(true).withVisibility { type.value == Type.TOTEM && checkDamage.value })
    private val player = register(Settings.booleanBuilder("Player").withValue(true).withVisibility { type.value == Type.TOTEM && checkDamage.value })
    private val crystal = register(Settings.booleanBuilder("Crystal").withValue(true).withVisibility { type.value == Type.TOTEM && checkDamage.value })

    // Gapple
    private val offhandGapple = register(Settings.booleanBuilder("OffhandGapple").withValue(false).withVisibility { type.value == Type.GAPPLE })
    private val checkAura = register(Settings.booleanBuilder("CheckAura").withValue(true).withVisibility { type.value == Type.GAPPLE && offhandGapple.value })
    private val checkWeapon = register(Settings.booleanBuilder("CheckWeapon").withValue(false).withVisibility { type.value == Type.GAPPLE && offhandGapple.value })
    private val checkCAGapple = register(Settings.booleanBuilder("CheckCrystalAura").withValue(true).withVisibility { type.value == Type.GAPPLE && offhandGapple.value && !offhandCrystal.value })

    // Crystal
    private val offhandCrystal = register(Settings.booleanBuilder("OffhandCrystal").withValue(false).withVisibility { type.value == Type.CRYSTAL })
    private val checkCACrystal = register(Settings.booleanBuilder("CheckCrystalAura").withValue(false).withVisibility { type.value == Type.CRYSTAL && offhandCrystal.value })

    private enum class Type(val itemId: Int) {
        TOTEM(449),
        GAPPLE(322),
        CRYSTAL(426)
    }

    private val transactionLog = HashMap<Short, Boolean>()
    private val movingTimer = TimerUtils.TickTimer()
    private var maxDamage = 0f

    init {
        listener<PacketEvent.Receive> {
            if (mc.player == null || it.packet !is SPacketConfirmTransaction || it.packet.windowId != 0 || !transactionLog.containsKey(it.packet.actionNumber)) return@listener
            transactionLog[it.packet.actionNumber] = it.packet.wasAccepted()
            if (!transactionLog.containsValue(false)) movingTimer.reset(-200L) // If all the click packets were accepted then we reset the timer for next moving
        }
    }

    override fun onRender() {
        if (mc.player == null || mc.player.isDead || !movingTimer.tick(200L, false)) return // Delay 4 ticks by default
        if (!mc.player.inventory.getItemStack().isEmpty()) { // If player is holding an in inventory
            if (mc.currentScreen is GuiContainer) {// If inventory is open (playing moving item)
                movingTimer.reset(-150) // delay for 1 tick
            } else { // If inventory is not open (ex. inventory desync)
                InventoryUtils.removeHoldingItem()
            }
        } else { // If player is not holding an item in inventory
            val type = when {
                checkTotem() -> Type.TOTEM
                checkCrystal() -> Type.CRYSTAL
                checkGapple() -> Type.GAPPLE
                mc.player.heldItemOffhand.isEmpty() -> Type.TOTEM
                else -> null
            }
            if (type != null && !checkOffhandItem(type)) getItemSlot(type)?.let { slot ->
                transactionLog.clear()
                transactionLog.putAll(InventoryUtils.moveToSlot(0, slot, 45).associate { it to false })
                transactionLog[InventoryUtils.quickMoveSlot(0, slot)] = false
                mc.playerController.updateController()
                movingTimer.reset()
            } ?: movingTimer.reset(-150) // Delay 1 tick if can't find an item
        }
    }

    override fun onUpdate(event: SafeTickEvent) {
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
    }

    private fun checkTotem() = mc.player.health < hpThreshold.value
            || (checkDamage.value && mc.player.absorptionAmount + mc.player.health - maxDamage < hpThreshold.value)

    private fun checkGapple(): Boolean {
        val item = mc.player.heldItemMainhand.getItem()
        return offhandGapple.value
                && (checkAura.value && CombatManager.isActiveAndTopPriority(Aura)
                || checkWeapon.value && (item is ItemSword || item is ItemAxe)
                || (checkCAGapple.value && !offhandCrystal.value) && CombatManager.isActiveAndTopPriority(CrystalAura))
    }

    private fun checkCrystal() = offhandCrystal.value && checkCACrystal.value && CrystalAura.isEnabled && CombatManager.isOnTopPriority(CrystalAura)

    private fun checkOffhandItem(type: Type) = Item.getIdFromItem(mc.player.heldItemOffhand.getItem()) == type.itemId

    private fun getItemSlot(type: Type): Int? = InventoryUtils.getSlotsFullInv(itemId = type.itemId)?.get(0)
            ?: if (type == Type.CRYSTAL) InventoryUtils.getSlotsFullInv(itemId = 449)?.get(0) else getItemSlot(getNextType(type))

    private fun getNextType(type: Type) = with(Type.values()) { this[(type.ordinal + 1) % this.size] }
}