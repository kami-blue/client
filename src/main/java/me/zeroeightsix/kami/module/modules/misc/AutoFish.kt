package me.zeroeightsix.kami.module.modules.misc

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.LocalPlayerUpdateEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting.SettingListeners
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MessageSendHelper
import net.minecraft.item.ItemFishingRod
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerDisconnectionFromClientEvent
import java.util.*

/**
 * Created by 086 on 22/03/2018.
 * Updated by Qther on 05/03/20
 * Updated by dominikaaaa on 26/05/20
 */
@Module.Info(
        name = "AutoFish",
        category = Module.Category.MISC,
        description = "Automatically catch fish",
        alwaysListening = true
)
class AutoFish : Module() {
    private val defaultSetting = register(Settings.b("Defaults", false))
    private val baseDelay = register(Settings.integerBuilder("ThrowDelay").withValue(450).withMinimum(50).withMaximum(1000).build())
    private val extraDelay = register(Settings.integerBuilder("CatchDelay").withValue(300).withMinimum(0).withMaximum(1000).build())
    private val variation = register(Settings.integerBuilder("Variation").withValue(50).withMinimum(0).withMaximum(1000).build())
    private val recastOnReconnect = register(Settings.booleanBuilder("CastOnReconnect").withValue(true).build())

    var random: Random? = null
    private var recast = false
    private var looking = arrayOf(0, 0) // pitch, yaw

    @EventHandler
    var localPlayerUpdateEventListener = Listener(EventHook { event: LocalPlayerUpdateEvent? ->
        if (mc.player != null && recast && recastOnReconnect.value && mc.player.heldItemMainhand.item is ItemFishingRod) {
            mc.player.rotationPitch = looking[0].toFloat()
            mc.player.rotationYaw = looking[1].toFloat()
            Thread(Runnable {
                try {
                    Thread.sleep(500)
                } catch (ignored: InterruptedException) { }

                mc.rightClickMouse()
                looking[0] = mc.player.rotationPitch.toInt()
                looking[1] = mc.player.rotationYaw.toInt()
            }).start()
            recast = false
        }
    })

    @EventHandler
    var clientDisconnect = Listener(EventHook { event: ClientDisconnectionFromServerEvent? ->
        if (isDisabled) return@EventHook
        recast = true
    })

    @EventHandler
    var serverDisconnect = Listener(EventHook { event: ServerDisconnectionFromClientEvent? ->
        if (isDisabled) return@EventHook
        recast = true
    })

    @EventHandler
    private val receiveListener = Listener(EventHook { e: PacketEvent.Receive ->
        if (isEnabled && e.packet is SPacketSoundEffect) {
            val pck = e.packet as SPacketSoundEffect
            if (pck.getSound().soundName.toString().toLowerCase().contains("entity.bobber.splash")) {
                if (mc.player.fishEntity == null) return@EventHook

                val soundX = pck.x.toInt()
                val soundZ = pck.z.toInt()
                val fishX = mc.player.fishEntity!!.posX.toInt()
                val fishZ = mc.player.fishEntity!!.posZ.toInt()

                if (kindaEquals(soundX, fishX) && kindaEquals(fishZ, soundZ)) {
                    Thread(Runnable {
                        random = Random()

                        try {
                            Thread.sleep(extraDelay.value + random!!.ints(1, -variation.value, variation.value).findFirst().asInt.toLong())
                        } catch (ignored: InterruptedException) { }

                        looking[0] = mc.player.rotationPitch.toInt()
                        looking[1] = mc.player.rotationYaw.toInt()
                        mc.rightClickMouse()
                        random = Random()

                        try {
                            Thread.sleep(baseDelay.value + random!!.ints(1, -variation.value, variation.value).findFirst().asInt.toLong())
                        } catch (e1: InterruptedException) {
                            e1.printStackTrace()
                        }

                        looking[0] = mc.player.rotationPitch.toInt()
                        looking[1] = mc.player.rotationYaw.toInt()
                        mc.rightClickMouse()
                    }).start()
                }
            }
        }
    })

    private fun kindaEquals(kara: Int, ni: Int): Boolean {
        return ni == kara || ni == kara - 1 || ni == kara + 1
    }

    private fun defaults() {
        baseDelay.value = 450
        extraDelay.value = 300
        variation.value = 50
        recastOnReconnect.value = true
        defaultSetting.value = false
        MessageSendHelper.sendChatMessage("$chatName Set to defaults!")
        closeSettings()
    }

    init {
        defaultSetting.settingListener = SettingListeners { if (defaultSetting.value) defaults() }
    }
}