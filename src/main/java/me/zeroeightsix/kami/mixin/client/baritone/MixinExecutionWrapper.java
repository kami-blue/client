package me.zeroeightsix.kami.mixin.client.baritone;

import baritone.api.command.ICommand;
import me.zeroeightsix.kami.event.KamiEventBus;
import me.zeroeightsix.kami.event.events.BaritoneCommandEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "baritone.command.manager.CommandManager$ExecutionWrapper", remap = false)
public class MixinExecutionWrapper {

    @Shadow private ICommand command;

    @Inject(method = "execute", at = @At("HEAD"))
    private void execute(CallbackInfo ci) {
        KamiEventBus.INSTANCE.post(new BaritoneCommandEvent(this.command));
    }

}
