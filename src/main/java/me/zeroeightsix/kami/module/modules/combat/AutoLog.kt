package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.gui.mc.KamiGuiDisconnected
import me.zeroeightsix.kami.manager.managers.CombatManager
import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.combat.AutoLog.Reasons.*
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.combat.CombatUtils
import me.zeroeightsix.kami.util.threads.safeListener
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


object AutoLog : Module(
    name = "AutoLog",
    description = "Automatically log when in danger or on low health",
    category = Category.COMBAT,
    alwaysListening = true
) {
    private val disable = setting("Disable", DisableMode.ALWAYS)
    private val health = setting("Health", 10, 6..36, 1)
    private val crystals = setting("Crystals", false)
    private val creeper = setting("Creepers", true)
    private val creeperDistance = setting("CreeperDistance", 5, 1..10, 1, { creeper.value })
    private val totem = setting("Totems", false)
    private val totemAmount = setting("MinTotems", 2, 1..10, 1, { totem.value })
    private val players = setting("Players", false)
    private val playerDistance = setting("PlayerDistance", 128, 64..256, 16, { players.value })
    private val friends = setting("Friends", false, { players.value })

    @Suppress("UNUSED")
    private enum class DisableMode {
        NEVER, ALWAYS, NOT_PLAYER
    }

    init {
        safeListener<TickEvent.ClientTickEvent>(-1000) {
            if (isDisabled || it.phase != TickEvent.Phase.END) return@safeListener

            when {
                player.health < health.value -> log(HEALTH)
                totem.value && totemAmount.value > InventoryUtils.countItemAll(449) -> log(TOTEM)
                crystals.value && checkCrystals() -> log(END_CRYSTAL)
                creeper.value && checkCreeper() -> { /* checkCreeper() does log() */ }
                players.value && checkPlayers() -> { /* checkPlayer() does log() */ }
            }
        }
    }

    private fun SafeClientEvent.checkCrystals(): Boolean {
        val maxSelfDamage = CombatManager.crystalMap.values.maxByOrNull { it.second }?.second ?: 0.0f
        return CombatUtils.getHealthSmart(player) - maxSelfDamage < health.value
    }

    private fun SafeClientEvent.checkCreeper(): Boolean {
        for (entity in world.loadedEntityList) {
            if (entity !is EntityCreeper) continue
            if (player.getDistance(entity) > creeperDistance.value) continue
            log(CREEPER, MathUtils.round(entity.getDistance(player), 2).toString())
            return true
        }
        return false
    }

    private fun SafeClientEvent.checkPlayers(): Boolean {
        for (entity in world.loadedEntityList) {
            if (entity !is EntityPlayer) continue
            if (AntiBot.botSet.contains(entity)) continue
            if (entity == player) continue
            if (player.getDistance(entity) > playerDistance.value) continue
            if (!friends.value && FriendManager.isFriend(entity.name)) continue
            log(PLAYER, entity.name)
            return true
        }
        return false
    }

    private fun SafeClientEvent.log(reason: Reasons, additionalInfo: String = "") {
        val reasonText = getReason(reason, additionalInfo)
        val screen = getScreen() // do this before disconnecting

        mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
        connection.networkManager.closeChannel(TextComponentString(""))
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