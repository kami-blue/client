package org.kamiblue.client.module.modules.player

import org.kamiblue.client.mixin.client.network.MixinNetworkManager
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.text.MessageSendHelper.sendWarningMessage

/**
 * @see MixinNetworkManager
 */
internal object NoPacketKick : Module(
    name = "NoPacketKick",
    category = Category.PLAYER,
    description = "Suppress network exceptions and prevent getting kicked",
    showOnArray = false
) {
    @JvmStatic
    fun sendWarning(throwable: Throwable) {
        sendWarningMessage("$chatName Caught exception - \"$throwable\" check log for more info.")
        throwable.printStackTrace()
    }
}
