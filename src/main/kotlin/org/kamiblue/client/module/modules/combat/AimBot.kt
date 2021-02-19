package org.kamiblue.client.module.modules.combat

import net.minecraft.init.Items
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.manager.managers.CombatManager
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.items.swapToItem
import org.kamiblue.client.util.math.RotationUtils.faceEntityClosest
import org.kamiblue.client.util.threads.safeListener

@CombatManager.CombatModule
internal object AimBot : Module(
    name = "AimBot",
    description = "Automatically aims at entities for you.",
    category = Category.COMBAT,
    modulePriority = 20
) {
    private val bowOnly by setting("Bow Only", true)
    private val autoSwap by setting("Auto Swap", false)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (player.heldItemMainhand.item != Items.BOW) {
                if (autoSwap) swapToItem(Items.BOW)
                if (bowOnly) return@safeListener
            }

            CombatManager.target?.let {
                faceEntityClosest(it)
            }
        }
    }
}