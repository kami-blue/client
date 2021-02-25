package org.kamiblue.client.module.modules.player

import org.kamiblue.client.mixin.client.world.MixinBlockLiquid
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module

/**
 * @see MixinBlockLiquid
 */
internal object LiquidInteract : Module(
    name = "LiquidInteract",
    category = Category.PLAYER,
    description = "Place blocks on liquid!"
)
