package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by d1gress/Qther on 25/11/2017.
 */

public class EntityStatsCommand extends Command {

    private String maxHealth;
    private String speed;
    private String jumpHeight;
    Minecraft mc = Minecraft.getMinecraft();
    
    public EntityStatsCommand() {
        super("estats", null);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public void call(String[] args) {
        if (mc.player.getRidingEntity() != null) {
            if     (mc.player.getRidingEntity() instanceof EntityHorse ||
                    mc.player.getRidingEntity() instanceof EntityDonkey ||
                    mc.player.getRidingEntity() instanceof EntityLlama ||
                    mc.player.getRidingEntity() instanceof EntityMule ||
                    mc.player.getRidingEntity() instanceof EntitySkeletonHorse ||
                    mc.player.getRidingEntity() instanceof EntityZombieHorse ||
                    mc.player.getRidingEntity() instanceof AbstractHorse) {
                AbstractHorse h = (AbstractHorse) mc.player.getRidingEntity();
                maxHealth = h.getMaxHealth() + " §2HP";
                speed = round(43.17 * h.getAIMoveSpeed(), 2) + " §2m/s";
                jumpHeight = round(-0.1817584952 * Math.pow(h.getHorseJumpStrength(), 3) + 3.689713992 * Math.pow(h.getHorseJumpStrength(), 2) + 2.128599134 * h.getHorseJumpStrength() - 0.343930367, 4) + " §2m";
                Command.sendChatMessage("§6Entity Stats:\n§cMax Health: §b" + maxHealth + "\n§cSpeed: §b" + speed + "\n§cJump: §b" + jumpHeight);
            }else if (mc.player.getRidingEntity() instanceof EntityLivingBase) {
                EntityLivingBase l = (EntityLivingBase) mc.player.getRidingEntity();
                maxHealth = l.getMaxHealth() + " §2HP";
                speed = round(43.17 * l.getAIMoveSpeed(), 2) + " §2m/s";
                Command.sendChatMessage("§6Entity Stats:\n§cMax Health: §b" + maxHealth + "\n§cSpeed: §b" + speed);
            }
        }else {
            Command.sendChatMessage("§4§lERROR: §cNot riding a living entity.");
        }
    }

}
