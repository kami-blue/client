package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.TickTimer
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import org.lwjgl.opengl.Display

internal object CommandConfig : Module(
    name = "CommandConfig",
    category = Category.CLIENT,
    description = "Configures client chat related stuff",
    showOnArray = false,
    alwaysEnabled = true
) {
    val prefix = setting("Prefix", ";", { false })
    val toggleMessages = setting("ToggleMessages", false)
    private val customTitle = setting("WindowTitle", true)
    val modifierEnabled = setting("ModifierEnabled", false, { false })

    private val timer = TickTimer()
    private val prevTitle = Display.getTitle()
    private const val title = "${KamiMod.NAME} ${KamiMod.KAMI_KATAKANA} ${KamiMod.VERSION_SIMPLE}"

    init {
        listener<TickEvent.ClientTickEvent> {
            if (timer.tick(10000L)) {
                if (customTitle.value) Display.setTitle(title)
                else Display.setTitle(prevTitle)
            }
        }

        customTitle.listeners.add {
            timer.reset(-0xCAFEBABE)
        }
    }
}