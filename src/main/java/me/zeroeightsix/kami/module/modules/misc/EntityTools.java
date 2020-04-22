package me.zeroeightsix.kami.module.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Mouse;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;

/**
 * @author 0x2E | PretendingToCode
 *
 * Inspired by ForgeHax, recreated with expressed permission from creator
 *
 * TODO: Fix delay timer because that shit broken
 */
@Module.Info(
        name = "EntityTools",
        category = Module.Category.MISC,
        description = "Right click entities to perform actions on them"
)
public class EntityTools extends Module {
    private Setting<Mode> mode = register(Settings.e("Mode", Mode.DELETE));
    private int delay = 0;
    private enum Mode {
        DELETE, INFO
    }

    @Override
    public void onUpdate() {
        if (delay > 0) {
            delay--;
        }
    }

    @EventHandler
    public Listener<InputEvent.MouseInputEvent> mouseListener = new Listener<>(event -> {
        if (Mouse.getEventButton() == 1 && delay == 0) {
            if (mc.objectMouseOver.typeOfHit.equals(RayTraceResult.Type.ENTITY)) {
                if (mode.getValue().equals(Mode.DELETE)) {
                    mc.world.removeEntity(mc.objectMouseOver.entityHit);
                }

                if (mode.getValue().equals(Mode.INFO)) {
                    NBTTagCompound tag = new NBTTagCompound();
                    mc.objectMouseOver.entityHit.writeToNBT(tag);
                    sendChatMessage(getChatName() + "&6Entity Tags:\n" + tag + "");
                }
            }
        }
    });
}
