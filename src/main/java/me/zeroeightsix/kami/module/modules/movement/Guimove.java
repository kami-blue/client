package me.zeroeightsix.kami.module.modules.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.client.event.InputUpdateEvent;
import org.lwjgl.input.Keyboard;

@Module.Info(name = "Guimove", category = Module.Category.MOVEMENT, description = "Move while GUI is open")
public class Guimove extends Module {
    @EventHandler
    private Listener<InputUpdateEvent> inputUpdateEventListener = new Listener<>(event -> {
        if(mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat) && mc.world != null && mc.player != null) {
            if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())) {
                event.getMovementInput().moveForward = 1;
            }
            if (Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode())) {
                event.getMovementInput().moveForward = -1;
            }
            if (Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode())) {
                event.getMovementInput().moveStrafe = 1;
            }
            if (Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode())) {
                event.getMovementInput().moveStrafe = -1;
            }
            if (Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
                if (mc.player.isInLava() || mc.player.isInWater()) {
                    final EntityPlayerSP player = mc.player;
                    player.motionY += 0.039;
                }
                else if (mc.player.onGround) {
                    mc.player.jump();
                }
            }
            if (Keyboard.isKeyDown(200)) {
                mc.player.rotationPitch -= 5;

            }
            if (Keyboard.isKeyDown(208)) {
                mc.player.rotationPitch += 5;
            }
            if (Keyboard.isKeyDown(205)) {
                mc.player.rotationYaw += 5;
            }
            if (Keyboard.isKeyDown(203)) {
                mc.player.rotationYaw -= 5;
            }
            if (mc.player.rotationPitch > 90) mc.player.rotationPitch = 90;
            if (mc.player.rotationPitch < -90) mc.player.rotationPitch = -90;

        }
    });
}
