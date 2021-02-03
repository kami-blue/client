package org.kamiblue.client.module.modules.combat

import org.kamiblue.client.KamiMod
import org.kamiblue.client.event.events.ConnectionEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.manager.managers.FriendManager
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.color.EnumTextColor
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.MessageSendHelper.sendServerMessage
import org.kamiblue.client.util.threads.safeListener
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.SPacketEntityStatus
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import java.util.*
import kotlin.collections.ArrayList

internal object TotemPopCounter : Module(
    name = "TotemPopCounter",
    description = "Counts how many times players pop",
    category = Category.COMBAT
) {
    private val countFriends = setting("Count Friends", true)
    private val countSelf = setting("Count Self", false)
    private val resetOnDeath = setting("Reset On Death", true)
    private val announceSetting = setting("Announce", Announce.CLIENT)
    private val thanksTo = setting("Thanks To", false)
    private val colorName = setting("Color Name", EnumTextColor.DARK_PURPLE)
    private val colorNumber = setting("Color Number", EnumTextColor.LIGHT_PURPLE)

    private enum class Announce {
        CLIENT, EVERYONE
    }

    private val playerList = Collections.synchronizedMap(HashMap<EntityPlayer, Int>())
    private var wasDead = false

    init {
        onDisable {
            playerList.clear()
        }

        safeListener<PacketEvent.Receive> {
            if (it.packet !is SPacketEntityStatus || it.packet.opCode.toInt() != 35 || player.isDead) return@safeListener
            val player = (it.packet.getEntity(world) as? EntityPlayer) ?: return@safeListener

            if (friendCheck(player) || selfCheck(player)) {
                val count = playerList.getOrDefault(player, 0) + 1
                playerList[player] = count
                sendMessage("${formatName(player)} popped ${formatNumber(count)} ${plural(count)}${ending()}")
            }
        }

        listener<ConnectionEvent.Disconnect> {
            playerList.clear()
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@safeListener

            if (wasDead && !player.isDead && resetOnDeath.value) {
                sendMessage("${formatName(player)} died and ${grammar(player)} pop list was reset!")
                playerList.clear()
                wasDead = false
                return@safeListener
            }

            val toRemove = ArrayList<EntityPlayer>()
            for ((poppedPlayer, count) in playerList) {
                if (!poppedPlayer.isDead) continue
                if (poppedPlayer == player) continue
                sendMessage("${formatName(poppedPlayer)} died after popping ${formatNumber(count)} ${plural(count)}${ending()}")
                toRemove.add(poppedPlayer)
            }
            playerList.keys.removeAll(toRemove)

            wasDead = player.isDead
        }
    }

    private fun friendCheck(player: EntityPlayer) = FriendManager.isFriend(player.name) && countFriends.value

    private fun selfCheck(player: EntityPlayer) = player == mc.player && countSelf.value

    private fun formatName(player: EntityPlayer): String {
        val name = when {
            player == mc.player -> "I"
            FriendManager.isFriend(player.name) -> if (isPublic) "My friend, " else "Your friend, "
            else -> player.name
        }
        return colorName.value.textFormatting.toString() + name + TextFormatting.RESET
    }

    private val isPublic: Boolean
        get() = announceSetting.value == Announce.EVERYONE

    private fun plural(count: Int) = if (count == 1) "totem" else "totems"

    private fun grammar(player: EntityPlayer) = if (player == mc.player) "my" else "their"

    private fun ending(): String = if (thanksTo.value) " thanks to ${KamiMod.NAME} !" else "!"

    private fun formatNumber(message: Int) = colorNumber.value.textFormatting.toString() + message + TextFormatting.RESET

    private fun sendMessage(message: String) {
        when (announceSetting.value) {
            Announce.CLIENT -> {
                MessageSendHelper.sendChatMessage("$chatName $message")
            }
            Announce.EVERYONE -> {
                sendServerMessage(TextFormatting.getTextWithoutFormattingCodes(message))
            }
        }
    }
}