package me.zeroeightsix.kami.module.modules.combat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.client.InfoOverlay.getItems
import me.zeroeightsix.kami.module.modules.misc.AutoReconnect
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.monster.EntityCreeper
import net.minecraft.init.Items
import net.minecraft.init.SoundEvents
import net.minecraftforge.event.entity.EntityJoinWorldEvent

/**
 * Created by 086 on 9/04/2018.
 */
@Module.Info(
        name = "AutoLog",
        description = "Automatically log when in danger or on low health",
        category = Module.Category.COMBAT
)
class AutoLog : Module() {
    private val autoDisable = register(Settings.b("AutoDisable", true))
    private val timeout = register(Settings.integerBuilder("Timeout").withRange(1, 100).withValue(30).withVisibility { !autoDisable.value }.build())
    private val health = register(Settings.integerBuilder("Health").withRange(0, 36).withValue(6).build())
    private val crystals = register(Settings.b("Crystals", true))
    private val creeper = register(Settings.b("Creepers", true))
    private val distance = register(Settings.integerBuilder("CreeperDistance").withRange(1, 10).withValue(5).withVisibility { creeper.value }.build())
    private val totem = register(Settings.b("Totems", false))
    private val totemAmount = register(Settings.integerBuilder("MinTotems").withRange(1, 10).withValue(2).withVisibility { totem.value }.build())

    private var shouldLog = false
    private var lastLog = System.currentTimeMillis()

    @EventHandler
    private val entityJoinWorldEventListener = Listener(EventHook { event: EntityJoinWorldEvent ->
        if (mc.player == null || !crystals.value) return@EventHook
        if (event.entity is EntityEnderCrystal) {
            if (mc.player.health - CrystalAura.calculateDamage(event.entity as EntityEnderCrystal, mc.player) < health.value) {
                mc.addScheduledTask(this::log)
            }
        }
    })

    override fun onUpdate() {
        if (mc.player == null) return

        if (shouldLog) {
            if ((!autoDisable.value && System.currentTimeMillis() - lastLog < timeout.value * 1000) || isDisabled) {
                return
            }
            shouldLog = false
            lastLog = System.currentTimeMillis()

            mc.getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
            mc.world.sendQuittingDisconnectingPacket()

            if (autoDisable.value) disable()
        }

        if (mc.player.health < health.value) {
            mc.addScheduledTask(this::log)
        }

        if (creeper.value) {
            for (entity in mc.world.loadedEntityList) {
                if (entity is EntityCreeper && entity.getDistance(mc.player) < distance.value) {
                    mc.addScheduledTask(this::log)
                }
            }
        }

        if (totem.value && getItems(Items.TOTEM_OF_UNDYING) <= totemAmount.value) {
            mc.addScheduledTask(this::log)
        }
    }

    private fun log() {
        KamiMod.MODULE_MANAGER.getModule(AutoReconnect::class.java).disable()
        shouldLog = true
    }
}