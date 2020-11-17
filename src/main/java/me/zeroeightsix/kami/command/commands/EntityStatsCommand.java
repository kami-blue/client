package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.manager.managers.UUIDManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.AbstractHorse;
import org.kamiblue.capeapi.PlayerProfile;
import org.kamiblue.commons.utils.MathUtils;

import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage;
import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendErrorMessage;

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
            double speed = MathUtils.INSTANCE.round(43.17 * horse.getAIMoveSpeed(), 2);
            double jump = MathUtils.INSTANCE.round(-0.1817584952 * Math.pow(horse.getHorseJumpStrength(), 3) + 3.689713992 * Math.pow(horse.getHorseJumpStrength(), 2) + 2.128599134 * horse.getHorseJumpStrength() - 0.343930367, 4);
            String ownerId = horse.getOwnerUniqueId() == null ? "Not tamed." : horse.getOwnerUniqueId().toString();

            PlayerProfile ownerProfile = UUIDManager.INSTANCE.getByString(ownerId);
            String ownerName = "";
            if (ownerProfile != null) ownerName = ownerProfile.getName();

            String builder = "&6Entity Statistics:" + "\n&cMax Health: " + maxHealth +
                    "\n&cSpeed: " + speed +
                    "\n&cJump: " + jump +
                    "\n&cOwner: " + ownerName;
            sendChatMessage(builder);
        } else if (mc.player.getRidingEntity() instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) mc.player.getRidingEntity();
            sendChatMessage("&6Entity Stats:\n&cMax Health: &b" + entity.getMaxHealth() + " &2HP" + "\n&cSpeed: &b" + MathUtils.INSTANCE.round(43.17 * entity.getAIMoveSpeed(), 2) + " &2m/s");
        } else {
            sendErrorMessage("&4&lError: &cNot riding a compatible entity.");
        }
    }
}
