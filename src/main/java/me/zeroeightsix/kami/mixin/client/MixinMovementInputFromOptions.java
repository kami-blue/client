package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.PlayerUpdateMoveEvent;
import me.zeroeightsix.kami.module.modules.player.Freecam;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author ionar2
 * Used with explicit permission and MIT license permission
 * https://github.com/ionar2/salhack/blob/fa9e383/src/main/java/me/ionar/salhack/mixin/client/MixinMovementInputFromOptions.java
 * @see me.zeroeightsix.kami.module.modules.movement.InventoryMove
 */
@Mixin(MovementInputFromOptions.class)
public abstract class MixinMovementInputFromOptions extends MovementInput {
    @Inject(method = "updatePlayerMoveState", at = @At("RETURN"))
    public void updatePlayerMoveStateReturn(CallbackInfo callback) {
        KamiMod.EVENT_BUS.post(new PlayerUpdateMoveEvent());
    }

    // We have to cancel this so original player doesn't move around, also reset movement input when we enable Freecam
    @Inject(method = "updatePlayerMoveState", at = @At("HEAD"), cancellable = true)
    public void updatePlayerMoveStateHead(CallbackInfo ci) {
        if (Freecam.INSTANCE.isEnabled() && Freecam.INSTANCE.getCameraGuy() != null) ci.cancel();
        if (Freecam.INSTANCE.getResetInput()) {
            this.moveForward = 0f;
            this.moveStrafe = 0f;
            this.forwardKeyDown = false;
            this.backKeyDown = false;
            this.leftKeyDown = false;
            this.rightKeyDown = false;
            this.jump = false;
            this.sneak = false;
            Freecam.INSTANCE.setResetInput(false);
        }
    }
}
