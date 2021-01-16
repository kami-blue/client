package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.manager.managers.WaypointManager
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.settings.impl.primitive.StringSetting
import me.zeroeightsix.kami.util.EntityUtils.flooredPosition
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendServerMessage
import me.zeroeightsix.kami.util.text.format
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*

internal object VisualRange : Module(
    name = "VisualRange",
    description = "Shows players who enter and leave range in chat",
    category = Category.COMBAT,
    alwaysListening = true
) {
    private val playSound = setting("PlaySound", false)
    private val leaving = setting("CountLeaving", false)
    private val friends = setting("Friends", true)
    private val uwuAura = setting("UwUAura", false)
    private val logToFile = setting("LogToFile", false)
    private val enterMessage = setting("EnterMessage", "%s spotted!")
    private val leaveMessage = setting("LeaveMessage", "%s left!", { leaving.value })

    private val playerSet = LinkedHashSet<EntityPlayer>()
    private val timer = TickTimer(TimeUnit.SECONDS)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END || !timer.tick(1L)) return@safeListener

            val loadedPlayerSet = LinkedHashSet(world.playerEntities)
            for (entityPlayer in loadedPlayerSet) {
                if (entityPlayer == mc.renderViewEntity || entityPlayer == player) continue // Self/Freecam check
                if (entityPlayer.entityId < 0) continue // Fake entity check
                if (!friends.value && FriendManager.isFriend(entityPlayer.name)) continue // Friend check

                if (playerSet.add(entityPlayer) && isEnabled) {
                    onEnter(entityPlayer)
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

    private fun StringSetting.replacedName(player: EntityPlayer) = this.value.replace("%s", getColor(player).format(player.name))

    private fun onEnter(player: EntityPlayer) {
        sendNotification(enterMessage.replacedName(player))
        if (logToFile.value) WaypointManager.add(player.flooredPosition, enterMessage.replacedName(player))
        if (uwuAura.value) sendServerMessage("/w ${player.name} hi uwu")
    }

    private fun onLeave(player: EntityPlayer) {
        if (leaving.value) {
            sendNotification(leaveMessage.replacedName(player))
            if (logToFile.value) WaypointManager.add(player.flooredPosition, leaveMessage.replacedName(player))
            if (uwuAura.value) sendServerMessage("/w ${player.name} bye uwu")
        }
    }

    private fun getColor(player: EntityPlayer) =
        if (FriendManager.isFriend(player.name)) TextFormatting.GREEN
        else TextFormatting.RED

    private fun sendNotification(message: String) {
        if (playSound.value) mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
        MessageSendHelper.sendChatMessage(message)
    }
}