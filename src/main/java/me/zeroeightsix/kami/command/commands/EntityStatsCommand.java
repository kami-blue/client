package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.AbstractHorse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendErrorMessage;

/**
 * Created by d1gress/Qther on 25/11/2017, updated on 16/12/2019
 * Updated by EmotionalLove on 16/12/2019
 * Updated by Dewy on 4th April, 2020
 */
public class EntityStatsCommand extends Command {

    public EntityStatsCommand() {
        super("entitystats", null, "estats");
        setDescription("Print the statistics of the entity you're currently riding");
    }

    @Override
    public void call(String[] args) {

        if (mc.player.getRidingEntity() != null && mc.player.getRidingEntity() instanceof AbstractHorse) {
            AbstractHorse horse = (AbstractHorse) mc.player.getRidingEntity();
            float maxHealth = horse.getMaxHealth();
            double speed = round(43.17 * horse.getAIMoveSpeed(), 2);
            double jump = round(-0.1817584952 * Math.pow(horse.getHorseJumpStrength(), 3) + 3.689713992 * Math.pow(horse.getHorseJumpStrength(), 2) + 2.128599134 * horse.getHorseJumpStrength() - 0.343930367, 4);
            String ownerId = horse.getOwnerUniqueId() == null ? "Not tamed." : horse.getOwnerUniqueId().toString();

            String builder = "&6Entity Statistics:" + "\n&cMax Health: " + maxHealth +
                      "\n&cSpeed: " + speed +
                      "\n&cJump: " + jump +
                      "\n&cOwner: " + Objects.requireNonNull(EntityUtil.getNameFromUUID(ownerId)).replace("\"", "");
            sendChatMessage(builder);
        } else if (mc.player.getRidingEntity() instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) mc.player.getRidingEntity();
            sendChatMessage("&6Entity Stats:\n&cMax Health: &b" + entity.getMaxHealth() + " &2HP" + "\n&cSpeed: &b" + round(43.17 * entity.getAIMoveSpeed(), 2) + " &2m/s");
        } else {
            sendErrorMessage("&4&lError: &cNot riding a compatible entity.");
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
