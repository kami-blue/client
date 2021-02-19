package org.kamiblue.client.module.modules.misc

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Mouse

internal object BlockData : Module(
    name = "BlockData",
    category = Category.MISC,
    description = "Right click blocks to display their data"
) {
    private val timer = TickTimer()
    private var lastPos = BlockPos.ORIGIN

    init {
        listener<InputEvent.MouseInputEvent> {
            if (Mouse.getEventButton() != 1 || mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != RayTraceResult.Type.BLOCK) return@listener
            if (timer.tick(5000L) || mc.objectMouseOver.blockPos != lastPos && timer.tick(500L)) {
                val blockPos = mc.objectMouseOver.blockPos
                val blockState = mc.world.getBlockState(blockPos)
                lastPos = blockPos

                if (blockState.block.hasTileEntity(blockState)) {
                    val tileEntity = mc.world.getTileEntity(blockPos) ?: return@listener
                    val tag = NBTTagCompound().apply { tileEntity.writeToNBT(this) }
                    MessageSendHelper.sendChatMessage("""$chatName &6Block Tags:$tag""".trimIndent())
                }
            }
        }
    }
}