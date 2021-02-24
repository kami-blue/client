package org.kamiblue.client.module.modules.player

import net.minecraft.item.ItemPickaxe
import org.kamiblue.client.mixin.client.MixinMinecraft
import org.kamiblue.client.mixin.client.render.MixinEntityRenderer
import org.kamiblue.client.mixin.client.world.MixinBlockLiquid
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module

/**
 * @see MixinBlockLiquid Liquid Interact
 * @see MixinMinecraft Multi Task
 * @see MixinEntityRenderer No Entity Trace
 */
internal object BlockInteraction : Module(
    name = "BlockInteraction",
    alias = arrayOf("LiquidInteract", "MultiTask", "NoEntityTrace", "NoMiningTrace"),
    category = Category.PLAYER,
    description = "Modifies block interaction"
) {
    private val liquidInteract by setting("Liquid Interact", false, description = "Place block on liquid")
    private val multiTask by setting("Multi Task", true, description = "Breaks block and uses item at the same time")
    private val noEntityTrace by setting("No Entity Trace", true, description = "Interact with blocks through entity")
    private val sneakTrigger by setting("Sneak Trigger", false)
    private val pickaxeOnly by setting("Pickaxe Only", true)

    @JvmStatic
    val isLiquidInteractEnabled
        get() = isEnabled && liquidInteract

    @JvmStatic
    val isMultiTaskEnabled
        get() = isEnabled && multiTask

    @JvmStatic
    val isNoEntityTraceEnabled
        get() = isEnabled && noEntityTrace
            && (!sneakTrigger || mc.player?.isSneaking == true)
            && (!pickaxeOnly || mc.player?.heldItemMainhand?.item is ItemPickaxe)
}