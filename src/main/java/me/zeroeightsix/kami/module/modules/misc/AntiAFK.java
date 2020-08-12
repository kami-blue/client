package me.zeroeightsix.kami.module.modules.misc;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalXZ;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.EnumHand;

import java.util.Objects;
import java.util.Random;

import static me.zeroeightsix.kami.util.MathsUtils.reverseNumber;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendServerMessage;

/**
 * Created by 086 on 16/12/2017.
 * Updated by dominikaaaa on 21/04/20
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
    private final Setting<Integer> radius = register(Settings.integerBuilder("Radius").withMinimum(1).withValue(64).build());
    private final Setting<Boolean> turn = register(Settings.b("Turn", true));

    private final Random random = new Random();

    private final int[] squareStartCoords = {0, 0};
    private int squareStep = 0;

    @Override
    public void onEnable() {
        if (mc.player == null)
            return;

        squareStartCoords[0] = (int) mc.player.posX;
        squareStartCoords[1] = (int) mc.player.posZ;
    }

    @Override
    public void onDisable() {
        if (mc.player == null)
            return;

        if (isBaritoneActive())
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
    }

    @Override
    public void onUpdate() {
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

    @EventHandler
    public Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (autoReply.getValue() && event.getPacket() instanceof SPacketChat && ((SPacketChat) event.getPacket()).getChatComponent().getUnformattedText().contains("whispers: ") && !((SPacketChat) event.getPacket()).getChatComponent().getUnformattedText().contains(mc.player.getName())) {
            sendServerMessage("/r I am currently AFK and using KAMI Blue!");
        }
    });

    private boolean isBaritoneActive() {
        return BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().isActive();
    }

    private void baritoneGotoXZ(int x, int z) {
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ(x, z));
    }

    private int clamp(int val) {
        if (val < 0)
            return 0;
        return val;
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
