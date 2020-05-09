package me.zeroeightsix.kami.module.modules.chat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.ColourTextFormatting
import me.zeroeightsix.kami.util.ColourTextFormatting.ColourCode
import me.zeroeightsix.kami.util.Friends
import me.zeroeightsix.kami.util.Friends.Friend
import me.zeroeightsix.kami.util.MessageSendHelper
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.client.event.ClientChatReceivedEvent
import java.util.function.Consumer

@Module.Info(
        name = "FriendHighlight",
        description = "Highlights your friends names in chat",
        category = Module.Category.CHAT,
        showOnArray = Module.ShowOnArray.OFF
)
class FriendHighlight : Module() {
    private val bold = register(Settings.b("Bold", true))
    private val colour = register(Settings.e<ColourCode>("Colour", ColourCode.GRAY))

    public override fun onEnable() {
        if (Friends.friends.value.size > 100) {
            MessageSendHelper.sendErrorMessage(chatName + "Your friends list is bigger then 100, disabling as it would cause too much of a performance impact.")
            disable()
        }
        noFriendsCheck()
    }

    @EventHandler
    var listener = Listener(EventHook { event: ClientChatReceivedEvent ->
        if (mc.player == null || noFriendsCheck()) return@EventHook
        val converted = arrayOf("")
        Friends.friends.value.forEach(Consumer { friend: Friend -> converted[0] = event.message.formattedText.replace("(?i)" + friend.username.toRegex(), colour() + bold() + friend.username + TextFormatting.RESET.toString()) })
        val message = TextComponentString(converted[0])
        event.message = message
    })

    private fun noFriendsCheck(): Boolean {
        if (Friends.friends.value.size == 0) {
            MessageSendHelper.sendErrorMessage(chatName + "You don't have any friends added, silly! Go add some friends before using the module")
            disable()
            return true
        }
        return false
    }

    private fun bold(): String {
        return if (!bold.value) "" else TextFormatting.BOLD.toString()
    }

    private fun colour(): String {
        return ColourTextFormatting.toTextMap[colour.value].toString()
    }
}
