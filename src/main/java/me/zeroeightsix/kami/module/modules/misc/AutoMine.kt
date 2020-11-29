package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.mixin.extension.sendClickBlockToController
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper

@Module.Info(
    name = "AutoMine",
    description = "Automatically mines chosen ores",
    category = Module.Category.MISC
)
object AutoMine : Module() {

    private val manual = register(Settings.b("Manual", false))
    private val iron = register(Settings.booleanBuilder("Iron").withValue(false).withVisibility { !manual.value })
    private val diamond = register(Settings.booleanBuilder("Diamond").withValue(false).withVisibility { !manual.value })
    private val gold = register(Settings.booleanBuilder("Gold").withValue(false).withVisibility { !manual.value })
    private val coal = register(Settings.booleanBuilder("Coal").withValue(false).withVisibility { !manual.value })
    private val log = register(Settings.booleanBuilder("Log").withValue(false).withVisibility { !manual.value })

    override fun onEnable() {
        if (mc.player == null) {
            disable()
        } else {
            run()
        }
    }

    private fun run() {
        if (mc.player == null || isDisabled || manual.value) return
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
            BaritoneUtils.cancelEverything()
            return
        }

        MessageSendHelper.sendBaritoneCommand("mine", *total.toTypedArray())
    }

    override fun onDisable() {
        BaritoneUtils.cancelEverything()
    }

    init {
        with(Setting.SettingListeners { run() }) {
            iron.settingListener = this
            diamond.settingListener = this
            gold.settingListener = this
            coal.settingListener = this
            log.settingListener = this
        }

        listener<SafeTickEvent> {
            if (manual.value) {
                mc.sendClickBlockToController(true)
            }
        }

        listener<ConnectionEvent.Disconnect> {
            disable()
        }
    }
}
