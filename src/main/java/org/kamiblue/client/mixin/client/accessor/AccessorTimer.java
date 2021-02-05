package org.kamiblue.client.mixin.client.accessor;

import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Timer.class)
public interface AccessorTimer {

    @Accessor("tickLength")
    float getTickLength();

    @Accessor("tickLength")
    void setTickLength(float value);

}
