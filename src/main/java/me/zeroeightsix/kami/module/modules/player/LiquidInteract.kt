package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.mixin.client.world.MixinBlockLiquid
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module

/**
 * @see MixinBlockLiquid
 */
internal object LiquidInteract : Module(
    name = "LiquidInteract",
    category = Category.PLAYER,
    description = "Place blocks on liquid!"
)
