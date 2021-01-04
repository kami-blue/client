package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.mixin.extension.syncCurrentPlayItem
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.combat.CombatUtils
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.block.state.IBlockState
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.Enchantments
import net.minecraftforge.event.entity.player.AttackEntityEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Mouse
import kotlin.math.pow

@Module.Info(
        name = "AutoTool",
        description = "Automatically switch to the best tools when mining or attacking",
        category = Module.Category.MISC
)
object AutoTool : Module() {
    private val switchBack = register(Settings.b("SwitchBack", true))
    private val timeout = register(Settings.integerBuilder("Timeout").withRange(1, 100).withValue(20).withVisibility { switchBack.value })
    private val swapWeapon = register(Settings.b("SwitchWeapon", false))
    private val preferWeapon = register(Settings.e<CombatUtils.PreferWeapon>("Prefer", CombatUtils.PreferWeapon.SWORD))

    private var shouldMoveBack = false
    private var lastSlot = 0
    private var lastChange = 0L

    init {
        listener<LeftClickBlock> {
            if (shouldMoveBack || !switchBack.value) equipBestTool(mc.world.getBlockState(it.pos))
        }

        listener<AttackEntityEvent> {
            if (swapWeapon.value && it.target is EntityLivingBase) CombatUtils.equipBestWeapon(preferWeapon.value)
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

    private fun equip(slot: Int) {
        mc.player.inventory.currentItem = slot
        mc.playerController.syncCurrentPlayItem()
    }

    init {
        switchBack.settingListener = Setting.SettingListeners { if (!switchBack.value) shouldMoveBack = false }
    }
}