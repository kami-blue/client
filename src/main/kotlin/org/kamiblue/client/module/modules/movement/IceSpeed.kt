package org.kamiblue.client.module.modules.movement

import net.minecraft.init.Blocks
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.threads.safeListener

internal object IceSpeed : Module(
    name = "IceSpeed",
    description = "Changes how slippery ice is",
    category = Category.MOVEMENT
) {
    private val slipperiness by setting("Slipperiness", 0.4f, 0.1f..1.0f, 0.01f)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            Blocks.ICE.setDefaultSlipperiness(slipperiness)
            Blocks.PACKED_ICE.setDefaultSlipperiness(slipperiness)
            Blocks.FROSTED_ICE.setDefaultSlipperiness(slipperiness)
        }

        onDisable {
            Blocks.ICE.setDefaultSlipperiness(0.98f)
            Blocks.PACKED_ICE.setDefaultSlipperiness(0.98f)
            Blocks.FROSTED_ICE.setDefaultSlipperiness(0.98f)
        }
    }
}