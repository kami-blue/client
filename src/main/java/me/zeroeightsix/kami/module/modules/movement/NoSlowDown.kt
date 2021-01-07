package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.mixin.client.world.MixinBlockSoulSand
import me.zeroeightsix.kami.mixin.client.world.MixinBlockWeb
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.init.Blocks
import net.minecraft.item.*
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerDigging.Action
import net.minecraft.util.EnumFacing
import net.minecraftforge.client.event.InputUpdateEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener

/**
 * @see MixinBlockSoulSand
 * @see MixinBlockWeb
 */
object NoSlowDown : Module(
    name = "NoSlowDown",
    category = Category.MOVEMENT,
    description = "Prevents being slowed down when using an item or going through cobwebs"
) {
    private val ncpStrict = setting("NCPStrict", true)
    private val sneak = setting("Sneak", true)
    val soulSand = setting("SoulSand", true)
    val cobweb = setting("Cobweb", true)
    private val slime = setting("Slime", true)
    private val allItems = setting("AllItems", false)
    private val food = setting("Food", true, { !allItems.value })
    private val bow = setting("Bows", true, { !allItems.value })
    private val potion = setting("Potions", true, { !allItems.value })
    private val shield = setting("Shield", true, { !allItems.value })

    /*
     * InputUpdateEvent is called just before the player is slowed down @see EntityPlayerSP.onLivingUpdate)
     * We'll abuse this fact, and multiply moveStrafe and moveForward by 5 to nullify the *0.2f hardcoded by Mojang.
     */
    init {
        listener<InputUpdateEvent> {
            if ((passItemCheck(mc.player.activeItemStack.getItem()) || (mc.player.isSneaking && sneak.value)) && !mc.player.isRiding) {
                it.movementInput.moveStrafe *= 5f
                it.movementInput.moveForward *= 5f
            }
        }

        /**
         * @author ionar2
         * Used with explicit permission and MIT license permission
         * https://github.com/ionar2/salhack/blob/163f86e/src/main/java/me/ionar/salhack/module/movement/NoSlowModule.java#L175
         */
        listener<PacketEvent.PostSend> {
            if (ncpStrict.value && it.packet is CPacketPlayer && passItemCheck(mc.player.activeItemStack.getItem()) && !mc.player.isRiding) {
                mc.player.connection.sendPacket(CPacketPlayerDigging(Action.ABORT_DESTROY_BLOCK, mc.player.positionVector.toBlockPos(), EnumFacing.DOWN))
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            @Suppress("DEPRECATION")
            if (slime.value) Blocks.SLIME_BLOCK.slipperiness = 0.4945f // normal block speed 0.4945
            else Blocks.SLIME_BLOCK.slipperiness = 0.8f
        }
    }

    override fun onDisable() {
        @Suppress("DEPRECATION")
        Blocks.SLIME_BLOCK.slipperiness = 0.8f
    }

    private fun passItemCheck(item: Item): Boolean {
        return if (!mc.player.isHandActive) false
        else allItems.value
                || food.value && item is ItemFood
                || bow.value && item is ItemBow
                || potion.value && item is ItemPotion
                || shield.value && item is ItemShield
    }
}