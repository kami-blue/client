package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.event.events.BaritoneCommandEvent
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.mixin.extension.sendClickBlockToController
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.formatValue
import org.kamiblue.event.listener.listener

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

    override fun onDisable() {
        BaritoneUtils.cancelEverything()
    }

    private fun run() {
        if (mc.player == null || isDisabled || manual.value) return

        val blocks = ArrayList<String>()

        if (iron.value) blocks.add("iron_ore")
        if (diamond.value) blocks.add("diamond_ore")
        if (gold.value) blocks.add("gold_ore")
        if (coal.value) blocks.add("coal_ore")
        if (log.value) {
            blocks.add("log")
            blocks.add("log2")
        }

        if (blocks.isEmpty()) {
            MessageSendHelper.sendBaritoneMessage("Error: you have to choose at least one thing to mine. " +
                "To mine custom blocks run the ${formatValue("${CommandManager.prefix}b mine block")} command")
            BaritoneUtils.cancelEverything()
            return
        }

        MessageSendHelper.sendBaritoneCommand("mine", *blocks.toTypedArray())
    }

    init {
        listener<SafeTickEvent> {
            if (manual.value) {
                mc.sendClickBlockToController(true)
            }
        }

        listener<ConnectionEvent.Disconnect> {
            disable()
        }

        listener<BaritoneCommandEvent> {
            if (it.command.contains("cancel")) {
                disable()
            }
        }

        with(Setting.SettingListeners { run() }) {
            iron.settingListener = this
            diamond.settingListener = this
            gold.settingListener = this
            coal.settingListener = this
            log.settingListener = this
        }
    }
}
