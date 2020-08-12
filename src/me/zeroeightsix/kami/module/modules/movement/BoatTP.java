// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import java.util.Comparator;
import net.minecraft.entity.item.EntityBoat;
import org.lwjgl.input.Keyboard;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraftforge.fml.client.FMLClientHandler;
import me.zeroeightsix.kami.module.Module;

@Info(name = "BoatTP", category = Category.MOVEMENT, description = "Charlie's boatTP module\nUse up/down arrow keys.")
public class BoatTP extends Module
{
    public KeyUpNotifier upKeyNotif;
    public KeyDownNotifier downKeyNotif;
    
    public BoatTP() {
        this.upKeyNotif = new KeyUpNotifier();
        this.downKeyNotif = new KeyDownNotifier();
    }
    
    public static void clip(final int y) {
        final Entity boat = BoatTP.mc.field_71439_g.func_184187_bx();
        boat.func_70634_a(BoatTP.mc.field_71439_g.field_70165_t, (double)y, BoatTP.mc.field_71439_g.field_70161_v);
        FMLClientHandler.instance().getClientToServerNetworkManager().func_179290_a((Packet)new CPacketVehicleMove(boat));
    }
    
    public static int findNextAvailableSpaceUp(final int start) {
        int up = start;
        final int playerX = (int)BoatTP.mc.field_71439_g.field_70165_t;
        final int playerZ = (int)BoatTP.mc.field_71439_g.field_70161_v;
        for (int i = start + 1; i < ((BoatTP.mc.field_71439_g.field_71093_bK != 1) ? 257 : 125); ++i) {
            if (BoatTP.mc.field_71441_e.func_180495_p(new BlockPos(playerX, i, playerZ)).func_177230_c().field_149764_J.equals(Material.field_151579_a) && BoatTP.mc.field_71441_e.func_180495_p(new BlockPos(playerX, i + 1, playerZ)).func_177230_c().field_149764_J.equals(Material.field_151579_a) && !BoatTP.mc.field_71441_e.func_180495_p(new BlockPos(playerX, i - 1, playerZ)).func_177230_c().field_149764_J.equals(Material.field_151579_a)) {
                up = i;
                return up;
            }
        }
        return up;
    }
    
    public static int findNextAvailableSpaceDown(final int start) {
        int down = start;
        final int playerX = (int)BoatTP.mc.field_71439_g.field_70165_t;
        final int playerZ = (int)BoatTP.mc.field_71439_g.field_70161_v;
        for (int i = start - 1; i > 4; --i) {
            if (BoatTP.mc.field_71441_e.func_180495_p(new BlockPos(playerX, i, playerZ)).func_177230_c().field_149764_J.equals(Material.field_151579_a) && BoatTP.mc.field_71441_e.func_180495_p(new BlockPos(playerX, i + 1, playerZ)).func_177230_c().field_149764_J.equals(Material.field_151579_a) && !BoatTP.mc.field_71441_e.func_180495_p(new BlockPos(playerX, i - 1, playerZ)).func_177230_c().field_149764_J.equals(Material.field_151579_a)) {
                down = i;
                return down;
            }
        }
        return down;
    }
    
    @Override
    public void onUpdate() {
        if (BoatTP.mc.field_71439_g.field_70173_aa % 2 != 0) {
            return;
        }
        this.upKeyNotif.set(Keyboard.isKeyDown(200));
        this.downKeyNotif.set(Keyboard.isKeyDown(208));
        BoatTP.mc.field_71441_e.func_72910_y().stream().filter(entity -> entity instanceof EntityBoat).map(entity -> entity).filter(entityBoat -> BoatTP.mc.field_71439_g.func_70032_d(entityBoat) < 3.0f).min(Comparator.comparing(entityBoat -> entityBoat.func_70032_d((Entity)BoatTP.mc.field_71439_g))).ifPresent(entityBoat -> {
            if (BoatTP.mc.field_71439_g.func_184187_bx() == null) {
                BoatTP.mc.field_71442_b.func_187097_a((EntityPlayer)BoatTP.mc.field_71439_g, entityBoat, EnumHand.MAIN_HAND);
            }
        });
    }
    
    @Override
    protected void onEnable() {
        if (BoatTP.mc.field_71439_g == null) {
            this.disable();
            return;
        }
        BoatTP.mc.field_71439_g.func_145747_a((ITextComponent)new TextComponentString("BoatTP enabled, get in a boat to use."));
    }
    
    class KeyUpNotifier extends LazyKeyPressNotifier
    {
        @Override
        public void onKeyDown() {
            Minecraft.func_71410_x().field_71439_g.func_145747_a((ITextComponent)new TextComponentString("Going up!"));
            if (Minecraft.func_71410_x().field_71439_g.func_184187_bx() instanceof EntityBoat && FMLClientHandler.instance().getClientToServerNetworkManager() != null && Keyboard.isKeyDown(200)) {
                BoatTP.clip(BoatTP.findNextAvailableSpaceUp((int)Minecraft.func_71410_x().field_71439_g.func_184187_bx().field_70163_u));
            }
        }
    }
    
    class KeyDownNotifier extends LazyKeyPressNotifier
    {
        @Override
        public void onKeyDown() {
            Minecraft.func_71410_x().field_71439_g.func_145747_a((ITextComponent)new TextComponentString("Going down!"));
            if (Minecraft.func_71410_x().field_71439_g.func_184187_bx() instanceof EntityBoat && FMLClientHandler.instance().getClientToServerNetworkManager() != null && Keyboard.isKeyDown(208)) {
                BoatTP.clip(BoatTP.findNextAvailableSpaceDown((int)Minecraft.func_71410_x().field_71439_g.func_184187_bx().field_70163_u));
            }
        }
    }
    
    abstract class LazyKeyPressNotifier
    {
        boolean pressed;
        
        public void set(final boolean value) {
            if (value && !this.pressed) {
                this.onKeyDown();
            }
            this.pressed = value;
        }
        
        public abstract void onKeyDown();
    }
}
