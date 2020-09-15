package me.zeroeightsix.kami.module.modules.combat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.gui.mc.KamiGuiDisconnected
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.Friends
import me.zeroeightsix.kami.util.InventoryUtils
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.monster.EntityCreeper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDamageEvent

/**
 * Created by 086 on 9/04/2018.
 * Updated by dominikaaaa on 06/09/20
 */
@Module.Info(
        name = "AutoLog",
        description = "Automatically log when in danger or on low health",
        category = Module.Category.COMBAT,
        alwaysListening = true
)
class AutoLog : Module() {
    private val health = register(Settings.integerBuilder("Health").withRange(0, 36).withValue(10).build())
    private val crystals = register(Settings.b("Crystals", false))
    private val creeper = register(Settings.b("Creepers", true))
    private val creeperDistance = register(Settings.integerBuilder("CreeperDistance").withRange(1, 10).withValue(5).withVisibility { creeper.value }.build())
    private val totem = register(Settings.b("Totems", false))
    private val totemAmount = register(Settings.integerBuilder("MinTotems").withRange(1, 10).withValue(2).withVisibility { totem.value }.build())
    private val players = register(Settings.b("Players", false))
    private val playerDistance = register(Settings.integerBuilder("PlayerDistance").withRange(64, 256).withValue(128).withVisibility { players.value }.build())
    private val friends = register(Settings.booleanBuilder("Friends").withValue(false).withVisibility { players.value }.build())

    private var lastDamageSource = "unknown"

    @EventHandler
    private val entityJoinWorldEventListener = Listener(EventHook { event: EntityJoinWorldEvent ->
        if (mc.player == null || !crystals.value || isDisabled) return@EventHook
        if (event.entity is EntityEnderCrystal) {
            if (mc.player.health - CrystalAura.calculateDamage(event.entity as EntityEnderCrystal, mc.player) < health.value) {
                log(listOf("An end crystal was placed too close to you!", "It would have done more then ${health.value} damage!"))
            }
        }
    })

    @EventHandler
    private val livingDamageEvent = Listener(EventHook { event: LivingDamageEvent ->
        if (mc.player == null) return@EventHook
        if (event.entity == mc.player) {
            event.source.trueSource?.let {
                lastDamageSource = it.name
            }
        }
    })

    override fun onEnable() {
        if (mc.player == null) disable()
    }

    override fun onUpdate() {
        if (mc.player == null || isDisabled) return

        if (mc.player.health < health.value) {
            log(listOf("Health went below ${health.value}!", "Last attacked by: $lastDamageSource"))
            return
        }

        if (totem.value && totemAmount.value > InventoryUtils.countItemAll(449)) {
            log(listOf("Less then ${totemMessage(totemAmount.value)}!"))
            return
        }

        if (creeper.value) {
            for (entity in mc.world.loadedEntityList) {
                if (entity is EntityCreeper && entity.getDistance(mc.player) < creeperDistance.value) {
                    log(listOf("Creeper came near you!"))
                    break
                }
            }
        }

        if (players.value) {
            for (entity in mc.world.loadedEntityList) {
                if (entity is EntityPlayer
                        && entity != mc.player
                        && entity.getDistance(mc.player) < playerDistance.value
                        && (friends.value || !Friends.isFriend(entity.name))) {
                    log(listOf("Player ${entity.name} came within ${playerDistance.value} blocks range!"))
                    break
                }
            }
        }

    }

    private fun log(reason: List<String>) {
        mc.getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
        mc.connection?.networkManager?.closeChannel(TextComponentString(""))
        mc.loadWorld(null)
        mc.displayGuiScreen(KamiGuiDisconnected(reason))

        Thread {
            Thread.sleep(250)
            disable()
        }.start()
    }

    private fun totemMessage(amount: Int): String {
        return if (amount == 1) {
            "one totem"
        } else {
            "$amount totems"
        }
    }
}