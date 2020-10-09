package me.zeroeightsix.kami.module.modules.misc

import baritone.api.BaritoneAPI
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper

@Module.Info(
        name = "AutoMine",
        description = "Automatically mines chosen ores",
        category = Module.Category.MISC
)
object AutoMine : Module() {
    private val iron = register(Settings.b("Iron", true))
    private val diamond = register(Settings.b("Diamond", true))
    private val gold = register(Settings.b("Gold", false))
    private val coal = register(Settings.b("Coal", false))
    private val log = register(Settings.b("Logs", false))

    override fun onEnable() {
        if (mc.player == null) {
            disable()
            return
        }
        run()
    }

    private fun run() {
        if (mc.player == null || isDisabled) return
        var current = ""
        if (iron.value) current += " iron_ore"
        if (diamond.value) current += " diamond_ore"
        if (gold.value) current += " gold_ore"
        if (coal.value) current += " coal_ore"
        if (log.value) current += " log log2"

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

    init {
        with(Setting.SettingListeners { run() }) {
            iron.settingListener = this
            diamond.settingListener = this
            gold.settingListener = this
            coal.settingListener = this
            log.settingListener = this
        }

        listener<ConnectionEvent.Disconnect> {
            disable()
        }
    }
}
