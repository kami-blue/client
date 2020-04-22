package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import net.minecraft.entity.Entity;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;

/**
 * Created by d1gress/Qther on 25/11/2017.
 */

public class VanishCommand extends Command {

    private static Entity vehicle;

    public VanishCommand() {
        super("vanish", null);
        setDescription("Allows you to vanish using an entity");
    }

    @Override
    public void call(String[] args) {
        if (mc.player.getRidingEntity() != null && vehicle == null) {
            vehicle = mc.player.getRidingEntity();
            mc.player.dismountRidingEntity();
            mc.world.removeEntityFromWorld(vehicle.getEntityId());
            sendChatMessage("Vehicle " + vehicle.getName() + " removed.");
        } else {
            if (vehicle != null) {
                vehicle.isDead = false;
                mc.world.addEntityToWorld(vehicle.getEntityId(), vehicle);
                mc.player.startRiding(vehicle, true);
                sendChatMessage("Vehicle " + vehicle.getName() + " created.");
                vehicle = null;
            } else {
                sendChatMessage("No Vehicle.");
            }
        }
    }
}
