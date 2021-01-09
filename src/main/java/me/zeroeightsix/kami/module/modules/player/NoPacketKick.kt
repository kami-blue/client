package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.mixin.client.network.MixinNetworkManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendWarningMessage

/**
 * @see MixinNetworkManager
 */
object NoPacketKick : Module(
    category = Category.PLAYER,
    showOnArray = false
) {
    @JvmStatic
    fun sendWarning(throwable: Throwable) {
        sendWarningMessage("$chatName Caught exception - \"$throwable\" check log for more info.")
        throwable.printStackTrace()
    }
}
