package me.zeroeightsix.kami.module.modules.movement

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.math.MathUtils
import net.minecraft.init.Blocks
import net.minecraft.item.*
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerDigging.Action
import net.minecraft.util.EnumFacing
import net.minecraftforge.client.event.InputUpdateEvent


/**
 * @see me.zeroeightsix.kami.mixin.client.MixinBlockSoulSand
 *
 * @see net.minecraft.client.entity.EntityPlayerSP.onLivingUpdate
 */
@Module.Info(
        name = "NoSlowDown",
        category = Module.Category.MOVEMENT,
        description = "Prevents being slowed down when using an item or going through cobwebs"
)
object NoSlowDown : Module() {
    private val ncpStrict = register(Settings.b("NCPStrict", true))
    private val sneak = register(Settings.b("Sneak", true))
    val soulSand = register(Settings.b("SoulSand", true))
    val cobweb = register(Settings.b("Cobweb", true))
    private val slime = register(Settings.b("Slime", true))
    private val allItems = register(Settings.b("AllItems", false))
    private val food = register(Settings.booleanBuilder().withName("Food").withValue(true).withVisibility { !allItems.value }.build())
    private val bow = register(Settings.booleanBuilder().withName("Bows").withValue(true).withVisibility { !allItems.value }.build())
    private val potion = register(Settings.booleanBuilder().withName("Potions").withValue(true).withVisibility { !allItems.value }.build())
    private val shield = register(Settings.booleanBuilder().withName("Shield").withValue(true).withVisibility { !allItems.value }.build())

    /*
     * InputUpdateEvent is called just before the player is slowed down @see EntityPlayerSP.onLivingUpdate)
     * We'll abuse this fact, and multiply moveStrafe and moveForward by 5 to nullify the *0.2f hardcoded by Mojang.
     */
    @EventHandler
    private val eventListener = Listener(EventHook { event: InputUpdateEvent ->
        if ((passItemCheck(mc.player.activeItemStack.getItem()) || (mc.player.isSneaking && sneak.value)) && !mc.player.isRiding) {
            event.movementInput.moveStrafe *= 5f
            event.movementInput.moveForward *= 5f
        }
    })

    /**
     * @author ionar2
     * Used with explicit permission and MIT license permission
     * https://github.com/ionar2/salhack/blob/163f86e/src/main/java/me/ionar/salhack/module/movement/NoSlowModule.java#L175
     */
    @EventHandler
    private val receivedEvent = Listener(EventHook { event: PacketEvent.PostSend ->
        if (ncpStrict.value && event.packet is CPacketPlayer && passItemCheck(mc.player.activeItemStack.getItem()) && !mc.player.isRiding) {
            mc.player.connection.sendPacket(CPacketPlayerDigging(Action.ABORT_DESTROY_BLOCK, MathUtils.mcPlayerPosFloored(mc), EnumFacing.DOWN))
        }
    })

    override fun onUpdate() {
        if (slime.value) Blocks.SLIME_BLOCK.slipperiness = 0.4945f // normal block speed 0.4945
        else Blocks.SLIME_BLOCK.slipperiness = 0.8f
    }

    override fun onDisable() {
        Blocks.SLIME_BLOCK.slipperiness = 0.8f
    }

    private fun passItemCheck(item: Item): Boolean {
        return if (!mc.player.isHandActive) false else allItems.value
                || food.value && item is ItemFood
                || bow.value && item is ItemBow
                || potion.value && item is ItemPotion
                || shield.value && item is ItemShield
    }
}