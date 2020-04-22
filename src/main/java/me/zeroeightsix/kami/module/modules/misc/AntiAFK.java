package me.zeroeightsix.kami.module.modules.misc;

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

    private Setting<Integer> frequency = register(Settings.integerBuilder("Action Frequency").withMinimum(1).withMaximum(100).withValue(40).build());
    public Setting<Boolean> autoReply = register(Settings.b("AutoReply", true));
    private Setting<Mode> mode = register(Settings.enumBuilder(Mode.class).withName("Mode").withValue(Mode.TRADITIONAL).withVisibility(v -> false).build());
    private Setting<Boolean> swing = register(Settings.b("Swing", true));
    private Setting<Boolean> jump = register(Settings.b("Jump", true));
    private Setting<Boolean> turn = register(Settings.booleanBuilder("Turn").withValue(true).withVisibility(v -> mode.getValue().equals(Mode.TRADITIONAL)).build());

    private Random random = new Random();
    private enum Mode { TRADITIONAL, CHUNK }
//    private int[] pos = { 0, 0 };
//
//    public void onEnable() {
//        if (mc.player == null) {
//            return;
//        }
//
//        if (mode.getValue().equals(Mode.CHUNK)) {
//            pos[0] = (int) mc.player.posX;
//            pos[1] = (int) mc.player.posZ;
//            sendChatMessage(getChatName() + "Registered chunk: X: [" + pos[0] + "][" + (pos[0] + 16) + "] Z: [" + pos[1] + "][" + (pos[1] + 16) + "]");
//        }
//    }
//
//    private boolean insideChunk() {
//        return (mc.player.posX > pos[0] && pos[0] + 16 > mc.player.posX) && (mc.player.posZ > pos[1] && pos[1] + 16 > mc.player.posZ);
//    }

    @Override
    public void onUpdate() {
        if (mc.playerController.getIsHittingBlock()) return;

        if (swing.getValue() && mc.player.ticksExisted % (0.5 * getFrequency()) == 0) {
            Objects.requireNonNull(mc.getConnection()).sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
        }

        if (jump.getValue() && mc.player.ticksExisted % (2 * getFrequency()) == 0) {
            mc.player.jump();
        }

        if (mode.getValue().equals(Mode.TRADITIONAL) && turn.getValue() && mc.player.ticksExisted % (0.375 * getFrequency()) == 0) {
            mc.player.rotationYaw = random.nextInt(360) - makeNegRandom(180);
        }
    }

    @EventHandler
    public Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (autoReply.getValue() && event.getPacket() instanceof SPacketChat && ((SPacketChat) event.getPacket()).getChatComponent().getUnformattedText().contains("whispers: ") && !((SPacketChat) event.getPacket()).getChatComponent().getUnformattedText().contains(mc.player.getName())) {
            sendServerMessage("/r I am currently AFK and using KAMI Blue!");
        }
    });

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
