package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Mouse

@Module.Info(
        name = "BlockData",
        category = Module.Category.MISC,
        description = "Right click blocks to display their data"
)
object BlockData : Module() {
    private val timer = TimerUtils.TickTimer()
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