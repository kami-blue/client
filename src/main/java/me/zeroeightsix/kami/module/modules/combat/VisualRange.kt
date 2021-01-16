package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.manager.managers.WaypointManager
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.EntityUtils.flooredPosition
import me.zeroeightsix.kami.util.EntityUtils.isFakeOrSelf
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
    private const val NAME_FORMAT = "\$NAME"

    private val playSound by setting("PlaySound", false)
    private val leaving by setting("CountLeaving", false)
    private val friends by setting("Friends", true)
    private val uwuAura by setting("UwUAura", false)
    private val logToFile by setting("LogToFile", false)
    private val enterMessage by setting("EnterMessage", "$NAME_FORMAT spotted!")
    private val leaveMessage by setting("LeaveMessage", "$NAME_FORMAT left!", { leaving })

    private val playerSet = LinkedHashSet<EntityPlayer>()
    private val timer = TickTimer(TimeUnit.SECONDS)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END || !timer.tick(1L)) return@safeListener

            val loadedPlayerSet = LinkedHashSet(world.playerEntities)
            for (entityPlayer in loadedPlayerSet) {
                if (entityPlayer.isFakeOrSelf) continue // Self / Freecam / FakePlayer check
                if (!friends && FriendManager.isFriend(entityPlayer.name)) continue // Friend check

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

    private fun onEnter(player: EntityPlayer) {
        val message = enterMessage.replaceName(player)

        sendNotification(message)
        if (logToFile) WaypointManager.add(player.flooredPosition, message)
        if (uwuAura) sendServerMessage("/w ${player.name} hi uwu")
    }

    private fun onLeave(player: EntityPlayer) {
        if (!leaving) return
        val message = leaveMessage.replaceName(player)

        sendNotification(message)
        if (logToFile) WaypointManager.add(player.flooredPosition, message)
        if (uwuAura) sendServerMessage("/w ${player.name} bye uwu")
    }

    private fun String.replaceName(player: EntityPlayer) = replace(NAME_FORMAT, getColor(player) format player.name)

    private fun getColor(player: EntityPlayer) =
        if (FriendManager.isFriend(player.name)) TextFormatting.GREEN
        else TextFormatting.RED

    private fun sendNotification(message: String) {
        if (playSound) mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
        MessageSendHelper.sendChatMessage(message)
    }
}