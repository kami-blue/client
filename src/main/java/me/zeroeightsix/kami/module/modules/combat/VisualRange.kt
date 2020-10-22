package me.zeroeightsix.kami.module.modules.combat

import com.mojang.realmsclient.gui.ChatFormatting
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.managers.WaypointManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.Friends
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraftforge.fml.common.gameevent.TickEvent

@Module.Info(
        name = "VisualRange",
        description = "Shows players who enter and leave range in chat",
        category = Module.Category.COMBAT,
        alwaysListening = true
)
object VisualRange : Module() {
    private val playSound = register(Settings.b("PlaySound", false))
    private val leaving = register(Settings.b("CountLeaving", false))
    private val friends = register(Settings.b("Friends", true))
    private val uwuAura = register(Settings.b("UwUAura", false))
    private val logToFile = register(Settings.b("LogTo File", false))

    private val playerSet = LinkedHashSet<EntityPlayer>()

    init {
        listener<SafeTickEvent> {
            if (it.phase != TickEvent.Phase.END || isDisabled && mc.player.ticksExisted % 5 != 0) return@listener

            val loadedPlayerSet = LinkedHashSet(mc.world.playerEntities)
            for (player in loadedPlayerSet) {
                if (player == mc.player || !friends.value && Friends.isFriend(player.name)) continue
                if (playerSet.add(player) && isEnabled) {
                    onEnter(player)
                }
            }

            val toRemove = ArrayList<EntityPlayer>()
            for (player in playerSet) {
                if (!loadedPlayerSet.contains(player)) {
                    toRemove.add(player)
                    if (isEnabled) onLeave(player)
                }
            }
            playerSet.removeAll(toRemove)
        }
    }

    private fun onEnter(player: EntityPlayer) {
        sendNotification("${getColor(player)}${player.name} ${ChatFormatting.RESET}joined!")
        if (logToFile.value) WaypointManager.add("${player.name} spotted!")
        if (uwuAura.value) MessageSendHelper.sendServerMessage("/w ${player.name} hi uwu")
    }

    private fun onLeave(player: EntityPlayer) {
        if (leaving.value) {
            sendNotification("${getColor(player)}${player.name} ${ChatFormatting.RESET}left!")
            if (logToFile.value) WaypointManager.add("${player.name} left!")
            if (uwuAura.value) MessageSendHelper.sendServerMessage("/w ${player.name} bye uwu")
        }
    }

    private fun getColor(player: EntityPlayer) = if (Friends.isFriend(player.name)) ChatFormatting.GREEN.toString() else ChatFormatting.RED.toString()

    private fun sendNotification(message: String) {
        if (playSound.value) mc.getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
        MessageSendHelper.sendChatMessage(message)
    }
}