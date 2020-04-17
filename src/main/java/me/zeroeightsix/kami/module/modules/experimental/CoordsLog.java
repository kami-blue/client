package me.zeroeightsix.kami.module.modules.experimental;

import me.zeroeightsix.kami.util.LogUtil;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;

import net.minecraft.client.Minecraft;

@Module.Info(name = "CoordsLog", description = "Automatically writes the coordinates of the player to a file with a user defined delay between logs.", category = Module.Category.EXPERIMENTAL, showOnArray = Module.ShowOnArray.ON)
public class CoordsLog extends Module {
    private Setting<Boolean> forceLogOnDeath = register(Settings.b("onDeath", true));
    private Setting<Boolean> deathInChat = register(Settings.b("logDeathInChat", true));
    private Setting<Boolean> autoLog = register(Settings.b("onDelay", false));
    private Setting<Double> delay = register(Settings.doubleBuilder("delay").withMinimum(1.0).withValue(15.0).withMaximum(60.0).build());
    private Setting<Boolean> checkDuplicates = register(Settings.b("avoidDuplicates", true));
    private Setting<Boolean> useChunkCoord = register(Settings.b("useChunk", false));


    private int previousCoord;

    private boolean playerIsDead = false;

    @Override
    public void onUpdate() {
        if (mc.player == null)
            return;
        if (autoLog.getValue()) {
            timeout();
        }
        if (0 < mc.player.getHealth() && playerIsDead) {
            playerIsDead = false;
        }
        if (!playerIsDead && 0 >= mc.player.getHealth() && forceLogOnDeath.getValue()) {
            int[] deathPoint = logCoordinates("deathPoint");
            if (deathInChat.getValue()) {
                sendChatMessage("Player died at " + deathPoint[0] + " " + deathPoint[1] + " " + deathPoint[2]);
                playerIsDead = true;
            } else {
                playerIsDead = true;
            }
        }
    }

    private static long startTime = 0;

    private void timeout() {
        if (startTime == 0)
            startTime = System.currentTimeMillis();
        if (startTime + (delay.getValue() * 1000) <= System.currentTimeMillis()) { // 1 timeout = 1 second = 1000 ms
            startTime = System.currentTimeMillis();
            int[] cCArray = LogUtil.getCurrentCoord(useChunkCoord.getValue());
            int currentCoord = cCArray[0]*3 + cCArray[1]*32 + cCArray[2]/2;
            if (checkDuplicates.getValue()) {
                if (currentCoord != previousCoord) {
                    logCoordinates("autoLogger");
                    previousCoord = currentCoord;
                }
            } else {
                logCoordinates("autoLogger");
                previousCoord = currentCoord;
            }
        }
    }

    private int[] logCoordinates(String name) {
        return LogUtil.writePlayerCoords(name, useChunkCoord.getValue());
    }

    public void onDisable() {
        startTime = 0;
    }
}
