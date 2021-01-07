package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.color.EnumTextColor
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendServerMessage
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.SPacketEntityStatus
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import java.util.*
import kotlin.collections.ArrayList

object TotemPopCounter : Module(
    name = "TotemPopCounter",
    description = "Counts how many times players pop",
    category = Category.COMBAT
) {
    private val countFriends = setting("CountFriends", true)
    private val countSelf = setting("CountSelf", false)
    private val resetOnDeath = setting("ResetOnDeath", true)
    private val announceSetting = setting("Announce", Announce.CLIENT)
    private val thanksTo = setting("ThanksTo", false)
    private val colorName = setting("ColorName", EnumTextColor.DARK_PURPLE)
    private val colorNumber = setting("ColorNumber", EnumTextColor.LIGHT_PURPLE)

    private enum class Announce {
        CLIENT, EVERYONE
    }

    private val playerList = HashMap<EntityPlayer, Int>()
    private var wasDead = false

    init {
        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketEntityStatus || it.packet.opCode.toInt() != 35 || mc.player == null || mc.player.isDead) return@listener
            val player = (it.packet.getEntity(mc.world) as? EntityPlayer) ?: return@listener

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

    override fun onDisable() {
        playerList.clear()
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