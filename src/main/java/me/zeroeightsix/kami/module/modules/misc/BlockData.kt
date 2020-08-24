package me.zeroeightsix.kami.module.modules.misc

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.MessageSendHelper
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.RayTraceResult
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Mouse
import java.util.*

/**
 * @author 0x2E | PretendingToCode
 *
 * TODO: Fix delay timer because that shit broken
 */
@Module.Info(
        name = "BlockData",
        category = Module.Category.MISC,
        description = "Right click blocks to display their data"
)
class BlockData : Module() {
    private var delay = 0

    override fun onUpdate() {
        if (delay > 0) {
            delay--
        }
    }

    @EventHandler
    private val mouseListener = Listener(EventHook { event: InputEvent.MouseInputEvent? ->
        if (Mouse.getEventButton() == 1 && delay == 0 && mc.objectMouseOver != null) {
            if (mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
                val blockPos = mc.objectMouseOver.blockPos
                val iBlockState = mc.world.getBlockState(blockPos)
                val block = iBlockState.block

                if (block.hasTileEntity()) {
                    val t = mc.world.getTileEntity(blockPos)
                    val tag = NBTTagCompound()

                    Objects.requireNonNull(t)!!.writeToNBT(tag)
                    MessageSendHelper.sendChatMessage("""$chatName &6Block Tags:$tag""".trimIndent())
                }
            }
        }
    })
}