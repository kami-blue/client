package me.zeroeightsix.kami.module.modules.chat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.manager.mangers.FriendManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.color.ColorTextFormatting
import me.zeroeightsix.kami.util.color.ColorTextFormatting.ColourCode
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.client.event.ClientChatReceivedEvent

@Module.Info(
        name = "FriendHighlight",
        description = "Highlights your friends names in chat",
        category = Module.Category.CHAT,
        showOnArray = Module.ShowOnArray.OFF
)
object FriendHighlight : Module() {
    private val bold = register(Settings.b("Bold", true))
    private val colour = register(Settings.e<ColourCode>("Colour", ColourCode.GRAY))

    override fun onEnable() {
        if (FriendManager.friendFile.friends.size > 100) {
            MessageSendHelper.sendErrorMessage("$chatName Your friends list is bigger then 100, disabling as it would cause too much of a performance impact.")
            disable()
        }
        noFriendsCheck()
    }

    @EventHandler
    private val listener = Listener(EventHook { event: ClientChatReceivedEvent ->
        if (mc.player == null || noFriendsCheck() || !FriendManager.friendFile.enabled) return@EventHook
        var converted = event.message.formattedText
        for (friend in FriendManager.friendFile.friends) {
            converted = converted.replace(friend.username.toRegex(RegexOption.IGNORE_CASE), getReplacement(friend.username) + TextFormatting.RESET.toString())
        }
        val message = TextComponentString(converted)
        event.message = message
    })

    private fun noFriendsCheck(): Boolean {
        if (FriendManager.friendFile.friends.size == 0) {
            MessageSendHelper.sendErrorMessage("$chatName You don't have any friends added, silly! Go add some friends before using the module")
            disable()
            return true
        }
        return false
    }

    private fun getReplacement(name: String): String {
        return colour() + bold() + name
    }

    private fun bold(): String {
        return if (!bold.value) "" else TextFormatting.BOLD.toString()
    }

    private fun colour(): String {
        return ColorTextFormatting.toTextMap[colour.value].toString()
    }
}
