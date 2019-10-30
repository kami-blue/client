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

import com.sun.jna.Callback;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/*
typedef struct DiscordEventHandlers {
    void (*ready)(DiscordUser*);
    void (*disconnected)(int errorCode, const char* message);
    void (*errored)(int errorCode, const char* message);
    void (*joinGame)(const char* joinSecret);
    void (*spectateGame)(const char* spectateSecret);
    void (*joinRequest)(const DiscordUser* request);
} DiscordEventHandlers;
 */
/**
 * Struct containing handlers for RPC events
 * <br>Provided handlers can be null.
 */
public class DiscordEventHandlers extends Structure
{
    /**
     * Handler function for the ready event
     */
    public interface OnReady extends Callback
    {
        void accept(DiscordUser user);
    }

    /**
     * Handler function for the exceptional events (error, disconnect)
     */
    public interface OnStatus extends Callback
    {
        void accept(int errorCode, String message);
    }

    /**
     * Handler function for game update events (joinGame, spectateGame)
     */
    public interface OnGameUpdate extends Callback
    {
        void accept(String secret);
    }

    /**
     * Handler function for user join requests
     */
    public interface OnJoinRequest extends Callback
    {
        void accept(DiscordUser request);
    }

    private static final List<String> FIELD_ORDER = Collections.unmodifiableList(Arrays.asList(
       "ready",
       "disconnected",
       "errored",
       "joinGame",
       "spectateGame",
       "joinRequest"
    ));

    /**
     * Called when the RPC connection has been established
     */
    public OnReady ready;
    /**
     * Called when the RPC connection has been severed
     */
    public OnStatus disconnected;
    /**
     * Called when an internal error is caught within the SDK
     */
    public OnStatus errored;
    /**
     * Called when the logged in user joined a game
     */
    public OnGameUpdate joinGame;
    /**
     * Called when the logged in user joined to spectate a game
     */
    public OnGameUpdate spectateGame;
    /**
     * Called when another discord user wants to join the game of the logged in user
     */
    public OnJoinRequest joinRequest;

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof DiscordEventHandlers))
            return false;
        DiscordEventHandlers that = (DiscordEventHandlers) o;
        return Objects.equals(ready, that.ready)
                && Objects.equals(disconnected, that.disconnected)
                && Objects.equals(errored, that.errored)
                && Objects.equals(joinGame, that.joinGame)
                && Objects.equals(spectateGame, that.spectateGame)
                && Objects.equals(joinRequest, that.joinRequest);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ready, disconnected, errored, joinGame, spectateGame, joinRequest);
    }

    @Override
    protected List<String> getFieldOrder()
    {
        return FIELD_ORDER;
    }
}
