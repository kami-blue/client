// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.movement;

import net.minecraft.util.MovementInput;
import java.util.function.Predicate;
import me.zero.alpine.listener.EventHandler;
import net.minecraftforge.client.event.InputUpdateEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;

@Info(name = "NoSlowDown", category = Category.MOVEMENT)
public class NoSlowDown extends Module
{
    @EventHandler
    private Listener<InputUpdateEvent> eventListener;
    
    public NoSlowDown() {
        final MovementInput movementInput;
        final MovementInput movementInput2;
        this.eventListener = new Listener<InputUpdateEvent>(event -> {
            if (NoSlowDown.mc.field_71439_g.func_184587_cr() && !NoSlowDown.mc.field_71439_g.func_184218_aH()) {
                event.getMovementInput();
                movementInput.field_78902_a *= 5.0f;
                event.getMovementInput();
                movementInput2.field_192832_b *= 5.0f;
            }
        }, (Predicate<InputUpdateEvent>[])new Predicate[0]);
    }
}
