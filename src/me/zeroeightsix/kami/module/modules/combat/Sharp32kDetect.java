// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.combat;

import java.util.Iterator;
import me.zeroeightsix.kami.command.Command;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemStack;
import java.util.Map;
import java.util.Collections;
import java.util.WeakHashMap;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Set;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Sharp32kDetect", category = Category.COMBAT, description = "Hits entities around you")
public class Sharp32kDetect extends Module
{
    private Setting<Boolean> watermark;
    private Setting<Boolean> color;
    private Set<EntityPlayer> sword;
    public static final Minecraft mc;
    
    public Sharp32kDetect() {
        this.watermark = this.register(Settings.b("Watermark", true));
        this.color = this.register(Settings.b("Color", false));
        this.sword = Collections.newSetFromMap(new WeakHashMap<EntityPlayer, Boolean>());
    }
    
    private boolean is32k(final EntityPlayer player, final ItemStack stack) {
        if (stack.func_77973_b() instanceof ItemSword) {
            final NBTTagList enchants = stack.func_77986_q();
            if (enchants != null) {
                for (int i = 0; i < enchants.func_74745_c(); ++i) {
                    if (enchants.func_150305_b(i).func_74765_d("lvl") >= 32767) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    @Override
    public void onUpdate() {
        for (final EntityPlayer player : Sharp32kDetect.mc.field_71441_e.field_73010_i) {
            if (player.equals((Object)Sharp32kDetect.mc.field_71439_g)) {
                continue;
            }
            if (this.is32k(player, player.field_184831_bT) && !this.sword.contains(player)) {
                if (this.watermark.getValue()) {
                    if (this.color.getValue()) {
                        Command.sendChatMessage("&4" + player.getDisplayNameString() + " is holding a 32k");
                    }
                    else {
                        Command.sendChatMessage(player.getDisplayNameString() + " is holding a 32k");
                    }
                }
                else if (this.color.getValue()) {
                    Command.sendRawChatMessage("&4" + player.getDisplayNameString() + " is holding a 32k");
                }
                else {
                    Command.sendRawChatMessage(player.getDisplayNameString() + " is holding a 32k");
                }
                this.sword.add(player);
            }
            if (!this.sword.contains(player)) {
                continue;
            }
            if (this.is32k(player, player.field_184831_bT)) {
                continue;
            }
            if (this.watermark.getValue()) {
                if (this.color.getValue()) {
                    Command.sendChatMessage("&2" + player.getDisplayNameString() + " is no longer holding a 32k");
                }
                else {
                    Command.sendChatMessage(player.getDisplayNameString() + " is no longer holding a 32k");
                }
            }
            else if (this.color.getValue()) {
                Command.sendRawChatMessage("&2" + player.getDisplayNameString() + " is no longer holding a 32k");
            }
            else {
                Command.sendRawChatMessage(player.getDisplayNameString() + " is no longer holding a 32k");
            }
            this.sword.remove(player);
        }
    }
    
    static {
        mc = Minecraft.func_71410_x();
    }
}
