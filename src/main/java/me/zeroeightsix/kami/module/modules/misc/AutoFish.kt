package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.mixin.extension.rightClickMouse
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.WorldUtils.isWater
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.init.Items
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.lang.Math.random
import kotlin.math.abs

/**
 * Created by 086 on 22/03/2018.
 * Updated by Qther on 05/03/20
 * Updated by l1ving on 26/05/20
 * Updated by Xiaro on 22/08/20
 */
internal object AutoFish : Module(
    name = "AutoFish",
    category = Category.MISC,
    description = "Automatically catch fish"
) {
    private val mode = setting("Mode", Mode.BOUNCE)
    private val autoCast = setting("AutoCast", true)
    private val castDelay = setting("AutoCastDelay(s)", 5, 1..20, 1, { autoCast.value })
    private val catchDelay = setting("CatchDelay(ms)", 300, 50..2000, 50)
    private val recastDelay = setting("RecastDelay(ms)", 450, 50..2000, 50)
    private val variation = setting("Variation(ms)", 100, 0..1000, 50)

    @Suppress("UNUSED")
    private enum class Mode {
        BOUNCE, SPLASH, ANY_SPLASH, ALL
    }

    private var catching = false
    private var recasting = false
    private val timer = TickTimer()

    init {
        safeListener<PacketEvent.Receive> {
            if (player.fishEntity == null || !isStabled()) return@safeListener
            if (mode.value == Mode.BOUNCE || it.packet !is SPacketSoundEffect) return@safeListener
            if (isSplash(it.packet)) catch()
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (player.heldItemMainhand.item != Items.FISHING_ROD) { // If not holding a fishing rod then don't do anything
                reset()
                return@safeListener
            }

            if (player.fishEntity == null) {
                if (recasting) { // Recast the fishing rod
                    if (timer.tick(recastDelay.value.toLong())) {
                        mc.rightClickMouse()
                        reset()
                    }
                } else if (autoCast.value && timer.tick(castDelay.value * 1000L)) { // Cast the fishing rod if a fishing rod is in hand and not fishing
                    mc.rightClickMouse()
                    reset()
                }
            } else if (isStabled() && isOnWater()) {
                if (catching) { // Catch the fish
                    if (timer.tick(catchDelay.value.toLong())) {
                        mc.rightClickMouse()
                        recast()
                    }
                } else {// Bounce detection
                    if ((mode.value == Mode.BOUNCE || mode.value == Mode.ALL) && isBouncing()) {
                        catch()
                    }
                }
            } else if (isStabled()) {// If the fishing rod is not in air and not in water (ex. hooked a block), then we recast it with extra delay
                mc.rightClickMouse()
                reset()
            }
        }

        onToggle {
            reset()
        }
    }

    private fun SafeClientEvent.isStabled(): Boolean {
        if (player.fishEntity?.isAirBorne != false || recasting) return false
        return abs(player.fishEntity!!.motionX) + abs(player.fishEntity!!.motionZ) < 0.01
    }

    private fun SafeClientEvent.isOnWater(): Boolean {
        if (player.fishEntity?.isAirBorne != false) return false
        val pos = player.fishEntity!!.position
        return isWater(pos) || isWater(pos.down())
    }

    private fun SafeClientEvent.isSplash(packet: SPacketSoundEffect): Boolean {
        if (mode.value == Mode.SPLASH && (player.fishEntity?.getDistance(packet.x, packet.y, packet.z)
                ?: 69420.0) > 2) return false
        val soundName = packet.sound.soundName.toString().toLowerCase()
        return (mode.value != Mode.SPLASH && isAnySplash(soundName)) || soundName.contains("entity.bobber.splash")
    }

    private fun isAnySplash(soundName: String): Boolean {
        return soundName.contains("entity.generic.splash")
            || soundName.contains("entity.hostile.splash")
            || soundName.contains("entity.player.splash")
    }

    private fun SafeClientEvent.isBouncing(): Boolean {
        if (player.fishEntity == null || !isOnWater()) return false
        return (player.fishEntity?.motionY ?: 911.0) !in -0.05..0.05
    }

    private fun catch() {
        if (catching) return
        resetTimer()
        catching = true
        recasting = false
    }

    private fun recast(extraDelay: Long = 0L) {
        if (recasting) return
        resetTimer()
        timer.reset(extraDelay)
        catching = false
        recasting = true
    }

    private fun reset() {
        resetTimer()
        catching = false
        recasting = false
    }

    private fun resetTimer() {
        val offset = if (variation.value > 0) (random() * (variation.value * 2) - variation.value).toLong() else 0
        timer.reset(offset)
    }
}