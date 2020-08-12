// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.command.commands;

import java.util.Scanner;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.JsonElement;
import com.mojang.util.UUIDTypeAdapter;
import com.google.gson.JsonParser;
import java.util.Collection;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.Minecraft;
import java.util.Iterator;
import me.zeroeightsix.kami.util.Friends;
import java.util.ArrayList;
import me.zeroeightsix.kami.command.syntax.SyntaxParser;
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.command.Command;

public class FriendCommand extends Command
{
    public FriendCommand() {
        super("friend", new ChunkBuilder().append("mode", true, new EnumParser(new String[] { "add", "del" })).append("name").build());
        this.setDescription("Add someone as your friend!");
    }
    
    @Override
    public void call(final String[] args) {
        if (args[0] == null) {
            if (Friends.friends.getValue().isEmpty()) {
                Command.sendChatMessage("You currently don't have any friends added. &4friend add <name>&r to add one.");
                return;
            }
            String f = "";
            for (final Friends.Friend friend2 : Friends.friends.getValue()) {
                f = f + friend2.getUsername() + ", ";
            }
            f = f.substring(0, f.length() - 2);
            Command.sendChatMessage("Your friends: " + f);
        }
        else {
            if (args[1] == null) {
                Command.sendChatMessage(String.format(Friends.isFriend(args[0]) ? "Yes, %s is your friend." : "No, %s isn't a friend of yours.", args[0]));
                Command.sendChatMessage(String.format(Friends.isFriend(args[0]) ? "Yes, %s is your friend." : "No, %s isn't a friend of yours.", args[0]));
                return;
            }
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("new")) {
                if (Friends.isFriend(args[1])) {
                    Command.sendChatMessage("That player is already your friend.");
                    return;
                }
                final Friends.Friend f2;
                new Thread(() -> {
                    f2 = getFriendByName(args[1]);
                    if (f2 == null) {
                        Command.sendChatMessage("Failed to find UUID of " + args[1]);
                    }
                    else {
                        Friends.friends.getValue().add(f2);
                        Command.sendChatMessage("&4" + f2.getUsername() + "&r has been friended.");
                    }
                }).start();
            }
            else {
                if (!args[0].equalsIgnoreCase("del") && !args[0].equalsIgnoreCase("remove") && !args[0].equalsIgnoreCase("delete")) {
                    Command.sendChatMessage("Please specify either &4add&r or &cremove");
                    return;
                }
                if (!Friends.isFriend(args[1])) {
                    Command.sendChatMessage("That player isn't your friend.");
                    return;
                }
                final Friends.Friend friend3 = Friends.friends.getValue().stream().filter(friend1 -> friend1.getUsername().equalsIgnoreCase(args[1])).findFirst().get();
                Friends.friends.getValue().remove(friend3);
                Command.sendChatMessage("&4" + friend3.getUsername() + "&r has been unfriended.");
            }
        }
    }
    
    public static Friends.Friend getFriendByName(final String input) {
        final ArrayList<NetworkPlayerInfo> infoMap = new ArrayList<NetworkPlayerInfo>(Minecraft.func_71410_x().func_147114_u().func_175106_d());
        final NetworkPlayerInfo profile = infoMap.stream().filter(networkPlayerInfo -> networkPlayerInfo.func_178845_a().getName().equalsIgnoreCase(input)).findFirst().orElse(null);
        if (profile == null) {
            Command.sendChatMessage("Player isn't online. Looking up UUID..");
            final String s = requestIDs("[\"" + input + "\"]");
            if (s == null || s.isEmpty()) {
                Command.sendChatMessage("Couldn't find player ID. Are you connected to the internet? (0)");
            }
            else {
                final JsonElement element = new JsonParser().parse(s);
                if (element.getAsJsonArray().size() == 0) {
                    Command.sendChatMessage("Couldn't find player ID. (1)");
                }
                else {
                    try {
                        final String id = element.getAsJsonArray().get(0).getAsJsonObject().get("id").getAsString();
                        final String username = element.getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();
                        final Friends.Friend friend = new Friends.Friend(username, UUIDTypeAdapter.fromString(id));
                        return friend;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        Command.sendChatMessage("Couldn't find player ID. (2)");
                    }
                }
            }
            return null;
        }
        final Friends.Friend f = new Friends.Friend(profile.func_178845_a().getName(), profile.func_178845_a().getId());
        return f;
    }
    
    private static String requestIDs(final String data) {
        try {
            final String query = "https://api.mojang.com/profiles/minecraft";
            final String json = data;
            final URL url = new URL(query);
            final HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            final OutputStream os = conn.getOutputStream();
            os.write(json.getBytes(StandardCharsets.UTF_8));
            os.close();
            final InputStream in = new BufferedInputStream(conn.getInputStream());
            final String res = convertStreamToString(in);
            in.close();
            conn.disconnect();
            return res;
        }
        catch (Exception e) {
            return null;
        }
    }
    
    private static String convertStreamToString(final InputStream is) {
        final Scanner s = new Scanner(is).useDelimiter("\\A");
        final String r = s.hasNext() ? s.next() : "/";
        return r;
    }
}
