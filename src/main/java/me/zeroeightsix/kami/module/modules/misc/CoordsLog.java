package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.CoordUtil;
import me.zeroeightsix.kami.util.Coordinate;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;

@Module.Info(
        name = "CoordsLog",
        description = "Automatically writes the coordinates of the player to a file with a user defined delay between logs.",
        category = Module.Category.MISC,
        showOnArray = Module.ShowOnArray.ON
)
public class CoordsLog extends Module {
    private Setting<Boolean> forceLogOnDeath = register(Settings.b("SaveDeathCoords", true));
    private Setting<Boolean> deathInChat = register(Settings.b("LogInChat", true));
    private Setting<Boolean> autoLog = register(Settings.b("Delay", false));
    private Setting<Double> delay = register(Settings.doubleBuilder("Delay").withMinimum(1.0).withValue(15.0).withMaximum(60.0).build());
    private Setting<Boolean> checkDuplicates = register(Settings.b("AvoidDuplicates", true));

    private String previousCoord;

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
            Coordinate deathPoint = logCoordinates("deathPoint");
            if (deathInChat.getValue()) {
                sendChatMessage("You died at " + deathPoint.x + " " + deathPoint.y + " " + deathPoint.z);
            }
            playerIsDead = true;
        }
    }

    private static long startTime = 0;

    private void timeout() {
        if (startTime == 0)
            startTime = System.currentTimeMillis();

        if (startTime + (delay.getValue() * 1000) <= System.currentTimeMillis()) { // 1 timeout = 1 second = 1000 ms
            startTime = System.currentTimeMillis();
            Coordinate pos = CoordUtil.getCurrentCoord();
            String currentCoord = pos.toString();
            if (checkDuplicates.getValue()) {
                if (!currentCoord.equals(previousCoord)) {
                    logCoordinates("autoLogger");
                    previousCoord = currentCoord;
                }
            } else {
                logCoordinates("autoLogger");
                previousCoord = currentCoord;
            }
        }
    }

    private Coordinate logCoordinates(String name) {
        return CoordUtil.writePlayerCoords(name);
    }

    public void onDisable() {
        startTime = 0;
    }
}
