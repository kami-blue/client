package me.zeroeightsix.kami.module.modules.combat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.EntityUseTotemEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.Friends
import me.zeroeightsix.kami.util.color.ColorTextFormatting
import me.zeroeightsix.kami.util.color.ColorTextFormatting.ColourCode
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.network.play.server.SPacketEntityStatus
import net.minecraft.util.text.TextFormatting
import java.util.*

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 25/03/20
 * Listener and stuff reused from CliNet
 * https://github.com/DarkiBoi/CliNet/blob/fd225a5c8cc373974b0c9a3457acbeed206e8cca/src/main/java/me/zeroeightsix/kami/module/modules/combat/TotemPopCounter.java
 */
@Module.Info(
        name = "TotemPopCounter",
        description = "Counts how many times players pop",
        category = Module.Category.COMBAT
)
class TotemPopCounter : Module() {
    private val countFriends = register(Settings.b("CountFriends", true))
    private val countSelf = register(Settings.b("CountSelf", false))
    private val resetDeaths = register(Settings.b("ResetOnDeath", true))
    private val resetSelfDeaths = register(Settings.b("ResetSelfDeath", true))
    private val announceSetting = register(Settings.e<Announce>("Announce", Announce.CLIENT))
    private val thanksTo = register(Settings.b("ThanksTo", false))
    private val colourCode = register(Settings.e<ColourCode>("ColorName", ColourCode.DARK_PURPLE))
    private val colourCode1 = register(Settings.e<ColourCode>("ColorNumber", ColourCode.LIGHT_PURPLE))

    private enum class Announce {
        CLIENT, EVERYONE
    }

    private var playerList: HashMap<String?, Int?>? = HashMap<String?, Int?>()
    private var isDead = false

    override fun onUpdate() {
        if (!isDead
                && resetSelfDeaths.value
                && 0 >= mc.player.health) {
            sendMessage(formatName(mc.player.name) + " died and " + grammar(mc.player.name) + " pop list was reset!")
            isDead = true
            playerList!!.clear()
            return
        }
        if (isDead && 0 < mc.player.health) isDead = false

        for (player in mc.world.playerEntities) {
            if (resetDeaths.value
                    && 0 >= player.health && friendCheck(player.name)
                    && selfCheck(player.name)
                    && playerList!!.containsKey(player.name)) {
                /* To note: if they died after popping 1 totem it's going to say totems, but I cba to fix it */
                sendMessage(formatName(player.name) + " died after popping " + formatNumber(playerList!![player.name]!!) + " totems" + ending())
                playerList!!.remove(player.name, playerList!![player.name])
            }
        }
    }

    @EventHandler
    private val listListener = Listener(EventHook { event: EntityUseTotemEvent ->
        if (playerList == null) playerList = HashMap()
        if (playerList!![event.entity.name] == null) {
            playerList!![event.entity.name] = 1
            sendMessage(formatName(event.entity.name) + " popped " + formatNumber(1) + " totem" + ending())
        } else if (playerList!![event.entity.name] != null) {
            var popCounter = playerList!![event.entity.name]!!
            popCounter += 1
            playerList!![event.entity.name] = popCounter
            sendMessage(formatName(event.entity.name) + " popped " + formatNumber(popCounter) + " totems" + ending())
        }
    })

    private fun friendCheck(name: String): Boolean {
        if (isDead) return false
        for (names in Friends.friends.value) {
            if (names.username.equals(name, ignoreCase = true)) return countFriends.value
        }
        return true
    }

    private fun selfCheck(name: String): Boolean {
        if (isDead) return false
        if (countSelf.value && name.equals(mc.player.name, ignoreCase = true)) {
            return true
        } else if (!countSelf.value && name.equals(mc.player.name, ignoreCase = true)) {
            return false
        }
        return true
    }

    private fun isSelf(name: String): Boolean {
        return name.equals(mc.player.name, ignoreCase = true)
    }

    private fun isFriend(name: String): Boolean {
        for (names in Friends.friends.value) {
            if (names.username.equals(name, ignoreCase = true)) return true
        }
        return false
    }

    private fun formatName(username: String): String {
        var name = username // this is a var because you change it in isSelf
        var extraText = ""
        if (isFriend(name) && !isPublic) extraText = "Your friend, " else if (isFriend(name) && isPublic) extraText = "My friend, "
        if (isSelf(name)) {
            extraText = ""
            name = "I"
        }
        return if (announceSetting.value == Announce.EVERYONE) {
            extraText + name
        } else extraText + setToText(colourCode.value) + name + TextFormatting.RESET
    }

    private fun grammar(name: String): String {
        return if (isSelf(name)) {
            "my"
        } else "their"
    }

    private fun ending(): String {
        return if (thanksTo.value) {
            " thanks to " + KamiMod.MODNAME + "!"
        } else "!"
    }

    private val isPublic: Boolean
        get() = announceSetting.value == Announce.EVERYONE

    private fun formatNumber(message: Int): String {
        return if (announceSetting.value == Announce.EVERYONE) "" + message else setToText(colourCode1.value).toString() + "" + message + TextFormatting.RESET
    }

    private fun sendMessage(message: String) {
        when (announceSetting.value) {
            Announce.CLIENT -> {
                MessageSendHelper.sendRawChatMessage(message)
                return
            }
            Announce.EVERYONE -> {
                MessageSendHelper.sendServerMessage(message)
                return
            }
            else -> {
            }
        }
    }

    @EventHandler
    private val popListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (mc.player == null) return@EventHook
        if (event.packet is SPacketEntityStatus) {
            val packet = event.packet as SPacketEntityStatus

            if (packet.opCode.toInt() == 35) {
                val entity = packet.getEntity(mc.world)

                if (friendCheck(entity.name) || selfCheck(entity.name)) {
                    KamiMod.EVENT_BUS.post(EntityUseTotemEvent(entity))
                }
            }
        }
    })

    private fun setToText(colourCode: ColourCode): TextFormatting? {
        return ColorTextFormatting.toTextMap[colourCode]
    }
}