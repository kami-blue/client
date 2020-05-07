package me.zeroeightsix.kami.module.modules.combat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.misc.AutoReconnect
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.client.Minecraft
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.network.play.server.SPacketDisconnect
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDamageEvent

/**
 * Created by 086 on 9/04/2018.
 */
@Module.Info(
        name = "AutoLog",
        description = "Automatically log when in danger or on low health",
        category = Module.Category.COMBAT
)
class AutoLog : Module() {
    private val health = register(Settings.integerBuilder("Health").withRange(0, 36).withValue(6).build())

    private var shouldLog = false
    private var lastLog = System.currentTimeMillis()

    @EventHandler
    private val livingDamageEventListener = Listener(EventHook { event: LivingDamageEvent ->
        if (mc.player == null) return@EventHook
        if (event.entity === mc.player) {
            if (mc.player.health - event.amount < health.value) {
                log()
            }
        }
    })

    @EventHandler
    private val entityJoinWorldEventListener = Listener(EventHook { event: EntityJoinWorldEvent ->
        if (mc.player == null) return@EventHook
        if (event.entity is EntityEnderCrystal) {
            if (mc.player.health - CrystalAura.calculateDamage(event.entity as EntityEnderCrystal, mc.player) < health.value) {
                log()
            }
        }
    })

    override fun onUpdate() {
        if (shouldLog) {
            shouldLog = false
            if (System.currentTimeMillis() - lastLog < 2000) return
            Minecraft.getMinecraft().connection!!.handleDisconnect(SPacketDisconnect(TextComponentString("AutoLogged")))
        }
    }

    private fun log() {
        KamiMod.MODULE_MANAGER.getModule(AutoReconnect::class.java).disable()
        shouldLog = true
        lastLog = System.currentTimeMillis()
    }
}