package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.manager.managers.CombatManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.combat.CombatUtils
import me.zeroeightsix.kami.util.combat.CrystalUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects
import net.minecraft.item.*
import net.minecraft.network.play.server.SPacketConfirmTransaction
import net.minecraft.potion.PotionUtils
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Keyboard
import kotlin.math.ceil
import kotlin.math.max

@Module.Info(
    name = "AutoOffhand",
    description = "Manages item in your offhand",
    category = Module.Category.COMBAT
)
object AutoOffhand : Module() {
    private val type = register(Settings.enumBuilder(Type::class.java, "Type").withValue(Type.TOTEM))

    // Totem
    private val hpThreshold = register(Settings.floatBuilder("HpThreshold").withValue(5f).withRange(1f, 20f).withStep(0.5f).withVisibility { type.value == Type.TOTEM })
    private val bindTotem = register(Settings.custom("BindTotem", Bind.none(), BindConverter()).withVisibility { type.value == Type.TOTEM })
    private val checkDamage = register(Settings.booleanBuilder("CheckDamage").withValue(true).withVisibility { type.value == Type.TOTEM })
    private val mob = register(Settings.booleanBuilder("Mob").withValue(true).withVisibility { type.value == Type.TOTEM && checkDamage.value })
    private val player = register(Settings.booleanBuilder("Player").withValue(true).withVisibility { type.value == Type.TOTEM && checkDamage.value })
    private val crystal = register(Settings.booleanBuilder("Crystal").withValue(true).withVisibility { type.value == Type.TOTEM && checkDamage.value })
    private val falling = register(Settings.booleanBuilder("Falling").withValue(true).withVisibility { type.value == Type.TOTEM && checkDamage.value })

    // Gapple
    private val offhandGapple = register(Settings.booleanBuilder("OffhandGapple").withValue(false).withVisibility { type.value == Type.GAPPLE })
    private val bindGapple = register(Settings.custom("BindGapple", Bind.none(), BindConverter()).withVisibility { type.value == Type.GAPPLE && offhandGapple.value })
    private val checkAuraG = register(Settings.booleanBuilder("CheckAuraG").withValue(true).withVisibility { type.value == Type.GAPPLE && offhandGapple.value })
    private val checkWeaponG = register(Settings.booleanBuilder("CheckWeaponG").withValue(false).withVisibility { type.value == Type.GAPPLE && offhandGapple.value })
    private val checkCAGapple = register(Settings.booleanBuilder("CheckCrystalAuraG").withValue(true).withVisibility { type.value == Type.GAPPLE && offhandGapple.value && !offhandCrystal.value })

    // Strength
    private val offhandStrength = register(Settings.booleanBuilder("OffhandStrength").withValue(false).withVisibility { type.value == Type.STRENGTH })
    private val bindStrength = register(Settings.custom("BindStrength", Bind.none(), BindConverter()).withVisibility { type.value == Type.STRENGTH && offhandStrength.value })
    private val checkAuraS = register(Settings.booleanBuilder("CheckAuraS").withValue(true).withVisibility { type.value == Type.STRENGTH && offhandStrength.value })
    private val checkWeaponS = register(Settings.booleanBuilder("CheckWeaponS").withValue(false).withVisibility { type.value == Type.STRENGTH && offhandStrength.value })

    // Crystal
    private val offhandCrystal = register(Settings.booleanBuilder("OffhandCrystal").withValue(false).withVisibility { type.value == Type.CRYSTAL })
    private val bindCrystal = register(Settings.custom("BindCrystal", Bind.none(), BindConverter()).withVisibility { type.value == Type.CRYSTAL && offhandCrystal.value })
    private val checkCACrystal = register(Settings.booleanBuilder("CheckCrystalAuraC").withValue(false).withVisibility { type.value == Type.CRYSTAL && offhandCrystal.value })

    // General
    private val priority = register(Settings.enumBuilder(Priority::class.java, "Priority").withValue(Priority.HOTBAR))
    private val switchMessage = register(Settings.b("SwitchMessage", true))

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
        listener<InputEvent.KeyInputEvent> {
            val key = Keyboard.getEventKey()
            when {
                bindTotem.value.isDown(key) -> switchToType(Type.TOTEM)
                bindGapple.value.isDown(key) -> switchToType(Type.GAPPLE)
                bindStrength.value.isDown(key) -> switchToType(Type.STRENGTH)
                bindCrystal.value.isDown(key) -> switchToType(Type.CRYSTAL)
            }
        }

        listener<PacketEvent.Receive> {
            if (mc.player == null || it.packet !is SPacketConfirmTransaction || it.packet.windowId != 0 || !transactionLog.containsKey(it.packet.actionNumber)) return@listener
            transactionLog[it.packet.actionNumber] = it.packet.wasAccepted()
            if (!transactionLog.containsValue(false)) movingTimer.reset(-175L) // If all the click packets were accepted then we reset the timer for next moving
        }

        safeListener<TickEvent.ClientTickEvent>(1100) {
            if (player.isDead || !movingTimer.tick(200L, false)) return@safeListener // Delays 4 ticks by default
            if (!player.inventory.itemStack.isEmpty) { // If player is holding an in inventory
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
        checkStrength() -> Type.STRENGTH
        checkGapple() -> Type.GAPPLE
        checkCrystal() -> Type.CRYSTAL
        mc.player.heldItemOffhand.isEmpty -> Type.TOTEM
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

    private fun checkGapple() = offhandGapple.value
        && (checkAuraS.value && CombatManager.isActiveAndTopPriority(KillAura)
        || checkWeaponG.value && mc.player.heldItemMainhand.item.isWeapon
        || (checkCAGapple.value && !offhandCrystal.value) && CombatManager.isOnTopPriority(CrystalAura))

    private fun checkCrystal() = offhandCrystal.value
        && checkCACrystal.value && CrystalAura.isEnabled && CombatManager.isOnTopPriority(CrystalAura)

    private fun checkStrength() = offhandStrength.value
        && !mc.player.isPotionActive(MobEffects.STRENGTH)
        && (checkAuraG.value && CombatManager.isActiveAndTopPriority(KillAura)
        || checkWeaponS.value && mc.player.heldItemMainhand.item.isWeapon)

    private fun checkOffhandItem(type: Type) = type.filter(mc.player.heldItemOffhand)

    private fun getItemSlot(type: Type, loopTime: Int = 1): Pair<Int, Type>? = getSlot(type)?.to(type)
        ?: if (loopTime <= 3) getItemSlot(getNextType(type), loopTime + 1)
        else null

    private fun getSlot(type: Type): Int? {
        val sublist = mc.player.inventoryContainer.inventory.subList(9, 46)

        // 9 - 35 are main inventory, 36 - 44 are hotbar. So finding last one will result in prioritize hotbar
        val slot = if (priority.value == Priority.HOTBAR) sublist.indexOfLast(type.filter)
        else sublist.indexOfFirst(type.filter)

        // Add 9 to it because it is the sub list's index
        return if (slot != -1) slot + 9 else null
    }

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