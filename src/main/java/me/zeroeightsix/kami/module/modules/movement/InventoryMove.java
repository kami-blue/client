package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.gui.GuiChat;
import org.lwjgl.input.Keyboard;

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 06/04/20
 * @see me.zeroeightsix.kami.mixin.client.MixinMovementInputFromOptions
 */
@Module.Info(
        name = "InventoryMove",
        description = "Allows you to walk around with GUIs opened",
        category = Module.Category.MOVEMENT
)
public class InventoryMove extends Module {
    private Setting<Integer> speed = register(Settings.i("Look speed", 10));
    public Setting<Boolean> sneak = register(Settings.b("Sneak", false));

    public void onUpdate() {
        if (mc.player == null || mc.currentScreen == null || mc.currentScreen instanceof GuiChat) return;
        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
            mc.player.rotationYaw = mc.player.rotationYaw - speed.getValue();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
            mc.player.rotationYaw = mc.player.rotationYaw + speed.getValue();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
            mc.player.rotationPitch = mc.player.rotationPitch - speed.getValue();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
            mc.player.rotationPitch = mc.player.rotationPitch + speed.getValue();
        }
    }
}

