package me.zeroeightsix.kami.module.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Mouse;

import java.util.Objects;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;

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
public class BlockData extends Module {
    private int delay = 0;

    @Override
    public void onUpdate() {
        if (delay > 0) {
            delay--;
        }
    }

    @EventHandler
    public Listener<InputEvent.MouseInputEvent> mouseListener = new Listener<>(event -> {
        if (Mouse.getEventButton() == 1 && delay == 0) {
            if (mc.objectMouseOver.typeOfHit.equals(RayTraceResult.Type.BLOCK)) {
                BlockPos blockpos = mc.objectMouseOver.getBlockPos();
                IBlockState iblockstate = mc.world.getBlockState(blockpos);
                Block block = iblockstate.getBlock();

                if (block.hasTileEntity()) {
                    TileEntity t = mc.world.getTileEntity(blockpos);
                    NBTTagCompound tag = new NBTTagCompound();
                    Objects.requireNonNull(t).writeToNBT(tag);

                    sendChatMessage(getChatName() + "&6Block Tags:\n" + tag + "");
                }
            }
        }
    });
}
