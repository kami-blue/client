package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings

/**
 * @author dominikaaaa
 * Updated by dominikaaaa on 27/12/19
 */
@Module.Info(
        name = "CleanGUI",
        category = Module.Category.RENDER,
        showOnArray = Module.ShowOnArray.OFF,
        description = "Modifies parts of the GUI to be transparent"
)
class CleanGUI : Module() {
    @JvmField
    var inventoryGlobal: Setting<Boolean> = register(Settings.b("Inventory", true))

    companion object {
        @JvmField
        var chatGlobal: Setting<Boolean> = Settings.b("Chat", false)
        private var INSTANCE = CleanGUI()

        @JvmStatic
        fun enabled(): Boolean {
            return INSTANCE.isEnabled
        }
    }

    init {
        INSTANCE = this
        register(chatGlobal)
    }
}