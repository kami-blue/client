package me.zeroeightsix.kami.module.modules.misc

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.combat.Aura.HitMode
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.block.state.IBlockState
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EnumCreatureAttribute
import net.minecraft.init.Enchantments
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemTool
import net.minecraftforge.event.entity.player.AttackEntityEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock
import org.lwjgl.input.Mouse
import kotlin.math.pow

@Module.Info(
        name = "AutoTool",
        description = "Automatically switch to the best tools when mining or attacking",
        category = Module.Category.MISC
)
object AutoTool : Module() {
    private val switchBack = register(Settings.b("SwitchBack", true))
    private val timeout = register(Settings.integerBuilder("Timeout").withRange(1, 100).withValue(20).withVisibility { switchBack.value }.build())
    private val preferTool = register(Settings.e<HitMode>("Prefer", HitMode.SWORD))

    private var shouldMoveBack = false
    private var lastSlot = 0
    private var lastChange = 0L

    @EventHandler
    private val leftClickListener = Listener(EventHook { event: LeftClickBlock -> if (shouldMoveBack || !switchBack.value) equipBestTool(mc.world.getBlockState(event.pos)) })

    @EventHandler
    private val attackListener = Listener(EventHook { event: AttackEntityEvent? -> equipBestWeapon(preferTool.value) })

    override fun onUpdate() {
        if (mc.currentScreen != null || !switchBack.value) return

        val mouse = Mouse.isButtonDown(0)
        if (mouse && !shouldMoveBack) {
            lastChange = System.currentTimeMillis()
            shouldMoveBack = true
            lastSlot = mc.player.inventory.currentItem
            mc.playerController.syncCurrentPlayItem()
        } else if (!mouse && shouldMoveBack && (lastChange + timeout.value * 10 < System.currentTimeMillis())) {
            shouldMoveBack = false
            mc.player.inventory.currentItem = lastSlot
            mc.playerController.syncCurrentPlayItem()
        }
    }

    private fun equipBestTool(blockState: IBlockState) {
        var bestSlot = -1
        var max = 0.0

        for (i in 0..8) {
            val stack = mc.player.inventory.getStackInSlot(i)
            if (stack.isEmpty) continue
            var speed = stack.getDestroySpeed(blockState)
            var eff: Int

            if (speed > 1) {
                speed += (if (EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack).also { eff = it } > 0.0) eff.toDouble().pow(2.0) + 1 else 0.0).toFloat()
                if (speed > max) {
                    max = speed.toDouble()
                    bestSlot = i
                }
            }
        }
        if (bestSlot != -1) equip(bestSlot)
    }

    @JvmStatic
    fun equipBestWeapon(hitMode: HitMode) {
        var bestSlot = -1
        var maxDamage = 0.0
        for (i in 0..8) {
            val stack = mc.player.inventory.getStackInSlot(i)
            if (stack.isEmpty) continue
            if (stack.getItem() !is ItemAxe && hitMode == HitMode.AXE) continue
            if (stack.getItem() !is ItemSword && hitMode == HitMode.SWORD) continue

            if (stack.getItem() is ItemSword && (hitMode == HitMode.SWORD || hitMode == HitMode.NONE)) {
                val damage = (stack.getItem() as ItemSword).attackDamage + EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED).toDouble()
                if (damage > maxDamage) {
                    maxDamage = damage
                    bestSlot = i
                }
            } else if (stack.getItem() is ItemAxe && (hitMode == HitMode.AXE || hitMode == HitMode.NONE)) {
                val damage = (stack.getItem() as ItemTool).attackDamage + EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED).toDouble()
                if (damage > maxDamage) {
                    maxDamage = damage
                    bestSlot = i
                }
            } else if (stack.getItem() is ItemTool) {
                val damage = (stack.getItem() as ItemTool).attackDamage + EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED).toDouble()
                if (damage > maxDamage) {
                    maxDamage = damage
                    bestSlot = i
                }
            }
        }
        if (bestSlot != -1) equip(bestSlot)
    }

    private fun equip(slot: Int) {
        mc.player.inventory.currentItem = slot
        mc.playerController.syncCurrentPlayItem()
    }

    init {
        switchBack.settingListener = Setting.SettingListeners { if (!switchBack.value) shouldMoveBack = false }
    }
}