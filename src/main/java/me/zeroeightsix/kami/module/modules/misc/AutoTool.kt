package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.mixin.extension.syncCurrentPlayItem
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.combat.CombatUtils
import me.zeroeightsix.kami.util.combat.CombatUtils.equipBestWeapon
import me.zeroeightsix.kami.util.items.swapToSlot
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.block.state.IBlockState
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.Enchantments
import net.minecraftforge.event.entity.player.AttackEntityEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Mouse
import kotlin.math.pow

internal object AutoTool : Module(
    name = "AutoTool",
    description = "Automatically switch to the best tools when mining or attacking",
    category = Category.MISC
) {
    private val switchBack = setting("SwitchBack", true)
    private val timeout = setting("Timeout", 20, 1..100, 5, { switchBack.value })
    private val swapWeapon = setting("SwitchWeapon", false)
    private val preferWeapon = setting("Prefer", CombatUtils.PreferWeapon.SWORD)

    private var shouldMoveBack = false
    private var lastSlot = 0
    private var lastChange = 0L

    init {
        safeListener<LeftClickBlock> {
            if (shouldMoveBack || !switchBack.value) equipBestTool(world.getBlockState(it.pos))
        }

        safeListener<AttackEntityEvent> {
            if (swapWeapon.value && it.target is EntityLivingBase) equipBestWeapon(preferWeapon.value)
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (mc.currentScreen != null || !switchBack.value) return@safeListener

            val mouse = Mouse.isButtonDown(0)
            if (mouse && !shouldMoveBack) {
                lastChange = System.currentTimeMillis()
                shouldMoveBack = true
                lastSlot = player.inventory.currentItem
                playerController.syncCurrentPlayItem()
            } else if (!mouse && shouldMoveBack && (lastChange + timeout.value * 10 < System.currentTimeMillis())) {
                shouldMoveBack = false
                player.inventory.currentItem = lastSlot
                playerController.syncCurrentPlayItem()
            }
        }
    }

    fun SafeClientEvent.equipBestTool(blockState: IBlockState) {
        var bestSlot = -1
        var max = 0.0

        for (i in 0..8) {
            val stack = player.inventory.getStackInSlot(i)
            if (stack.isEmpty) continue
            var speed = stack.getDestroySpeed(blockState)
            var eff: Int

            if (speed > 1) {
                speed += (
                    if (EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack).also { eff = it } > 0.0) eff.toDouble().pow(2.0) + 1
                    else 0.0
                    ).toFloat()
                if (speed > max) {
                    max = speed.toDouble()
                    bestSlot = i
                }
            }

        }

        if (bestSlot != -1) swapToSlot(bestSlot)
    }

    init {
        switchBack.listeners.add { if (!switchBack.value) shouldMoveBack = false }
    }
}