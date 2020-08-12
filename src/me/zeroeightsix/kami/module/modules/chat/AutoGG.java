// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.chat;

import java.util.Objects;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketChatMessage;
import java.util.Iterator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import java.util.function.Predicate;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.world.World;
import net.minecraft.network.play.client.CPacketUseEntity;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.setting.Setting;
import java.util.concurrent.ConcurrentHashMap;
import me.zeroeightsix.kami.module.Module;

@Info(name = "AutoGG", category = Category.CHAT, description = "Announce killed Players")
public class AutoGG extends Module
{
    private ConcurrentHashMap<String, Integer> targetedPlayers;
    private Setting<Boolean> toxicMode;
    private Setting<Boolean> clientName;
    private Setting<Integer> timeoutTicks;
    @EventHandler
    public Listener<PacketEvent.Send> sendListener;
    @EventHandler
    public Listener<LivingDeathEvent> livingDeathEventListener;
    
    public AutoGG() {
        this.targetedPlayers = null;
        this.toxicMode = this.register(Settings.b("Svenska", false));
        this.clientName = this.register(Settings.b("AstraMod", true));
        this.timeoutTicks = this.register(Settings.i("TimeoutTicks", 20));
        CPacketUseEntity cPacketUseEntity;
        Entity targetEntity;
        this.sendListener = new Listener<PacketEvent.Send>(event -> {
            if (AutoGG.mc.field_71439_g == null) {
                return;
            }
            else {
                if (this.targetedPlayers == null) {
                    this.targetedPlayers = new ConcurrentHashMap<String, Integer>();
                }
                if (!(event.getPacket() instanceof CPacketUseEntity)) {
                    return;
                }
                else {
                    cPacketUseEntity = (CPacketUseEntity)event.getPacket();
                    if (!cPacketUseEntity.func_149565_c().equals((Object)CPacketUseEntity.Action.ATTACK)) {
                        return;
                    }
                    else {
                        targetEntity = cPacketUseEntity.func_149564_a((World)AutoGG.mc.field_71441_e);
                        if (!EntityUtil.isPlayer(targetEntity)) {
                            return;
                        }
                        else {
                            this.addTargetedPlayer(targetEntity.func_70005_c_());
                            return;
                        }
                    }
                }
            }
        }, (Predicate<PacketEvent.Send>[])new Predicate[0]);
        EntityLivingBase entity;
        EntityPlayer player;
        String name;
        this.livingDeathEventListener = new Listener<LivingDeathEvent>(event -> {
            if (AutoGG.mc.field_71439_g != null) {
                if (this.targetedPlayers == null) {
                    this.targetedPlayers = new ConcurrentHashMap<String, Integer>();
                }
                entity = event.getEntityLiving();
                if (entity != null) {
                    if (!(!EntityUtil.isPlayer((Entity)entity))) {
                        player = (EntityPlayer)entity;
                        if (player.func_110143_aJ() <= 0.0f) {
                            name = player.func_70005_c_();
                            if (this.shouldAnnounce(name)) {
                                this.doAnnounce(name);
                            }
                        }
                    }
                }
            }
        }, (Predicate<LivingDeathEvent>[])new Predicate[0]);
    }
    
    public void onEnable() {
        this.targetedPlayers = new ConcurrentHashMap<String, Integer>();
    }
    
    public void onDisable() {
        this.targetedPlayers = null;
    }
    
    @Override
    public void onUpdate() {
        if (this.isDisabled() || AutoGG.mc.field_71439_g == null) {
            return;
        }
        if (this.targetedPlayers == null) {
            this.targetedPlayers = new ConcurrentHashMap<String, Integer>();
        }
        for (final Entity entity : AutoGG.mc.field_71441_e.func_72910_y()) {
            if (!EntityUtil.isPlayer(entity)) {
                continue;
            }
            final EntityPlayer player = (EntityPlayer)entity;
            if (player.func_110143_aJ() > 0.0f) {
                continue;
            }
            final String name2 = player.func_70005_c_();
            if (this.shouldAnnounce(name2)) {
                this.doAnnounce(name2);
                break;
            }
        }
        this.targetedPlayers.forEach((name, timeout) -> {
            if (timeout <= 0) {
                this.targetedPlayers.remove(name);
            }
            else {
                this.targetedPlayers.put(name, timeout - 1);
            }
        });
    }
    
    private boolean shouldAnnounce(final String name) {
        return this.targetedPlayers.containsKey(name);
    }
    
    private void doAnnounce(final String name) {
        this.targetedPlayers.remove(name);
        final StringBuilder message = new StringBuilder();
        if (this.toxicMode.getValue()) {
            message.append("ded.");
        }
        else {
            message.append("Ezzz ");
        }
        message.append(name);
        message.append("!");
        if (this.clientName.getValue()) {
            message.append(" ");
            message.append("AstraMod");
            message.append(" On top!! CrawlerExE Rules me and all!!");
        }
        String messageSanitized = message.toString().replaceAll("§", "");
        if (messageSanitized.length() > 255) {
            messageSanitized = messageSanitized.substring(0, 255);
        }
        AutoGG.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketChatMessage(messageSanitized));
    }
    
    public void addTargetedPlayer(final String name) {
        if (Objects.equals(name, AutoGG.mc.field_71439_g.func_70005_c_())) {
            return;
        }
        if (this.targetedPlayers == null) {
            this.targetedPlayers = new ConcurrentHashMap<String, Integer>();
        }
        this.targetedPlayers.put(name, this.timeoutTicks.getValue());
    }
}
