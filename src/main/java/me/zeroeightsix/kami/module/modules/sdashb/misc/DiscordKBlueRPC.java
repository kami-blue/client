package me.zeroeightsix.kami.module.modules.sdashb.misc;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

// discord imports
import club.minnced.discord.rpc.*;
import com.google.common.hash.Hashing;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.fml.common.FMLLog;
 
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * @author cookiedragon234
 * Updated by S-B99 on 28/10/19
 */
@Module.Info(name = "DiscordRPC", category = Module.Category.MISC, description = "Discord Rich Presence")
public class DiscordKBlueRPC extends Module {

	//1//protected void onEnable() {
	//1//
	//1//	if (isDisabled()) {
	//1//		this.disable();
	//1//		return;
	//1//	}
	//1//}

    //2//private static final String APP_ID = "638403216278683661";
	//2//private static final DiscordKBlueRPC rpc = DiscordKBlueRPC.INSTANCE;
	//2//
	//2//private static DiscordRichPresence presence = new DiscordRichPresence();
	//2//
	//2//private static boolean hasStarted = false;
	//2//
	//2//public static boolean start()
	//2//{
	//2//FMLLog.log.info("Starting Discord RPC");
	//2//   
	//2//    if(hasStarted) return false;
	//2//   
	//2//    hasStarted = true;
	//2//   
	//2//    DiscordEventHandlers handlers = new DiscordEventHandlers();
	//2//   
	//2//    handlers.disconnected = (int var1, String var2) ->
	//2//    {
	//2//        System.out.println("Discord RPC disconnected, var1: " + String.valueOf(var1) + ", var2: " + var2);
	//2//    };
	//2//   
	//2//   rpc.Discord_Initialize(APP_ID, handlers, true, "");
	//2//   
	//2//    presence.startTimestamp = System.currentTimeMillis() / 1000;
	//2//    presence.details = "Main Menu";
	//2//    presence.state = "discord.gg/ncQkFKU";
	//2//    //presence.smallImageKey = "backdoored_logo";
	//2//    presence.largeImageKey = "backdoored_logo";
	//2//    //presence.spectateSecret = String.valueOf(new Random().nextInt((9000000 - 100000) + 1) + 100000);
	//2//    //presence.joinSecret = String.valueOf(new Random().nextInt((9000000 - 100000) + 1) + 100000);
	//2//   
	//2//   rpc.Discord_UpdatePresence(presence);
	//2//   
	//2//    new Thread(() ->
	//2//    {
	//2//        while(!Thread.currentThread().isInterrupted())
	//2//        {
	//2//           
	//2//            try
	//2//            {
	//2//                // Run callbacks
	//2//                rpc.Discord_RunCallbacks();
	//2//               
	//2//                String details = "";
	//2//                String state = "";
	//2//                int players = 0;
	//2//                int maxPlayers = 0;
	//2//               
	//2//                // If we're in singleplayer
	//2//                if (Globals.mc.isIntegratedServerRunning())
	//2//                {
	//2//                    details = "Singleplayer";
	//2//                }
	//2//                else
	//2//                {
	//2//                    if (Globals.mc.getCurrentServerData() != null)
	//2//                    {
	//2//                        ServerData svr = Globals.mc.getCurrentServerData();
	//2//                        if (!svr.serverIP.equals(""))
	//2//                        {
	//2//                            // If we're on multiplayer
	//2//                            details = "Multiplayer";
	//2//                            state = svr.serverIP;
	//2//                            if(svr.populationInfo != null)
	//2//                            {
	//2//                                String[] popInfo = svr.populationInfo.split("/");
	//2//                                if(popInfo.length > 2)
	//2//                               {
	//2//                                    players = Integer.valueOf(popInfo[0]);
	//2//                                    maxPlayers = Integer.valueOf(popInfo[1]);
	//2//                                }
	//2//                            }
	//2//                           
	//2//                            if(state.contains("2b2t.org"))
	//2//                            {
	//2//                                try
	//2//                                {
	//2//                                    if(Backdoored.lastChat.startsWith("Position in queue: "))
	//2//                                    {
	//2//                                        state = state + " " + Integer.parseInt(Backdoored.lastChat.substring(19)) + " in queue";
	//2//                                    }
	//2//                                } catch(Throwable e){ e.printStackTrace(); }
	//2//                            }
	//2//                        }
	//2//                    }
	//2//                    // If we're in the main menu
	//2//                    else
	//2//                    {
	//2//                        details = "Main Menu";
	//2//                        state = "discord.gg/ncQkFKU";
	//2//                    }
	//2//                }
	//2//               
	//2//                if(!details.equals(presence.details) || !state.equals(presence.state))
	//2//                {
	//2//                    presence.startTimestamp = System.currentTimeMillis() / 1000;
	//2//                }
	//2//               
	//2//                presence.details = details;
	//2//                presence.state = state;
	//2//                //presence.partySize = players;
	//2//                //presence.partyMax = maxPlayers;
	//2//               
	//2//                /*if(players > 0)
	//2//                {
	//2//                    presence.partyId = String.valueOf(new Random().nextInt((9000000 - 100000) + 1) + 100000);
	//2//                }*/
	//2//               
	//2//                rpc.Discord_UpdatePresence(presence);
	//2//            } catch(Exception e){e.printStackTrace();}
	//2//           
	//2//            try
	//2//            {
	//2//                Thread.sleep(5000);
	//2//            }
	//2//            catch(InterruptedException e)
	//2//            {
	//2//                e.printStackTrace();
	//2//            }
	//2//        }
	//2//    }, "Discord-RPC-Callback-Handler").start();
	//2//    FMLLog.log.info("Discord RPC initialised succesfully");
	//2//    return true;
	//2//}
}
