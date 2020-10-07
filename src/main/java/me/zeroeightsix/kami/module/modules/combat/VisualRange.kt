package me.zeroeightsix.kami.module.modules.combat

import com.mojang.realmsclient.gui.ChatFormatting
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.mangers.WaypointManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.Friends
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import java.util.*

@Module.Info(
        name = "VisualRange",
        description = "Shows players who enter and leave range in chat",
        category = Module.Category.COMBAT
)
object VisualRange : Module() {
    private val playSound = register(Settings.b("PlaySound", false))
    private val leaving = register(Settings.b("CountLeaving", false))
    private val friends = register(Settings.b("Friends", true))
    private val uwuAura = register(Settings.b("UwUAura", false))
    private val logToFile = register(Settings.b("LogTo File", false))

    private var knownPlayers: MutableList<String>? = null

    override fun onUpdate(event: SafeTickEvent) {
        val tickPlayerList: MutableList<String> = ArrayList()

        for (entity in mc.world.getLoadedEntityList()) {
            if (entity is EntityPlayer) tickPlayerList.add(entity.getName())
        }

        if (tickPlayerList.size > 0) {
            for (playerName in tickPlayerList) {
                if ((playerName == mc.player.name) || (!friends.value && Friends.isFriend(playerName))) continue

                if (!knownPlayers!!.contains(playerName)) {
                    knownPlayers!!.add(playerName)
                    if (Friends.isFriend(playerName)) {
                        sendNotification(ChatFormatting.GREEN.toString() + playerName + ChatFormatting.RESET.toString() + " joined!")
                    } else {
                        sendNotification(ChatFormatting.RED.toString() + playerName + ChatFormatting.RESET.toString() + " joined!")
                    }
                    if (logToFile.value) {
                        WaypointManager.add("$playerName spotted!")
                    }
                    if (uwuAura.value) MessageSendHelper.sendServerMessage("/w $playerName hi uwu")
                    return
                }
            }
        }

        if (knownPlayers!!.size > 0) {
            for (playerName in knownPlayers!!) {
                if (!tickPlayerList.contains(playerName)) {
                    knownPlayers!!.remove(playerName)
                    if (leaving.value) {
                        if (Friends.isFriend(playerName)) {
                            sendNotification(ChatFormatting.GREEN.toString() + playerName + ChatFormatting.RESET.toString() + " left!")
                        } else {
                            sendNotification(ChatFormatting.RED.toString() + playerName + ChatFormatting.RESET.toString() + " left!")
                        }
                        if (uwuAura.value) MessageSendHelper.sendServerMessage("/w $playerName bye uwu")
                    }
                    return
                }
            }
        }
    }

    private fun sendNotification(s: String) {
        if (playSound.value) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
        }
        MessageSendHelper.sendChatMessage(s)
    }

    override fun onEnable() {
        knownPlayers = ArrayList()
    }
}