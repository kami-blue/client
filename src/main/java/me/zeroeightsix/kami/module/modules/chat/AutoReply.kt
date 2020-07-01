package me.zeroeightsix.kami.module.modules.chat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.misc.AntiAFK
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MessageDetectionHelper
import me.zeroeightsix.kami.util.MessageSendHelper
import net.minecraft.network.play.server.SPacketChat

/**
 * @author dominikaaaa
 * Updated by dominikaaaa on 07/05/20
 */
@Module.Info(
        name = "AutoReply",
        description = "Automatically reply to direct messages",
        category = Module.Category.CHAT
)
class AutoReply : Module() {
    @JvmField
    var customMessage: Setting<Boolean> = register(Settings.b("CustomMessage", false))
    @JvmField
    var message: Setting<String> = register(Settings.stringBuilder("CustomText").withValue("Use &7" + Command.getCommandPrefix() + "autoreply&r to modify this").withConsumer { _: String?, _: String? -> }.withVisibility { customMessage.value }.build())

    @EventHandler
    private val receiveListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (KamiMod.MODULE_MANAGER.isModuleEnabled(AntiAFK::class.java) && KamiMod.MODULE_MANAGER.getModuleT(AntiAFK::class.java).autoReply.value) return@EventHook

        if (event.packet is SPacketChat && MessageDetectionHelper.isDirect(true, (event.packet as SPacketChat).getChatComponent().unformattedText)) {
            if (customMessage.value) {
                MessageSendHelper.sendServerMessage("/r " + message.value)
            } else {
                MessageSendHelper.sendServerMessage("/r I just automatically replied, thanks to KAMI Blue's AutoReply module!")
            }
        }
    })
}