package org.kamiblue.client.mixin.client.accessor;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface AccessorMinecraft {

    @Accessor("timer")
    Timer getTimer();

    @Accessor("renderPartialTicksPaused")
    float getRenderPartialTicksPaused();

    @Accessor("rightClickDelayTimer")
    int getRightClickDelayTimer();

    @Accessor("rightClickDelayTimer")
    void setRightClickDelayTimer(int value);

    @Invoker("rightClickMouse")
    void invokeRightClickMouse();

    @Invoker("sendClickBlockToController")
    void invokeSendClickBlockToController(boolean leftClick);

}
