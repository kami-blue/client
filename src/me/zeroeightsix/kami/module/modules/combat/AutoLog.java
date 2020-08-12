// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.combat;

import me.zeroeightsix.kami.module.ModuleManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import java.util.function.Predicate;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import me.zero.alpine.listener.EventHandler;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "AutoLog", description = "Automatically log when in danger or on low health", category = Category.COMBAT)
public class AutoLog extends Module
{
    private Setting<Integer> health;
    private boolean shouldLog;
    long lastLog;
    @EventHandler
    private Listener<LivingDamageEvent> livingDamageEventListener;
    @EventHandler
    private Listener<EntityJoinWorldEvent> entityJoinWorldEventListener;
    
    public AutoLog() {
        this.health = this.register((Setting<Integer>)Settings.integerBuilder("Health").withRange(0, 36).withValue(6).build());
        this.shouldLog = false;
        this.lastLog = System.currentTimeMillis();
        this.livingDamageEventListener = new Listener<LivingDamageEvent>(event -> {
            if (AutoLog.mc.field_71439_g == null) {
                return;
            }
            else {
                if (event.getEntity() == AutoLog.mc.field_71439_g && AutoLog.mc.field_71439_g.func_110143_aJ() - event.getAmount() < this.health.getValue()) {
                    this.log();
                }
                return;
            }
        }, (Predicate<LivingDamageEvent>[])new Predicate[0]);
        this.entityJoinWorldEventListener = new Listener<EntityJoinWorldEvent>(event -> {
            if (AutoLog.mc.field_71439_g != null) {
                if (event.getEntity() instanceof EntityEnderCrystal && AutoLog.mc.field_71439_g.func_110143_aJ() - CrystalAura2.calculateDamage((EntityEnderCrystal)event.getEntity(), (Entity)AutoLog.mc.field_71439_g) < this.health.getValue()) {
                    this.log();
                }
            }
        }, (Predicate<EntityJoinWorldEvent>[])new Predicate[0]);
    }
    
    @Override
    public void onUpdate() {
        if (this.shouldLog) {
            this.shouldLog = false;
            if (System.currentTimeMillis() - this.lastLog < 2000L) {
                return;
            }
            Minecraft.func_71410_x().func_147114_u().func_147253_a(new SPacketDisconnect((ITextComponent)new TextComponentString("AutoLogged")));
        }
    }
    
    private void log() {
        ModuleManager.getModuleByName("AutoReconnect").disable();
        this.shouldLog = true;
        this.lastLog = System.currentTimeMillis();
    }
}
