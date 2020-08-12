// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.combat;

import java.util.Iterator;
import me.zeroeightsix.kami.command.Command;
import net.minecraft.init.MobEffects;
import java.util.Map;
import java.util.Collections;
import java.util.WeakHashMap;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Set;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "StrengthDetect", category = Category.COMBAT, description = "Hits entities around you")
public class StrengthDetect extends Module
{
    private Setting<Boolean> watermark;
    private Setting<Boolean> color;
    private Set<EntityPlayer> str;
    public static final Minecraft mc;
    
    public StrengthDetect() {
        this.watermark = this.register(Settings.b("Watermark", true));
        this.color = this.register(Settings.b("Color", false));
        this.str = Collections.newSetFromMap(new WeakHashMap<EntityPlayer, Boolean>());
    }
    
    @Override
    public void onUpdate() {
        for (final EntityPlayer player : StrengthDetect.mc.field_71441_e.field_73010_i) {
            if (player.equals((Object)StrengthDetect.mc.field_71439_g)) {
                continue;
            }
            if (player.func_70644_a(MobEffects.field_76420_g) && !this.str.contains(player)) {
                if (this.watermark.getValue()) {
                    if (this.color.getValue()) {
                        Command.sendChatMessage("&a" + player.getDisplayNameString() + " has drank strength");
                    }
                    else {
                        Command.sendChatMessage(player.getDisplayNameString() + " has drank strength");
                    }
                }
                else if (this.color.getValue()) {
                    Command.sendRawChatMessage("&a" + player.getDisplayNameString() + " has drank strength");
                }
                else {
                    Command.sendRawChatMessage(player.getDisplayNameString() + " has drank strength");
                }
                this.str.add(player);
            }
            if (!this.str.contains(player)) {
                continue;
            }
            if (player.func_70644_a(MobEffects.field_76420_g)) {
                continue;
            }
            if (this.watermark.getValue()) {
                if (this.color.getValue()) {
                    Command.sendChatMessage("&c" + player.getDisplayNameString() + " has ran out of strength");
                }
                else {
                    Command.sendChatMessage(player.getDisplayNameString() + " has ran out of strength");
                }
            }
            else if (this.color.getValue()) {
                Command.sendRawChatMessage("&c" + player.getDisplayNameString() + " has ran out of strength");
            }
            else {
                Command.sendRawChatMessage(player.getDisplayNameString() + " has ran out of strength");
            }
            this.str.remove(player);
        }
    }
    
    static {
        mc = Minecraft.func_71410_x();
    }
}
