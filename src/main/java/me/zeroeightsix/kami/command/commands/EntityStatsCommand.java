package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.AbstractHorse;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by d1gress/Qther on 25/11/2017, updated on 16/12/2019
 * Updated by EmotionalLove on 16/12/2019
 */

public class EntityStatsCommand extends Command {

    public EntityStatsCommand() {
        super("entitystats", null, "estats", "horestats", "hstats", "vehiclestats");
        setDescription("Print the statistics of the entity you're currently riding");
    }

    @Override
    public void call(String[] args) {
        if (mc.player.getRidingEntity() != null && mc.player.getRidingEntity() instanceof AbstractHorse) {
            AbstractHorse horse = (AbstractHorse) mc.player.getRidingEntity();
            // TODO: Function that resolves UUID's to Minecraft usernames. @bella
            float maxHealth = horse.getMaxHealth();
            double speed = round(43.17 * horse.getAIMoveSpeed(), 2);
            double jump = round(-0.1817584952 * Math.pow(horse.getHorseJumpStrength(), 3) + 3.689713992 * Math.pow(horse.getHorseJumpStrength(), 2) + 2.128599134 * horse.getHorseJumpStrength() - 0.343930367, 4);
            String ownerId = horse.getOwnerUniqueId() == null ? "Not tamed." : horse.getOwnerUniqueId().toString();

            StringBuilder builder = new StringBuilder("&6Entity Statistics:");
            builder.append("\n&cMax Health: ").append(maxHealth);
            builder.append("\n&cSpeed: ").append(speed);
            builder.append("\n&cJump: ").append(jump);
            builder.append("\n&cOwner: ").append(ownerId);

            Command.sendChatMessage(builder.toString());
        } else if (mc.player.getRidingEntity() instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) mc.player.getRidingEntity();
            Command.sendChatMessage("&6Entity Stats:\n&cMax Health: &b" + entity.getMaxHealth() + " &2HP" + "\n&cSpeed: &b" + round(43.17 * entity.getAIMoveSpeed(), 2) + " &2m/s");
        } else {
            Command.sendChatMessage("&4&lError: &cNot riding a compatible entity.");
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
