// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.misc;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.entity.Entity;
import java.util.function.Consumer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import java.util.function.Predicate;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.client.Minecraft;
import me.zeroeightsix.kami.event.events.RenderEvent;
import java.util.ArrayList;
import me.zeroeightsix.kami.module.Module;

@Info(name = "AutoToxicity", category = Category.MISC, description = "Tells players to fuck outta your way.")
public class AutoToxicity extends Module
{
    public ArrayList<String> names;
    public ArrayList<String> names2;
    public ArrayList<String> removal;
    
    public AutoToxicity() {
        this.names = new ArrayList<String>();
        this.names2 = new ArrayList<String>();
        this.removal = new ArrayList<String>();
    }
    
    @Override
    public void onWorldRender(final RenderEvent event) {
        this.names2.clear();
        Minecraft.func_71410_x().field_71441_e.field_72996_f.stream().filter(EntityUtil::isLiving).filter(entity -> !EntityUtil.isFakeLocalPlayer(entity)).filter(entity -> entity instanceof EntityPlayer).filter(entity -> !(entity instanceof EntityPlayerSP)).forEach(this::testName);
        this.testLeave();
    }
    
    private void testName(final Entity entityIn) {
        this.names2.add(entityIn.func_70005_c_());
        if (!this.names.contains(entityIn.func_70005_c_())) {
            this.sendMessage(entityIn);
            this.names.add(entityIn.func_70005_c_());
        }
    }
    
    private void testLeave() {
        this.names.forEach(name -> {
            if (!this.names2.contains(name)) {
                this.removal.add(name);
            }
            return;
        });
        this.removal.forEach(name -> this.names.remove(name));
        this.removal.clear();
    }
    
    private void sendMessage(final Entity entityIn) {
        Minecraft.func_71410_x().field_71442_b.field_78774_b.func_147297_a((Packet)new CPacketChatMessage("/w " + entityIn.func_70005_c_() + " Why are you here " + entityIn.func_70005_c_() + "? Get the fuck out of my way."));
    }
    
    private enum AutoToxicityMode
    {
        PRIVATE, 
        PUBLIC;
    }
}
