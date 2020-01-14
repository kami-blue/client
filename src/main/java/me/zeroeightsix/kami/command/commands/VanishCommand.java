package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

/**
 * Created by d1gress/Qther on 25/11/2017.
 */

public class VanishCommand extends Command {

    private static Entity vehicle;
    Minecraft mc = Minecraft.getMinecraft();

    public VanishCommand() {
        super("vanish", null, "entitydesync", "edesync", "entityvanish", "evanish", "ev", "van");
        setDescription("Allows you to vanish using an entity");
    }

    @Override
    public void call(String[] args) {
        if (mc.player.getRidingEntity() != null && vehicle == null) {
            vehicle = mc.player.getRidingEntity();
            mc.player.dismountRidingEntity();
            mc.world.removeEntityFromWorld(vehicle.getEntityId());
            Command.sendChatMessage("Vehicle " + vehicle.getName() + " removed.");
        } else {
            if (vehicle != null) {
                vehicle.isDead = false;
                mc.world.addEntityToWorld(vehicle.getEntityId(), vehicle);
                mc.player.startRiding(vehicle, true);
                Command.sendChatMessage("Vehicle " + vehicle.getName() + " created.");
                vehicle = null;
            } else {
                Command.sendChatMessage("No Vehicle.");
            }
        }
    }
}
