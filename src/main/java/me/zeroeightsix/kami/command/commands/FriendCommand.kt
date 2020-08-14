package me.zeroeightsix.kami.command.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.util.UUIDTypeAdapter;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;

/**
 * Created by 086 on 14/12/2017.
 */
public class FriendCommand extends Command {

    public FriendCommand() {
        super("friend", new ChunkBuilder()
                .append("mode", true, new EnumParser(new String[]{"add", "del"}))
                .append("name")
                .build(), "f");
        setDescription("Add someone as your friend!");
    }

    @Override
    public void call(String[] args) {
        if (args[0] == null) {
            if (Friends.friends.getValue().isEmpty()) {
                sendChatMessage("You currently don't have any friends added. &bfriend add <name>&r to add one.");
                return;
            }
            String f = "";
            for (Friends.Friend friend : Friends.friends.getValue())
                f += friend.getUsername() + ", ";
            f = f.substring(0, f.length() - 2);
            sendChatMessage("Your friends: " + f);
        } else {
            if (args[1] == null) {
                sendChatMessage(String.format(Friends.isFriend(args[0]) ? "Yes, %s is your friend." : "No, %s isn't a friend of yours.", args[0]));
                return;
            }

            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("new")) {
                if (Friends.isFriend(args[1])) {
                    sendChatMessage("That player is already your friend.");
                    return;
                }

                // New thread because of potential internet connection made
                new Thread(() -> {
                    Friends.Friend f = getFriendByName(args[1]);
                    if (f == null) {
                        sendChatMessage("Failed to find UUID of " + args[1]);
                        return;
                    }
                    Friends.friends.getValue().add(f);
                    sendChatMessage("&b" + f.getUsername() + "&r has been friended.");
                }).start();

            } else if (args[0].equalsIgnoreCase("del") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete")) {
                if (!Friends.isFriend(args[1])) {
                    sendChatMessage("That player isn't your friend.");
                    return;
                }

                Friends.Friend friend = Friends.friends.getValue().stream().filter(friend1 -> friend1.getUsername().equalsIgnoreCase(args[1])).findFirst().get();
                Friends.friends.getValue().remove(friend);
                sendChatMessage("&b" + friend.getUsername() + "&r has been unfriended.");
            } else {
                sendChatMessage("Please specify either &6add&r or &6remove");
            }
        }
    }

    public Friends.Friend getFriendByName(String input) {
        ArrayList<NetworkPlayerInfo> infoMap = new ArrayList<NetworkPlayerInfo>(Minecraft.getMinecraft().getConnection().getPlayerInfoMap());
        NetworkPlayerInfo profile = infoMap.stream().filter(networkPlayerInfo -> networkPlayerInfo.getGameProfile().getName().equalsIgnoreCase(input)).findFirst().orElse(null);
        if (profile == null) {
            sendChatMessage("Player isn't online. Looking up UUID..");
            String s = requestIDs("[\"" + input + "\"]");
            if (s == null || s.isEmpty()) {
                sendChatMessage("Couldn't find player ID. Are you connected to the internet? (0)");
            } else {
                JsonElement element = new JsonParser().parse(s);
                if (element.getAsJsonArray().size() == 0) {
                    sendChatMessage("Couldn't find player ID. (1)");
                } else {
                    try {
                        String id = element.getAsJsonArray().get(0).getAsJsonObject().get("id").getAsString();
                        String username = element.getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();
                        return new Friends.Friend(username, UUIDTypeAdapter.fromString(id));
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendChatMessage("Couldn't find player ID. (2)");
                    }
                }
            }
            return null;
        }
        return new Friends.Friend(profile.getGameProfile().getName(), profile.getGameProfile().getId());
    }

    private static String requestIDs(String data) {
        try {
            String query = "https://api.mojang.com/profiles/minecraft";

            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes(StandardCharsets.UTF_8));
            os.close();

            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String res = convertStreamToString(in);
            in.close();
            conn.disconnect();

            return res;
        } catch (Exception e) {
            return null;
        }
    }

    private static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "/";
    }
}
