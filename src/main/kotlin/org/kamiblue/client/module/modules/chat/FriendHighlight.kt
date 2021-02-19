package org.kamiblue.client.module.modules.chat

import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.init.SoundEvents
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.client.event.ClientChatReceivedEvent
import org.kamiblue.client.manager.managers.FriendManager
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.color.EnumTextColor
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.event.listener.listener

internal object FriendHighlight : Module(
    name = "FriendHighlight",
    description = "Highlights your friends names in chat",
    category = Category.CHAT,
    showOnArray = false
) {
    private val bold = setting("Bold", true)
    private val color = setting("Color", EnumTextColor.GRAY)
    private val sound = setting("Sound", true)

    private val regex1 = "<(.*?)>".toRegex()
    private val regex2 = "[<>]".toRegex()

    init {
        onEnable {
            noFriendsCheck()
        }

        listener<ClientChatReceivedEvent>(0) {
            if (noFriendsCheck() || !FriendManager.enabled) return@listener

            val playerName = regex1.find(it.message.unformattedText)?.value?.replace(regex2, "")
            if (playerName == null || !FriendManager.isFriend(playerName)) return@listener
            val modified = it.message.formattedText.replaceFirst(playerName, getReplacement(playerName))
            val textComponent = TextComponentString(modified)
            if (sound.value) mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))

            it.message = textComponent
        }
    }

    private fun noFriendsCheck() = FriendManager.empty.also {
        if (it) {
            MessageSendHelper.sendErrorMessage("$chatName You don't have any friends added, silly! Go add some friends before using the module")
            disable()
        }
    }

    private fun getReplacement(name: String) = "${color.value.textFormatting}${bold()}$name${TextFormatting.RESET}"

    private fun bold() = if (!bold.value) "" else TextFormatting.BOLD.toString()
}
