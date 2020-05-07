package me.zeroeightsix.kami.module.modules.combat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemPickaxe
import net.minecraft.network.play.server.SPacketBlockBreakAnim
import net.minecraft.util.math.BlockPos
import kotlin.math.pow

/**
 * @author Antonio32A
 * Updated by dominikaaaa on 31/03/20
 *
 * Antonio32A created the pastDistance method, used by ForgeHax here:
 * https://github.com/fr1kin/ForgeHax/blob/2011740/src/main/java/com/matt/forgehax/mods/CoordsFinder.java#L126
 */
@Module.Info(
        name = "BreakingWarning",
        category = Module.Category.COMBAT,
        description = "Notifies you when someone is breaking a block near you."
)
class BreakingWarning : Module() {
    private val minRange = register(Settings.doubleBuilder("Min Range").withMinimum(0.0).withValue(1.5).withMaximum(10.0).build())
    private val obsidianOnly = register(Settings.b("Obsidian Only", true))
    private val pickaxeOnly = register(Settings.b("Pickaxe Only", true))

    private var warn = false
    private var playerName: String? = null
    private var delay = 0

    @EventHandler
    private val receiveListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (event.packet is SPacketBlockBreakAnim) {
            val packet = event.packet as SPacketBlockBreakAnim

            val progress = packet.progress
            val breakerId = packet.breakerId
            val pos = packet.position
            val block = mc.world.getBlockState(pos).block
            val breaker = mc.world.getEntityByID(breakerId) as EntityPlayer? ?: return@EventHook

            if (obsidianOnly.value && block != Blocks.OBSIDIAN) return@EventHook

            if (pickaxeOnly.value) {
                if (breaker.itemStackMainHand.isEmpty() || breaker.itemStackMainHand.getItem() !is ItemPickaxe) return@EventHook
            }

            if (pastDistance(mc.player, pos, minRange.value)) {
                playerName = breaker.name
                warn = true
                delay = 0
                if (progress == 255) warn = false
            }
        }
    })

    override fun onRender() {
        if (!warn) return
        if (delay++ > 100) warn = false

        val text = "$playerName is breaking blocks near you!"
        val renderer = Wrapper.getFontRenderer()
        val divider = DisplayGuiScreen.getScale()

        renderer.drawStringWithShadow(mc.displayWidth / divider / 2 - renderer.getStringWidth(text) / 2, mc.displayHeight / divider / 2 - 16, 240, 87, 70, text)
    }

    private fun pastDistance(player: EntityPlayer, pos: BlockPos, dist: Double): Boolean {
        return player.getDistanceSqToCenter(pos) <= dist.pow(2.0)
    }
}