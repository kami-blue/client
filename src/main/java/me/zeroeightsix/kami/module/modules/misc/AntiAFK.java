package me.zeroeightsix.kami.module.modules.misc;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalXZ;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.Timer;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.util.Objects;
import java.util.Random;

import static me.zeroeightsix.kami.util.MathsUtils.reverseNumber;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendServerMessage;

/**
 * Created by 086 on 16/12/2017.
 * Updated by dominikaaaa on 21/04/20
 * Updated by Xiaro on 23/08/20
 * TODO: Path finding to stay inside 1 chunk
 * TODO: Render which chunk is selected
 */
@Module.Info(
        name = "AntiAFK",
        category = Module.Category.MISC,
        description = "Prevents being kicked for AFK"
)
public class AntiAFK extends Module {

    private final Setting<Integer> frequency = register(Settings.integerBuilder("ActionFrequency").withMinimum(1).withMaximum(100).withValue(40).build());
    public Setting<Boolean> autoReply = register(Settings.b("AutoReply", true));
    private final Setting<Boolean> swing = register(Settings.b("Swing", true));
    private final Setting<Boolean> jump = register(Settings.b("Jump", true));
    private final Setting<Boolean> squareWalk = register(Settings.b("SquareWalk", true));
    private final Setting<Integer> radius = register(Settings.integerBuilder("Radius").withMinimum(1).withValue(64).withVisibility(v -> squareWalk.getValue()).build());
    private final Setting<Boolean> turn = register(Settings.b("Turn", true));
    private final Setting<Integer> inputTimeout = register(Settings.integerBuilder("InputTimeout(m)").withValue(0).withRange(0, 15).build());

    private final Random random = new Random();

    private final int[] squareStartCoords = {0, 0};
    private int squareStep = 0;
    private Boolean baritoneDisconnectOnArrival = false;
    private final Timer inputTimer = new Timer(Timer.TimeUnit.MINUTES);

    public AntiAFK() {
        super();

        squareWalk.settingListener = setting -> {
            if (isEnabled()) {
                BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
            }
        };
    }

    @Override
    public void onEnable() {
        if (mc.player == null || inputTimeout.getValue() != 0)
            return;

        baritoneDisconnectOnArrival = BaritoneAPI.getSettings().disconnectOnArrival.value;
        BaritoneAPI.getSettings().disconnectOnArrival.value = false;
        squareStartCoords[0] = (int) mc.player.posX;
        squareStartCoords[1] = (int) mc.player.posZ;
    }

    @Override
    public void onDisable() {
        if (mc.player == null)
            return;

        BaritoneAPI.getSettings().disconnectOnArrival.value = baritoneDisconnectOnArrival;
        if (isBaritoneActive() && squareWalk.getValue())
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
    }

    @Override
    public void onUpdate() {
        if (inputTimeout.getValue() != 0) {
            if (!squareWalk.getValue() && BaritoneAPI.getProvider().getPrimaryBaritone().getPathingControlManager().mostRecentInControl().isPresent()) {
                inputTimer.reset();
            }
            if (!inputTimer.tick(inputTimeout.getValue(), false)) {
                return;
            }
        }

        if (mc.playerController.getIsHittingBlock()) return;

        if (swing.getValue() && mc.player.ticksExisted % (0.5 * getFrequency()) == 0) {
            Objects.requireNonNull(mc.getConnection()).sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
        }

        if (squareWalk.getValue() && mc.player.ticksExisted % getFrequency() == 0 && !isBaritoneActive()) {
            int r = clamp(radius.getValue());
            switch (squareStep) {
                // +z
                case 0:
                    baritoneGotoXZ(squareStartCoords[0], squareStartCoords[1] + r);
                    break;
                // +x
                case 1:
                    baritoneGotoXZ(squareStartCoords[0] + r, squareStartCoords[1] + r);
                    break;
                // -z
                case 2:
                    baritoneGotoXZ(squareStartCoords[0] + r, squareStartCoords[1]);
                    break;
                // -x
                case 3:
                    baritoneGotoXZ(squareStartCoords[0], squareStartCoords[1]);
                    break;
            }
            squareStep = (squareStep + 1) % 4;
        }

        if (jump.getValue() && mc.player.ticksExisted % (2 * getFrequency()) == 0) {
            mc.player.jump();
        }

        if (turn.getValue() && mc.player.ticksExisted % (0.375 * getFrequency()) == 0) {
            mc.player.rotationYaw = random.nextInt(360) - makeNegRandom(180);
        }
    }

    @Override
    public String getHudInfo() {
        return String.valueOf(System.currentTimeMillis() - inputTimer.lastTickTime);
    }

    @EventHandler
    private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (autoReply.getValue() && event.getPacket() instanceof SPacketChat && ((SPacketChat) event.getPacket()).getChatComponent().getUnformattedText().contains("whispers: ") && !((SPacketChat) event.getPacket()).getChatComponent().getUnformattedText().contains(mc.player.getName())) {
            sendServerMessage("/r I am currently AFK and using KAMI Blue!");
        }
    });

    @EventHandler
    private final Listener<InputEvent.MouseInputEvent> mouseInputListener = new Listener<>(event -> {
        if (inputTimeout.getValue() != 0 && isInputting()) {
            inputTimer.reset();
        }
    });

    @EventHandler
    private final Listener<InputEvent.KeyInputEvent> keyInputListener = new Listener<>(event -> {
        if (inputTimeout.getValue() != 0 && isInputting()) {
            inputTimer.reset();
        }
    });

    private boolean isInputting() {
        return mc.gameSettings.keyBindAttack.isKeyDown()
                || mc.gameSettings.keyBindUseItem.isKeyDown()
                || mc.gameSettings.keyBindJump.isKeyDown()
                || mc.gameSettings.keyBindSneak.isKeyDown()
                || mc.gameSettings.keyBindForward.isKeyDown()
                || mc.gameSettings.keyBindBack.isKeyDown()
                || mc.gameSettings.keyBindLeft.isKeyDown()
                || mc.gameSettings.keyBindRight.isKeyDown();
    }

    private boolean isBaritoneActive() {
        return BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().isActive();
    }

    private void baritoneGotoXZ(int x, int z) {
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ(x, z));
    }

    private int clamp(int val) {
        return Math.max(val, 0);
    }

    private float getFrequency() {
        return reverseNumber(frequency.getValue(), 1, 100);
    }

    private int makeNegRandom(int input) {
        int rand = random.nextBoolean() ? 1 : 0;
        if (rand == 0) {
            return -input;
        } else {
            return input;
        }
    }
}
