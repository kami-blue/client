package org.kamiblue.client.util

import baritone.api.BaritoneAPI
import com.google.common.collect.ImmutableSet
import net.minecraft.block.Block
import net.minecraft.init.Blocks

object BaritoneUtils {
    var initialized = false

    val provider get() = if (initialized) BaritoneAPI.getProvider() else null
    val settings get() = if (initialized) BaritoneAPI.getSettings() else null
    val primary get() = provider?.primaryBaritone
    val prefix get() = settings?.prefix?.value ?: "#"

    val isPathing get() = primary?.pathingBehavior?.isPathing ?: false
    val isActive
        get() = primary?.customGoalProcess?.isActive ?: false
            || primary?.pathingControlManager?.mostRecentInControl()?.orElse(null)?.isActive ?: false

    fun cancelEverything() = primary?.pathingBehavior?.cancelEverything()

    val baritoneCachedBlocks: ImmutableSet<Block> = ImmutableSet.of(
        Blocks.DIAMOND_BLOCK,
        Blocks.COAL_BLOCK,
        Blocks.IRON_BLOCK,
        Blocks.GOLD_BLOCK,
        Blocks.EMERALD_ORE,
        Blocks.EMERALD_BLOCK,
        Blocks.ENDER_CHEST,
        Blocks.FURNACE,
        Blocks.CHEST,
        Blocks.TRAPPED_CHEST,
        Blocks.END_PORTAL,
        Blocks.END_PORTAL_FRAME,
        Blocks.MOB_SPAWNER,
        Blocks.BARRIER,
        Blocks.OBSERVER,
        Blocks.WHITE_SHULKER_BOX,
        Blocks.ORANGE_SHULKER_BOX,
        Blocks.MAGENTA_SHULKER_BOX,
        Blocks.LIGHT_BLUE_SHULKER_BOX,
        Blocks.YELLOW_SHULKER_BOX,
        Blocks.LIME_SHULKER_BOX,
        Blocks.PINK_SHULKER_BOX,
        Blocks.GRAY_SHULKER_BOX,
        Blocks.SILVER_SHULKER_BOX,
        Blocks.CYAN_SHULKER_BOX,
        Blocks.PURPLE_SHULKER_BOX,
        Blocks.BLUE_SHULKER_BOX,
        Blocks.BROWN_SHULKER_BOX,
        Blocks.GREEN_SHULKER_BOX,
        Blocks.RED_SHULKER_BOX,
        Blocks.BLACK_SHULKER_BOX,
        Blocks.PORTAL,
        Blocks.HOPPER,
        Blocks.BEACON,
        Blocks.BREWING_STAND,
        Blocks.SKULL,
        Blocks.ENCHANTING_TABLE,
        Blocks.ANVIL,
        Blocks.LIT_FURNACE,
        Blocks.BED,
        Blocks.DRAGON_EGG,
        Blocks.JUKEBOX,
        Blocks.END_GATEWAY,
        Blocks.WEB,
        Blocks.NETHER_WART,
        Blocks.LADDER,
        Blocks.VINE
    )

}