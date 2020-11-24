package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.mixin.extension.rightClickMouse
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting.SettingListeners
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BlockUtils.isWater
import me.zeroeightsix.kami.util.TimerUtils.TickTimer
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.init.Items
import net.minecraft.network.play.server.SPacketSoundEffect
import java.lang.Math.random
import kotlin.math.abs

/**
 * Created by 086 on 22/03/2018.
 * Updated by Qther on 05/03/20
 * Updated by l1ving on 26/05/20
 * Updated by Xiaro on 22/08/20
 */
@Module.Info(
        name = "AutoFish",
        category = Module.Category.MISC,
        description = "Automatically catch fish"
)
object AutoFish : Module() {
    private val mode = register(Settings.e<Mode>("Mode", Mode.BOUNCE))
    private val defaultSetting = register(Settings.b("Defaults", false))
    private val autoCast = register(Settings.b("AutoCast", true))
    private val castDelay = register(Settings.integerBuilder("AutoCastDelay(s)").withValue(5).withRange(1, 20).withVisibility { autoCast.value })
    private val catchDelay = register(Settings.integerBuilder("CatchDelay(ms)").withValue(300).withRange(50, 2000))
    private val recastDelay = register(Settings.integerBuilder("RecastDelay(ms)").withValue(450).withRange(50, 2000))
    private val variation = register(Settings.integerBuilder("Variation(ms)").withValue(100).withRange(0, 1000))

    @Suppress("UNUSED")
    private enum class Mode {
        BOUNCE, SPLASH, ANY_SPLASH, ALL
    }

    private var catching = false
    private var recasting = false
    private val timer = TickTimer()

    init {
        listener<PacketEvent.Receive> {
            if (mc.player == null || mc.player.fishEntity == null || !isStabled()) return@listener
            if (mode.value == Mode.BOUNCE || it.packet !is SPacketSoundEffect) return@listener
            if (isSplash(it.packet)) catch()
        }

        listener<SafeTickEvent> {
            if (mc.player.heldItemMainhand.item != Items.FISHING_ROD) { // If not holding a fishing rod then don't do anything
                reset()
                return@listener
            }

            if (mc.player.fishEntity == null) {
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
    }

    override fun onToggle() {
        reset()
    }

    private fun isStabled(): Boolean {
        if (mc.player.fishEntity == null || mc.player.fishEntity!!.isAirBorne || recasting) return false
        return abs(mc.player.fishEntity!!.motionX) + abs(mc.player.fishEntity!!.motionZ) < 0.01
    }

    private fun isOnWater(): Boolean {
        if (mc.player.fishEntity == null || mc.player.fishEntity!!.isAirBorne) return false
        val pos = mc.player.fishEntity!!.position
        return isWater(pos) || isWater(pos.down())
    }

    private fun isSplash(packet: SPacketSoundEffect): Boolean {
        if (mode.value == Mode.SPLASH && mc.player.fishEntity!!.getDistance(packet.x, packet.y, packet.z) > 2) return false
        val soundName = packet.sound.soundName.toString().toLowerCase()
        return (mode.value != Mode.SPLASH && isAnySplash(soundName)) || soundName.contains("entity.bobber.splash")
    }

    private fun isAnySplash(soundName: String): Boolean {
        return soundName.contains("entity.generic.splash")
                || soundName.contains("entity.generic.splash")
                || soundName.contains("entity.hostile.splash")
                || soundName.contains("entity.player.splash")
    }

    private fun isBouncing(): Boolean {
        if (mc.player.fishEntity == null || !isOnWater()) return false
        return mc.player.fishEntity!!.motionY !in -0.05..0.05
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

    private fun defaults() {
        autoCast.value = true
        castDelay.value = 5
        catchDelay.value = 300
        recastDelay.value = 450
        variation.value = 100
        defaultSetting.value = false
        MessageSendHelper.sendChatMessage("$chatName Set to defaults!")
    }

    init {
        defaultSetting.settingListener = SettingListeners { if (defaultSetting.value) defaults() }
    }
}