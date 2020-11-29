package me.zeroeightsix.kami.mixin.client.accessor;

import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Timer.class)
public interface AccessorTimer {

    @Accessor
    float getTickLength();

    @Accessor
    void setTickLength(float value);

}
