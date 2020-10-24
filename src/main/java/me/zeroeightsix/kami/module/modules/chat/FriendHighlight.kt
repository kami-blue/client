package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.color.ColorTextFormatting
import me.zeroeightsix.kami.util.color.ColorTextFormatting.ColourCode
import me.zeroeightsix.kami.util.event.listener
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
    private val colour = register(Settings.e<ColourCode>("Color", ColourCode.GRAY))

    private val regex1 = "<(.*?)>".toRegex()
    private val regex2 = "[<>]".toRegex()

    override fun onEnable() {
        noFriendsCheck()
    }

    init {
        listener<ClientChatReceivedEvent>(0) {
            if (noFriendsCheck() || !FriendManager.enabled) return@listener

            val playerName = regex1.find(it.message.unformattedText)?.value?.replace(regex2, "")
            if (playerName == null || !FriendManager.isFriend(playerName)) return@listener
            val modified = it.message.formattedText.replaceFirst(playerName, getReplacement(playerName))
            val textComponent = TextComponentString(modified)

            it.message = textComponent
        }
    }

    private fun noFriendsCheck() = FriendManager.empty.also {
        if (it) {
            MessageSendHelper.sendErrorMessage("$chatName You don't have any friends added, silly! Go add some friends before using the module")
            disable()
        }
    }

    private fun getReplacement(name: String) = color() + bold() + name + TextFormatting.RESET.toString()

    private fun bold() = if (!bold.value) "" else TextFormatting.BOLD.toString()

    private fun color() = ColorTextFormatting.toTextMap[colour.value].toString()
}
