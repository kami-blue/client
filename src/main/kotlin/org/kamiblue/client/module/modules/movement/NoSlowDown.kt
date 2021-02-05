package org.kamiblue.client.module.modules.movement

import net.minecraft.init.Blocks
import net.minecraft.item.*
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerDigging.Action
import net.minecraft.util.EnumFacing
import net.minecraftforge.client.event.InputUpdateEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.mixin.client.world.MixinBlockSoulSand
import org.kamiblue.client.mixin.client.world.MixinBlockWeb
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.EntityUtils.flooredPosition
import org.kamiblue.client.util.threads.safeListener

/**
 * @see MixinBlockSoulSand
 * @see MixinBlockWeb
 */
internal object NoSlowDown : Module(
    name = "NoSlowDown",
    category = Category.MOVEMENT,
    description = "Prevents being slowed down when using an item or going through cobwebs"
) {
    private val ncpStrict by setting("NCP Strict", true)
    private val sneak by setting("Sneak", false)
    val soulSand by setting("Soul Sand", true)
    val cobweb by setting("Cobweb", true)
    private val slime by setting("Slime", true)
    private val allItems by setting("All Items", false)
    private val food by setting("Food", true, { !allItems })
    private val bow by setting("Bows", true, { !allItems })
    private val potion by setting("Potions", true, { !allItems })
    private val shield by setting("Shield", true, { !allItems })

    /*
     * InputUpdateEvent is called just before the player is slowed down @see EntityPlayerSP.onLivingUpdate)
     * We'll abuse this fact, and multiply moveStrafe and moveForward by 5 to nullify the *0.2f hardcoded by Mojang.
     */
    init {
        safeListener<InputUpdateEvent> {
            if (player.isRiding) return@safeListener

            if (sneak && player.isSneaking || passItemCheck(player.activeItemStack.item)) {
                it.movementInput.moveStrafe *= 5f
                it.movementInput.moveForward *= 5f
            }
        }

        /**
         * @author ionar2
         * Used with explicit permission and MIT license permission
         * https://github.com/ionar2/salhack/blob/163f86e/src/main/java/me/ionar/salhack/module/movement/NoSlowModule.java#L175
         */
        safeListener<PacketEvent.PostSend> {
            if (player.isRiding) return@safeListener

            if (ncpStrict && it.packet is CPacketPlayer && passItemCheck(player.activeItemStack.item)) {
                connection.sendPacket(CPacketPlayerDigging(Action.ABORT_DESTROY_BLOCK, player.flooredPosition, EnumFacing.DOWN))
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (slime) Blocks.SLIME_BLOCK.setDefaultSlipperiness(0.4945f)  // normal block speed 0.4945
            else Blocks.SLIME_BLOCK.setDefaultSlipperiness(0.8f)
        }

        onDisable {
            Blocks.SLIME_BLOCK.setDefaultSlipperiness(0.8f)
        }
    }

    private fun SafeClientEvent.passItemCheck(item: Item): Boolean {
        return if (!player.isHandActive) false
        else allItems
            || food && item is ItemFood
            || bow && item is ItemBow
            || potion && item is ItemPotion
            || shield && item is ItemShield
    }
}