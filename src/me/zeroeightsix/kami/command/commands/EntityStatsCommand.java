// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.command.commands;

import java.math.RoundingMode;
import java.math.BigDecimal;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.AbstractHorse;
import me.zeroeightsix.kami.command.syntax.SyntaxChunk;
import me.zeroeightsix.kami.command.Command;

public class EntityStatsCommand extends Command
{
    public EntityStatsCommand() {
        super("entitystats", null);
        this.setDescription("Print the statistics of the entity you're currently riding");
    }
    
    @Override
    public void call(final String[] args) {
        if (this.mc.field_71439_g.func_184187_bx() != null && this.mc.field_71439_g.func_184187_bx() instanceof AbstractHorse) {
            final AbstractHorse horse = (AbstractHorse)this.mc.field_71439_g.func_184187_bx();
            final float maxHealth = horse.func_110138_aP();
            final double speed = round(43.17 * horse.func_70689_ay(), 2);
            final double jump = round(-0.1817584952 * Math.pow(horse.func_110215_cj(), 3.0) + 3.689713992 * Math.pow(horse.func_110215_cj(), 2.0) + 2.128599134 * horse.func_110215_cj() - 0.343930367, 4);
            final String ownerId = (horse.func_184780_dh() == null) ? "Not tamed." : horse.func_184780_dh().toString();
            final StringBuilder builder = new StringBuilder("&6Entity Statistics:");
            builder.append("\n&cMax Health: ").append(maxHealth);
            builder.append("\n&cSpeed: ").append(speed);
            builder.append("\n&cJump: ").append(jump);
            builder.append("\n&cOwner: ").append(ownerId);
            Command.sendChatMessage(builder.toString());
        }
        else if (this.mc.field_71439_g.func_184187_bx() instanceof EntityLivingBase) {
            final EntityLivingBase entity = (EntityLivingBase)this.mc.field_71439_g.func_184187_bx();
            Command.sendChatMessage("&6Entity Stats:\n&cMax Health: &b" + entity.func_110138_aP() + " &2HP\n&cSpeed: &b" + round(43.17 * entity.func_70689_ay(), 2) + " &2m/s");
        }
        else {
            Command.sendChatMessage("&4&lError: &cNot riding a compatible entity.");
        }
    }
    
    public static double round(final double value, final int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
