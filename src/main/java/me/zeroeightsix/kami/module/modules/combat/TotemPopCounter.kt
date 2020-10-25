package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.color.ColorTextFormatting
import me.zeroeightsix.kami.util.color.ColorTextFormatting.ColourCode
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.SPacketEntityStatus
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*
import kotlin.collections.ArrayList

@Module.Info(
        name = "TotemPopCounter",
        description = "Counts how many times players pop",
        category = Module.Category.COMBAT
)
object TotemPopCounter : Module() {
    private val countFriends = register(Settings.b("CountFriends", true))
    private val countSelf = register(Settings.b("CountSelf", false))
    private val resetOnDeath = register(Settings.b("ResetOnDeath", true))
    private val announceSetting = register(Settings.e<Announce>("Announce", Announce.CLIENT))
    private val thanksTo = register(Settings.b("ThanksTo", false))
    private val colorName = register(Settings.e<ColourCode>("ColorName", ColourCode.DARK_PURPLE))
    private val colorNumber = register(Settings.e<ColourCode>("ColorNumber", ColourCode.LIGHT_PURPLE))

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

        listener<SafeTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@listener

            if (wasDead && !mc.player.isDead && resetOnDeath.value) {
                sendMessage("${formatName(mc.player)} died and ${grammar(mc.player)} pop list was reset!")
                playerList.clear()
                wasDead = false
                return@listener
            }

            val toRemove = ArrayList<EntityPlayer>()
            for ((player, count) in playerList) {
                if (!player.isDead) continue
                if (player == mc.player) continue
                sendMessage("${formatName(player)} died after popping ${formatNumber(count)} ${plural(count)}${ending()}")
                toRemove.add(player)
            }
            playerList.keys.removeAll(toRemove)

            wasDead = mc.player.isDead
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
        return setToText(colorName.value) + name + TextFormatting.RESET
    }

    private val isPublic: Boolean
        get() = announceSetting.value == Announce.EVERYONE

    private fun plural(count: Int) = if (count == 1) "totem" else "totems"

    private fun grammar(player: EntityPlayer) = if (player == mc.player) "my" else "their"

    private fun ending(): String = if (thanksTo.value) " thanks to ${KamiMod.MODNAME} !" else "!"

    private fun formatNumber(message: Int) = setToText(colorNumber.value) + message + TextFormatting.RESET

    private fun sendMessage(message: String) {
        when (announceSetting.value) {
            Announce.CLIENT -> {
                MessageSendHelper.sendChatMessage("$chatName $message")
            }
            Announce.EVERYONE -> {
                MessageSendHelper.sendServerMessage(TextFormatting.getTextWithoutFormattingCodes(message))
            }
            else -> {
            }
        }
    }

    private fun setToText(colourCode: ColourCode) = ColorTextFormatting.toTextMap[colourCode]!!.toString()
}