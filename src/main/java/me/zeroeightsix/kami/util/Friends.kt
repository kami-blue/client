package me.zeroeightsix.kami.util;

import com.google.common.base.Converter;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by 086 on 13/12/2017.
 */
public class Friends {
    public static final Friends INSTANCE = new Friends();

    public static Setting<ArrayList<Friend>> friends;

    private Friends() {
    }

    public static void initFriends() {
        friends = Settings.custom("Friends", new ArrayList<Friend>(), new FriendListConverter()).buildAndRegister("friends");
    }

    public static boolean isFriend(String name) {
        return friends.getValue().stream().anyMatch(friend -> friend.username.equalsIgnoreCase(name));
    }

    public static class Friend {
        String username;
        UUID uuid;

        public Friend(String username, UUID uuid) {
            this.username = username;
            this.uuid = uuid;
        }

        public String getUsername() {
            return username;
        }
    }

    public static class FriendListConverter extends Converter<ArrayList<Friend>, JsonElement> {
        public FriendListConverter() {
        }

        @Override
        protected JsonElement doForward(ArrayList<Friend> list) {
            StringBuilder present = new StringBuilder();
            for (Friend friend : list)
                present.append(String.format("%s;%s$", friend.username, friend.uuid.toString()));
            return new JsonPrimitive(present.toString());
        }

        @Override
        protected ArrayList<Friend> doBackward(JsonElement jsonElement) {
            String v = jsonElement.getAsString();
            String[] pairs = v.split(Pattern.quote("$"));
            ArrayList<Friend> friends = new ArrayList<>();
            for (String pair : pairs) {
                try {
                    String[] split = pair.split(";");
                    String username = split[0];
                    UUID uuid = UUID.fromString(split[1]);
                    friends.add(new Friend(getUsernameByUUID(uuid, username), uuid));
                } catch (Exception ignored) {
                } // Empty line, wrong formatting or something, we don't care
            }
            return friends;
        }

        private String getUsernameByUUID(UUID uuid, String saved) {
            String src = getSource("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString());
            if (src == null || src.isEmpty()) return saved;
            try {
                JsonElement object = new JsonParser().parse(src);
                return object.getAsJsonObject().get("name").getAsString();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(src);
                return saved;
            }
        }

        private static String getSource(String link) {
            try {
                URL u = new URL(link);
                URLConnection con = u.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder buffer = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    buffer.append(inputLine);
                in.close();

                return buffer.toString();
            } catch (Exception e) {
                return null;
            }
        }
    }

}
