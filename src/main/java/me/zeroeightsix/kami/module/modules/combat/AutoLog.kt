package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.gui.mc.KamiGuiDisconnected
import me.zeroeightsix.kami.manager.managers.CombatManager
import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.combat.AutoLog.Reasons.*
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.combat.CombatUtils
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.entity.monster.EntityCreeper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.utils.MathUtils
import java.time.LocalTime


@Module.Info(
        name = "AutoLog",
        description = "Automatically log when in danger or on low health",
        category = Module.Category.COMBAT,
        alwaysListening = true
)
object AutoLog : Module() {
    private val disable: Setting<DisableMode> = register(Settings.e("Disable", DisableMode.ALWAYS))
    private val health = register(Settings.integerBuilder("Health").withValue(10).withRange(6, 36).withStep(1))
    private val crystals = register(Settings.b("Crystals", false))
    private val creeper = register(Settings.b("Creepers", true))
    private val creeperDistance = register(Settings.integerBuilder("CreeperDistance").withValue(5).withRange(1, 10).withVisibility { creeper.value })
    private val totem = register(Settings.b("Totems", false))
    private val totemAmount = register(Settings.integerBuilder("MinTotems").withValue(2).withRange(1, 10).withVisibility { totem.value })
    private val players = register(Settings.b("Players", false))
    private val playerDistance = register(Settings.integerBuilder("PlayerDistance").withValue(128).withRange(64, 256).withVisibility { players.value })
    private val friends = register(Settings.booleanBuilder("Friends").withValue(false).withVisibility { players.value })

    @Suppress("UNUSED")
    private enum class DisableMode {
        NEVER, ALWAYS, NOT_PLAYER
    }


    init {
        listener<SafeTickEvent>(-1000) {
            if (isDisabled || it.phase != TickEvent.Phase.END) return@listener

            when {
                mc.player.health < health.value -> log(HEALTH)
                totem.value && totemAmount.value > InventoryUtils.countItemAll(449) -> log(TOTEM)
                crystals.value && checkCrystals() -> log(END_CRYSTAL)
                creeper.value && checkCreeper() -> { /* checkCreeper() does log() */ }
                players.value && checkPlayers() -> { /* checkPlayer() does log() */ }
            }
        }
    }

    private fun checkCrystals(): Boolean {
        val maxSelfDamage = CombatManager.crystalMap.values.maxBy { it.second }?.second ?: 0.0f
        return CombatUtils.getHealthSmart(mc.player) - maxSelfDamage < health.value
    }

    private fun checkCreeper(): Boolean {
        for (entity in mc.world.loadedEntityList) {
            if (entity !is EntityCreeper) continue
            if (mc.player.getDistance(entity) > creeperDistance.value) continue
            log(CREEPER, MathUtils.round(entity.getDistance(mc.player), 2).toString())
            return true
        }
        return false
    }

    private fun checkPlayers(): Boolean {
        for (entity in mc.world.loadedEntityList) {
            if (entity !is EntityPlayer) continue
            if (AntiBot.botSet.contains(entity)) continue
            if (entity == mc.player) continue
            if (mc.player.getDistance(entity) > playerDistance.value) continue
            if (!friends.value && FriendManager.isFriend(entity.name)) continue
            log(PLAYER, entity.name)
            return true
        }
        return false
    }

    private fun log(reason: Reasons, additionalInfo: String = "") {
        val reasonText = getReason(reason, additionalInfo)
        val screen = getScreen() // do this before disconnecting

        mc.getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
        mc.connection?.networkManager?.closeChannel(TextComponentString(""))
        mc.loadWorld(null as WorldClient?)

        mc.displayGuiScreen(KamiGuiDisconnected(reasonText, screen, disable.value == DisableMode.ALWAYS || (disable.value == DisableMode.NOT_PLAYER && reason != PLAYER), LocalTime.now()))
    }

    private fun getScreen() = if (mc.isIntegratedServerRunning) {
        GuiMainMenu()
    } else {
        GuiMultiplayer(GuiMainMenu())
    }

    private fun getReason(reason: Reasons, additionalInfo: String) = when (reason) {
        HEALTH -> arrayOf("Health went below ${health.value}!")
        TOTEM -> arrayOf("Less then ${totemMessage(totemAmount.value)}!")
        CREEPER -> arrayOf("Creeper came near you!", "It was $additionalInfo blocks away")
        PLAYER -> arrayOf("Player $additionalInfo came within ${playerDistance.value} blocks range!")
        END_CRYSTAL -> arrayOf("An end crystal was placed too close to you!", "It would have done more then ${health.value} damage!")
    }

    private enum class Reasons {
        HEALTH, TOTEM, CREEPER, PLAYER, END_CRYSTAL
    }

    private fun totemMessage(amount: Int) = if (amount == 1) "one totem" else "$amount totems"
}