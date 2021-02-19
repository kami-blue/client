package org.kamiblue.client.module.modules.misc

import net.minecraft.entity.Entity
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.RayTraceResult
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Mouse

internal object EntityTools : Module(
    name = "EntityTools",
    category = Category.MISC,
    description = "Right click entities to perform actions on them"
) {
    private val mode = setting("Mode", Mode.INFO)

    private enum class Mode {
        DELETE, INFO
    }

    private val timer = TickTimer()
    private var lastEntity: Entity? = null

    init {
        listener<InputEvent.MouseInputEvent> {
            if (Mouse.getEventButton() != 1 || mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != RayTraceResult.Type.ENTITY) return@listener
            if (timer.tick(5000L) || mc.objectMouseOver.entityHit != lastEntity && timer.tick(500L)) {
                when (mode.value) {
                    Mode.DELETE -> {
                        mc.world.removeEntity(mc.objectMouseOver.entityHit)
                    }
                    Mode.INFO -> {
                        val tag = NBTTagCompound().apply { mc.objectMouseOver.entityHit.writeToNBT(this) }
                        MessageSendHelper.sendChatMessage("""$chatName &6Entity Tags:$tag""".trimIndent())
                    }
                }
            }
        }
    }
}
