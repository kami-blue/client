package me.zeroeightsix.kami.mixin.client.accessor;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface AccessorMinecraft {

    @Accessor
    Timer getTimer();

    @Accessor
    float getRenderPartialTicksPaused();

    @Accessor
    int getRightClickDelayTimer();

    @Accessor
    void setRightClickDelayTimer(int value);

    @Invoker
    void invokeRightClickMouse();

}
