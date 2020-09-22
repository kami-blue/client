package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.manager.mangers.CombatManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.math.RotationUtils.faceEntityClosest
import net.minecraft.init.Items

@Module.Info(
        name = "AimBot",
        description = "Automatically aims at entities for you.",
        category = Module.Category.COMBAT,
        modulePriority = 20
)
object AimBot : Module() {
    private val bowOnly = register(Settings.b("BowOnly", true))
    private val autoSwap = register(Settings.booleanBuilder("AutoSwap").withValue(false).withVisibility { bowOnly.value })

    override fun onUpdate() {
        if (bowOnly.value && mc.player.heldItemMainhand.getItem() != Items.BOW) {
            if (autoSwap.value) InventoryUtils.swapSlotToItem(261)
            return
        }
        CombatManager.target?.let {
            faceEntityClosest(it)
        }
    }
}