/*
 * Copyright 2016 - 2019 Florian Spieß and the java-discord-rpc contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package club.minnced.discord.rpc;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/*
typedef struct DiscordRichPresence {
    const char* state; // max 128 bytes
    const char* details; // max 128 bytes
    int64_t startTimestamp;
    int64_t endTimestamp;
    const char* largeImageKey; // max 32 bytes
    const char* largeImageText; // max 128 bytes
    const char* smallImageKey; // max 32 bytes
    const char* smallImageText; // max 128 bytes
    const char* partyId; // max 128 bytes
    int partySize;
    int partyMax;
    const char* matchSecret; // max 128 bytes
    const char* joinSecret; // max 128 bytes
    const char* spectateSecret; // max 128 bytes
    int8_t instance;
} DiscordRichPresence;
 */

/**
 * Struct binding for a RichPresence
 */
public class DiscordRichPresence extends Structure
{
    private static final List<String> FIELD_ORDER = Collections.unmodifiableList(Arrays.asList(
            "state",
            "details",
            "startTimestamp",
            "endTimestamp",
            "largeImageKey",
            "largeImageText",
            "smallImageKey",
            "smallImageText",
            "partyId",
            "partySize",
            "partyMax",
            "matchSecret",
            "joinSecret",
            "spectateSecret",
            "instance"
    ));

    public DiscordRichPresence(String encoding) {
        super();
        setStringEncoding(encoding);
    }

    public DiscordRichPresence() {
        this("UTF-8");
    }

    /**
     * The user's current party status.
     * <br>Example: "Looking to Play", "Playing Solo", "In a Group"
     * 
     * <p><b>Maximum: 128 characters</b>
     */
    public String state;

    /**
     * What the player is currently doing.
     * <br>Example: "Competitive - Captain's Mode", "In Queue", "Unranked PvP"
     * 
     * <p><b>Maximum: 128 characters</b>
     */
    public String details;

    /**
     * Unix timestamp (seconds) for the start of the game.
     * <br>Example: 1507665886
     */
    public long startTimestamp;

    /**
     * Unix timestamp (seconds) for the start of the game.
     * <br>Example: 1507665886
     */
    public long endTimestamp;

    /**
     * Name of the uploaded image for the large profile artwork.
     * <br>Example: "default"
     * 
     * <p><b>Maximum: 32 characters</b>
     */
    public String largeImageKey;

    /**
     * Tooltip for the largeImageKey.
     * <br>Example: "Blade's Edge Arena", "Numbani", "Danger Zone"
     * 
     * <p><b>Maximum: 128 characters</b>
     */
    public String largeImageText;

    /**
     * Name of the uploaded image for the small profile artwork.
     * <br>Example: "rogue"
     * 
     * <p><b>Maximum: 32 characters</b>
     */
    public String smallImageKey;

    /**
     * Tooltip for the smallImageKey.
     * <br>Example: "Rogue - Level 100"
     * 
     * <p><b>Maximum: 128 characters</b>
     */
    public String smallImageText;

    /**
     * ID of the player's party, lobby, or group.
     * <br>Example: "ae488379-351d-4a4f-ad32-2b9b01c91657"
     * 
     * <p><b>Maximum: 128 characters</b>
     */
    public String partyId;

    /**
     * Current size of the player's party, lobby, or group.
     * <br>Example: 1
     */
    public int partySize;

    /**
     * Maximum size of the player's party, lobby, or group.
     * <br>Example: 5
     */
    public int partyMax;

    /**
     * Unique hashed string for Spectate and Join.
     * Required to enable match interactive buttons in the user's presence.
     * <br>Example: "MmhuZToxMjMxMjM6cWl3amR3MWlqZA=="
     * 
     * <p><b>Maximum: 128 characters</b>
     */
    public String matchSecret;

    /**
     * Unique hashed string for Spectate button.
     * This will enable the "Spectate" button on the user's presence if whitelisted.
     * <br>Example: "MTIzNDV8MTIzNDV8MTMyNDU0"
     * 
     * <p><b>Maximum: 128 characters</b>
     */
    public String joinSecret;

    /**
     * Unique hashed string for chat invitations and Ask to Join.
     * This will enable the "Ask to Join" button on the user's presence if whitelisted.
     * <br>Example: "MTI4NzM0OjFpMmhuZToxMjMxMjM="
     * 
     * <p><b>Maximum: 128 characters</b>
     */
    public String spectateSecret;

    /**
     * Marks the matchSecret as a game session with a specific beginning and end.
     * Boolean value of 0 or 1.
     * <br>Example: 1
     */
    public byte instance;

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof DiscordRichPresence))
            return false;
        DiscordRichPresence presence = (DiscordRichPresence) o;
        return startTimestamp == presence.startTimestamp
                && endTimestamp == presence.endTimestamp
                && partySize == presence.partySize
                && partyMax == presence.partyMax
                && instance == presence.instance
                && Objects.equals(state, presence.state)
                && Objects.equals(details, presence.details)
                && Objects.equals(largeImageKey, presence.largeImageKey)
                && Objects.equals(largeImageText, presence.largeImageText)
                && Objects.equals(smallImageKey, presence.smallImageKey)
                && Objects.equals(smallImageText, presence.smallImageText)
                && Objects.equals(partyId, presence.partyId)
                && Objects.equals(matchSecret, presence.matchSecret)
                && Objects.equals(joinSecret, presence.joinSecret)
                && Objects.equals(spectateSecret, presence.spectateSecret);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(state, details, startTimestamp, endTimestamp, largeImageKey, largeImageText, smallImageKey,
                smallImageText, partyId, partySize, partyMax, matchSecret, joinSecret, spectateSecret, instance);
    }

    @Override
    protected List<String> getFieldOrder()
    {
        return FIELD_ORDER;
    }
}
