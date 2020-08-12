// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.movement;

import net.minecraft.client.entity.EntityPlayerSP;
import java.util.function.Predicate;
import org.lwjgl.input.Keyboard;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiContainer;
import me.zeroeightsix.kami.setting.Settings;
import me.zero.alpine.listener.EventHandler;
import net.minecraftforge.client.event.InputUpdateEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "GuiMove", category = Category.MOVEMENT)
public class GuiMove extends Module
{
    private Setting<Integer> pitchSpeed;
    private Setting<Integer> yawSpeed;
    private Setting<Boolean> chat;
    @EventHandler
    private Listener<InputUpdateEvent> inputUpdateEventListener;
    
    public GuiMove() {
        this.pitchSpeed = this.register(Settings.i("PitchSpeed", 6));
        this.yawSpeed = this.register(Settings.i("YawSpeed", 6));
        this.chat = this.register(Settings.b("Chat", false));
        this.inputUpdateEventListener = new Listener<InputUpdateEvent>(event -> {
            if (GuiMove.mc.field_71462_r instanceof GuiContainer || (GuiMove.mc.field_71462_r instanceof GuiChat && this.chat.getValue())) {
                if (Keyboard.isKeyDown(GuiMove.mc.field_71474_y.field_74351_w.func_151463_i())) {
                    event.getMovementInput().field_192832_b = 1.0f;
                }
                if (Keyboard.isKeyDown(GuiMove.mc.field_71474_y.field_74368_y.func_151463_i())) {
                    event.getMovementInput().field_192832_b = -1.0f;
                }
                if (Keyboard.isKeyDown(GuiMove.mc.field_71474_y.field_74370_x.func_151463_i())) {
                    event.getMovementInput().field_78902_a = 1.0f;
                }
                if (Keyboard.isKeyDown(GuiMove.mc.field_71474_y.field_74366_z.func_151463_i())) {
                    event.getMovementInput().field_78902_a = -1.0f;
                }
            }
        }, (Predicate<InputUpdateEvent>[])new Predicate[0]);
    }
    
    @Override
    public void onUpdate() {
        if (GuiMove.mc.field_71462_r instanceof GuiContainer || (GuiMove.mc.field_71462_r instanceof GuiChat && this.chat.getValue())) {
            if (GuiMove.mc.field_71439_g.func_180799_ab() || GuiMove.mc.field_71439_g.func_70090_H()) {
                final EntityPlayerSP field_71439_g;
                final EntityPlayerSP player = field_71439_g = GuiMove.mc.field_71439_g;
                field_71439_g.field_70181_x += 0.039000000804662704;
            }
            else if (GuiMove.mc.field_71439_g.field_70122_E && Keyboard.isKeyDown(GuiMove.mc.field_71474_y.field_74314_A.func_151463_i())) {
                GuiMove.mc.field_71439_g.func_70664_aZ();
            }
            if (Keyboard.isKeyDown(200)) {
                final EntityPlayerSP field_71439_g2 = GuiMove.mc.field_71439_g;
                field_71439_g2.field_70125_A -= this.pitchSpeed.getValue();
            }
            if (Keyboard.isKeyDown(208)) {
                final EntityPlayerSP field_71439_g3 = GuiMove.mc.field_71439_g;
                field_71439_g3.field_70125_A += this.pitchSpeed.getValue();
            }
            if (Keyboard.isKeyDown(205)) {
                final EntityPlayerSP field_71439_g4 = GuiMove.mc.field_71439_g;
                field_71439_g4.field_70177_z += this.yawSpeed.getValue();
            }
            if (Keyboard.isKeyDown(203)) {
                final EntityPlayerSP field_71439_g5 = GuiMove.mc.field_71439_g;
                field_71439_g5.field_70177_z -= this.yawSpeed.getValue();
            }
            if (Keyboard.isKeyDown(GuiMove.mc.field_71474_y.field_151444_V.func_151463_i())) {
                GuiMove.mc.field_71439_g.func_70031_b(true);
            }
        }
    }
}
