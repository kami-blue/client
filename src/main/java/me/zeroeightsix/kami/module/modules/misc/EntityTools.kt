package me.zeroeightsix.kami.module.modules.misc

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.RayTraceResult
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Mouse

/**
 * TODO: Fix delay timer because that shit broken
 */
@Module.Info(
        name = "EntityTools",
        category = Module.Category.MISC,
        description = "Right click entities to perform actions on them"
)
object EntityTools : Module() {
    private val mode = register(Settings.e<Mode>("Mode", Mode.INFO))

    private enum class Mode {
        DELETE, INFO
    }

    private var delay = 0

    override fun onUpdate() {
        if (delay > 0) {
            delay--
        }
    }

    @EventHandler
    private val mouseListener = Listener(EventHook { event: InputEvent.MouseInputEvent? ->
        if (Mouse.getEventButton() == 1 && delay == 0 && mc.objectMouseOver != null) {
            if (mc.objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY) {
                if (mode.value == Mode.DELETE) {
                    mc.world.removeEntity(mc.objectMouseOver.entityHit)
                }
                if (mode.value == Mode.INFO) {
                    val tag = NBTTagCompound()
                    mc.objectMouseOver.entityHit.writeToNBT(tag)
                    MessageSendHelper.sendChatMessage("""$chatName &6Entity Tags:$tag""".trimIndent())
                }
            }
        }
    })
}
