package me.zeroeightsix.kami.module.modules.misc

import baritone.api.BaritoneAPI
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraftforge.fml.common.network.FMLNetworkEvent

/**
 * @author dominikaaaa
 */
@Module.Info(
        name = "AutoMine",
        description = "Automatically mines chosen ores",
        category = Module.Category.MISC
)
class AutoMine : Module() {
    private var iron = register(Settings.b("Iron", true))
    private var diamond = register(Settings.b("Diamond", true))
    private var gold = register(Settings.b("Gold", false))
    private var coal = register(Settings.b("Coal", false))
    private var log = register(Settings.b("Logs", false))

    init {
        iron.settingListener = Setting.SettingListeners { if (mc.player != null && isEnabled) run() }
        diamond.settingListener = Setting.SettingListeners { if (mc.player != null && isEnabled) run() }
        gold.settingListener = Setting.SettingListeners { if (mc.player != null && isEnabled) run() }
        coal.settingListener = Setting.SettingListeners { if (mc.player != null && isEnabled) run() }
        log.settingListener = Setting.SettingListeners { if (mc.player != null && isEnabled) run() }
    }

    override fun onEnable() {
        if (mc.player == null) {
            disable()
            return
        }
        run()
    }

    private fun run() {
        var current = ""
        if (iron.value) current += " iron_ore"
        if (diamond.value) current += " diamond_ore"
        if (gold.value) current += " gold_ore"
        if (coal.value) current += " coal_ore"
        if (log.value) current += " log"

        if (current.startsWith(" ")) {
            current = current.substring(1)
        }
        val total = current.split(" ")

        if (current.length < 2) {
            MessageSendHelper.sendBaritoneMessage("Error: you have to choose at least one thing to mine. To mine custom blocks run the &7" + Command.getCommandPrefix() + "b mine block&f command")
            BaritoneAPI.getProvider().primaryBaritone.pathingBehavior.cancelEverything()
            return
        }

        MessageSendHelper.sendBaritoneCommand("mine", *total.toTypedArray())
    }

    override fun onDisable() {
        mc.player?.let {
            BaritoneAPI.getProvider().primaryBaritone.pathingBehavior.cancelEverything()
        }
    }

    @EventHandler
    private val clientDisconnect = Listener(EventHook { event: FMLNetworkEvent.ClientDisconnectionFromServerEvent ->
        BaritoneAPI.getProvider().primaryBaritone.pathingBehavior.cancelEverything()
    })

    @EventHandler
    private val serverDisconnect = Listener(EventHook { event: FMLNetworkEvent.ServerDisconnectionFromClientEvent ->
        BaritoneAPI.getProvider().primaryBaritone.pathingBehavior.cancelEverything()
    })
}