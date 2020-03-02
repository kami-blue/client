package me.zeroeightsix.kami.module.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.commands.FriendCommand;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Mouse;

/**
 * @author Indrit
 * Updated by Indrit on 02/03/20
 */

@Module.Info(name = "MidClickFriends", category = Module.Category.MISC, description = "Middle click payers to friend/unfriend them")
public class MidClickFriends extends Module {
    private int delay = 0;

    @Override
    public void onUpdate(){
        if (delay > 0) {
            delay--;
        }
    }

    @EventHandler
    public Listener<InputEvent.MouseInputEvent> mouseListener = new Listener<>(event -> {
        if (delay == 0) {
            if(Mouse.getEventButton() == 2) { // Because 2 is middle click on mouse, wtf???
                if (Minecraft.getMinecraft().objectMouseOver.typeOfHit.equals(RayTraceResult.Type.ENTITY)) {
                    Entity uwu = Minecraft.getMinecraft().objectMouseOver.entityHit;
                    if (!(uwu instanceof EntityOtherPlayerMP)) {
                        return;
                    }
                    if (Friends.isFriend(uwu.getName())) {
                        remove(uwu.getName());
                    } else {
                        add(uwu.getName());
                    }
                }
            }
        }
    });

    private void remove(String name){
        delay = 20;
        Friends.Friend friend = Friends.INSTANCE.friends.getValue().stream().filter(friend1 -> friend1.getUsername().equalsIgnoreCase(name)).findFirst().get();
        Friends.INSTANCE.friends.getValue().remove(friend);
        Command.sendChatMessage("&b" + friend.getUsername() + "&r has been unfriended.");
    }

    private void add(String name){
        delay = 20;
        new Thread(() -> {
            Friends.Friend f = new FriendCommand().getFriendByName(name);
            if (f == null) {
                Command.sendChatMessage("Failed to find UUID of " + name);
                return;
            }
            Friends.INSTANCE.friends.getValue().add(f);
            Command.sendChatMessage("&b" + f.getUsername() + "&r has been friended.");
        }).start();
    }
}
